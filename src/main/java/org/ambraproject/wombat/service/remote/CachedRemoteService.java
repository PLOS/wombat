package org.ambraproject.wombat.service.remote;

import com.google.common.base.Preconditions;
import org.ambraproject.rhombat.HttpDateUtil;
import org.ambraproject.rhombat.cache.Cache;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.Calendar;

/**
 * Decorator class that adds caching capability to a wrapped {@link RemoteService} object. The uncached RemoteService
 * methods are still available through delegating methods.
 *
 * @param <S>
 */
public class CachedRemoteService<S extends Closeable> implements RemoteService<S> {
  private static final Logger log = LoggerFactory.getLogger(CachedRemoteService.class);

  private final RemoteService<S> remoteService;
  private final Cache cache;

  public CachedRemoteService(RemoteService<S> remoteService, Cache cache) {
    this.remoteService = Preconditions.checkNotNull(remoteService);
    this.cache = Preconditions.checkNotNull(cache);
  }

  @Override // delegate
  public CloseableHttpResponse getResponse(URI targetUri, Header... headers) throws IOException {
    return remoteService.getResponse(targetUri, headers);
  }

  @Override // delegate
  public S request(URI targetUri) throws IOException {
    return remoteService.request(targetUri);
  }

  @Override // delegate
  public S open(HttpEntity entity) throws IOException {
    return remoteService.open(entity);
  }

  /**
   * Representation of a stream from the SOA service and a timestamp indicating when it was last modified.
   */
  private static class TimestampedResponse implements Closeable {
    private final Calendar timestamp;
    private final CloseableHttpResponse response;

    private TimestampedResponse(Calendar timestamp, CloseableHttpResponse response) {
      this.timestamp = timestamp; // null if the service does not support the If-Modified-Since header for this request
      this.response = response; // null if the object has not been modified since a given time
    }

    @Override
    public void close() throws IOException {
      if (response != null) {
        response.close();
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

  public <T> T requestCached(String cacheKey, URI address, CacheDeserializer<? super S, ? extends T> callback)
      throws IOException {
    Preconditions.checkNotNull(address);
    Preconditions.checkNotNull(callback);

    CachedObject<T> cached = getCachedObject(cacheKey);
    Calendar lastModified = getLastModified(cached);

    try (TimestampedResponse fromServer = requestIfModifiedSince(address, lastModified)) {
      if (fromServer.response != null) {
        try (S stream = remoteService.open(fromServer.response.getEntity())) {
          T value = callback.read(stream);
          if (fromServer.timestamp != null) {
            cache.put(cacheKey, new CachedObject<>(fromServer.timestamp, value));
          }
          return value;
        }
      } else {
        return cached.object;
      }
    }
  }

  private <T> CachedObject<T> getCachedObject(String cacheKey) {
    try {
      return cache.get(Preconditions.checkNotNull(cacheKey));
    } catch (Exception e) {
      // Unexpected, but to degrade gracefully, treat it the same as a cache miss
      log.error("Error accessing cache", e);
      return null;
    }
  }

  private static <T> Calendar getLastModified(CachedObject<? extends T> cached) {
    if (cached == null) {
      Calendar lastModified = Calendar.getInstance();
      lastModified.setTimeInMillis(0);  // Set to beginning of epoch since it's not in the cache
      return lastModified;
    } else {
      return cached.timestamp;
    }
  }

  /**
   * Requests a stream, using the "If-Modified-Since" header in the request so that the object will only be returned if
   * it was modified after the given time.  Otherwise, the stream field of the returned object will be null.  This is
   * useful when results from the SOA service are being added to a cache, and we only want to retrieve the result if it
   * is newer than the version stored in the cache.
   *
   * @param address      the address to which to send the REST request
   * @param lastModified the object will be returned iff the SOA server indicates that it was modified after this
   *                     timestamp
   * @return a timestamped stream, or a null stream with non-null timestamp
   * @throws IOException
   */
  private TimestampedResponse requestIfModifiedSince(URI address, Calendar lastModified) throws IOException {
    Preconditions.checkNotNull(lastModified);
    CloseableHttpResponse response = null;
    boolean returningStream = false;
    try {
      response = remoteService.getResponse(address, new BasicHeader("If-Modified-Since", HttpDateUtil.format(lastModified)));
      Header[] lastModifiedHeaders = response.getHeaders("Last-Modified");
      if (lastModifiedHeaders.length == 0) {
        TimestampedResponse timestamped = new TimestampedResponse(null, response);
        returningStream = true;
        return timestamped;
      }
      if (lastModifiedHeaders.length != 1) {
        throw new RuntimeException("Expecting 1 Last-Modified header, got " + lastModifiedHeaders.length);
      }
      Calendar resultLastModified = HttpDateUtil.parse(lastModifiedHeaders[0].getValue());

      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode == 200) {
        TimestampedResponse timestamped = new TimestampedResponse(resultLastModified, response);
        returningStream = true;
        return timestamped;
      } else if (statusCode == 304) {
        return new TimestampedResponse(resultLastModified, null);
      } else {
        throw new RuntimeException("Unexpected status code " + statusCode);
      }
    } finally {
      if (!returningStream && response != null) {
        response.close();
      }
    }
  }

}
