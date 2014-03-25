package org.ambraproject.wombat.service;

import com.google.common.base.Preconditions;
import org.ambraproject.rhombat.HttpDateUtil;
import org.ambraproject.rhombat.cache.Cache;
import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.Calendar;

public class SoaServiceImpl extends JsonService implements SoaService {

  @Autowired
  private RuntimeConfiguration runtimeConfiguration;
  @Autowired
  private Cache cache;

  @Override
  public InputStream requestStream(String address) throws IOException {
    return requestStream(buildUri(address));
  }

  @Override
  public <T> T requestObject(String address, Class<T> responseClass) throws IOException {
    // Just try to cache everything. We may want to narrow this in the future.
    return requestCachedObject("obj:" + address, address, responseClass);
  }


  /**
   * Representation of a stream from the SOA service and a timestamp indicating when it was last modified.
   */
  private static class TimestampedStream implements Closeable {
    private final Calendar timestamp;
    private final InputStream stream;

    private TimestampedStream(Calendar timestamp, InputStream stream) {
      this.timestamp = timestamp; // null if the service does not support the If-Modified-Since header for this request
      this.stream = stream; // null if the object has not been modified since a given time
    }

    @Override
    public void close() throws IOException {
      if (stream != null) {
        stream.close();
      }
    }
  }

  /**
   * Representation of an object stored in the cache.
   */
  private static class CachedObject<T> implements Serializable {
    // Avoid mutating these fields, which are public and non-final for the sake of net.spy.memcached.MemcachedClient.
    public Calendar timestamp;
    public T object;

    private CachedObject(Calendar timestamp, T object) {
      this.timestamp = Preconditions.checkNotNull(timestamp);
      this.object = object; // nullable
    }
  }

  @Override
  public <T> T requestCachedStream(String cacheKey, String address, CacheCallback<? extends T> callback) throws IOException {
    Preconditions.checkNotNull(cacheKey);
    Preconditions.checkNotNull(address);
    Preconditions.checkNotNull(callback);

    CachedObject<T> cached = cache.get(cacheKey);
    Calendar lastModified;
    if (cached == null) {
      lastModified = Calendar.getInstance();
      lastModified.setTimeInMillis(0);  // Set to beginning of epoch since it's not in the cache
    } else {
      lastModified = cached.timestamp;
    }

    try (TimestampedStream fromServer = requestStreamIfModifiedSince(address, lastModified);
         InputStream streamFromServer = fromServer.stream) {
      if (streamFromServer != null) {
        T value = callback.call(streamFromServer);
        if (fromServer.timestamp != null) {
          cache.put(cacheKey, new CachedObject<>(fromServer.timestamp, value));
        }
        return value;
      } else {
        return cached.object;
      }
    }
  }

  /**
   * Requests a stream, using the "If-Modified-Since" header in the request so that the object will only be returned if
   * it was modified after the given time.  Otherwise, the stream field of the returned object will be null.  This is
   * useful when results from the SOA service are being added to a cache, and we only want to retrieve the result if it
   * is newer than the version stored in the cache.
   *
   * @param address      the path to which to send the REST request
   * @param lastModified the object will be returned iff the SOA server indicates that it was modified after this
   *                     timestamp
   * @return a timestamped stream, or a null stream with non-null timestamp
   * @throws IOException
   */
  private TimestampedStream requestStreamIfModifiedSince(String address, Calendar lastModified) throws IOException {
    Preconditions.checkNotNull(lastModified);
    URI uri = buildUri(address);
    CloseableHttpResponse response = null;
    boolean returningStream = false;
    try {
      response = makeRequest(uri, new BasicHeader("If-Modified-Since", HttpDateUtil.format(lastModified)));
      Header[] lastModifiedHeaders = response.getHeaders("Last-Modified");
      if (lastModifiedHeaders.length == 0) {
        TimestampedStream timestamped = new TimestampedStream(null, new BufferedInputStream(response.getEntity().getContent()));
        returningStream = true;
        return timestamped;
      }
      if (lastModifiedHeaders.length != 1) {
        throw new RuntimeException("Expecting 1 Last-Modified header, got " + lastModifiedHeaders.length);
      }
      Calendar resultLastModified = HttpDateUtil.parse(lastModifiedHeaders[0].getValue());

      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode == 200) {
        TimestampedStream timestamped = new TimestampedStream(resultLastModified, new BufferedInputStream(response.getEntity().getContent()));
        returningStream = true;
        return timestamped;
      } else if (statusCode == 304) {
        return new TimestampedStream(resultLastModified, null);
      } else {
        throw new RuntimeException("Unexpected status code " + statusCode);
      }
    } finally {
      if (!returningStream && response != null) {
        response.close();
      }
    }
  }

  @Override
  public <T> T requestCachedObject(String cacheKey, final String address, final Class<T> responseClass) throws IOException {
    Preconditions.checkNotNull(responseClass);
    return requestCachedStream(cacheKey, address, new CacheCallback<T>() {
      @Override
      public T call(InputStream stream) throws IOException {
        return deserializeStream(responseClass, stream, address);
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CloseableHttpResponse requestAsset(String assetId, Header... headers) throws IOException {
    return makeRequest(buildUri("assetfiles/" + assetId), headers);
  }

  private URI buildUri(String address) {
    return buildUri(runtimeConfiguration.getServer(), address);
  }
}
