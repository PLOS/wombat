package org.ambraproject.wombat.service.remote;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.ambraproject.rhombat.HttpDateUtil;
import org.ambraproject.rhombat.cache.Cache;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Calendar;

/**
 * Superclass for service beans that make calls to remote APIs.
 */
abstract class RemoteService {
  private static final Logger log = LoggerFactory.getLogger(RemoteService.class);

  @Autowired
  private HttpClientConnectionManager httpClientConnectionManager;
  @Autowired
  private Cache cache;

  private CloseableHttpClient createClient() {
    return HttpClientBuilder.create()
        .setConnectionManager(httpClientConnectionManager)
        .build();
  }

  /**
   * Makes a request to the given URI, checks for response codes 400 or above, and returns the response.
   * <p/>
   * The caller <em>must</em> either close the returned {@code CloseableHttpResponse} object or close the {@code
   * InputStream} to the response body (i.e., {@code CloseableHttpResponse.getEntity().getContent()}). This is very
   * important, because leaving responses hanging open can starve the connection pool and cause horrible timeouts.
   *
   * @param targetUri the URI to which to send the request
   * @param headers   headers to add to the request, if any
   * @return response from the server
   * @throws IOException                                             if there is an error connecting to the server
   * @throws NullPointerException                                    if the address is null
   * @throws org.ambraproject.wombat.service.EntityNotFoundException if the object at the address does not exist
   */
  protected CloseableHttpResponse makeRequest(URI targetUri, Header... headers) throws IOException {
    // Don't close the client, as this shuts down the connection pool. Do close every response or its entity stream.
    CloseableHttpClient client = createClient();
    HttpGet get = new HttpGet(targetUri);
    for (Header header : headers) {
      get.setHeader(header);
    }

    // We want to return an unclosed response, so close the response only if we throw an exception.
    boolean returningResponse = false;
    CloseableHttpResponse response = null;
    try {
      response = client.execute(get);
      StatusLine statusLine = response.getStatusLine();
      if (statusLine.getStatusCode() >= 400) {
        String address = targetUri.getPath();
        if (!Strings.isNullOrEmpty(targetUri.getQuery())) {
          address += "?" + targetUri.getQuery();
        }
        if (statusLine.getStatusCode() == 404) {
          throw new EntityNotFoundException(address);
        } else {
          String message = String.format("Request to \"%s\" failed (%d): %s",
              address, statusLine.getStatusCode(), statusLine.getReasonPhrase());
          throw new ServiceRequestException(message);
        }
      }
      returningResponse = true;
      return response;
    } finally {
      if (!returningResponse && response != null) {
        response.close();
      }
    }
  }

  /**
   * Send a ReST request and open a stream as the response.
   * <p/>
   * The caller must close the stream when it is done reading. This is important for connection pooling.
   *
   * @param targetUri the URI to which to send the REST request
   * @return a stream to the response
   * @throws java.io.IOException                                     if there is an error connecting to the server
   * @throws NullPointerException                                    if the address is null
   * @throws org.ambraproject.wombat.service.EntityNotFoundException if the object at the address does not exist
   */
  protected InputStream requestStream(URI targetUri) throws IOException {
    return requestEntity(targetUri).getContent();
  }

  protected Reader requestReader(URI targetUri) throws IOException {
    return openReader(requestEntity(targetUri));
  }

  private HttpEntity requestEntity(URI targetUri) throws IOException {
    Preconditions.checkNotNull(targetUri);

    // We must leave the response open if we return a valid stream, but must close it otherwise.
    boolean returningResponse = false;
    CloseableHttpResponse response = null;
    try {
      response = makeRequest(targetUri);
      HttpEntity entity = response.getEntity();
      if (entity == null) {
        throw new RuntimeException("No response");
      }
      returningResponse = true;
      return entity;
    } finally {
      if (!returningResponse && response != null) {
        response.close();
      }
    }
  }

  private static Reader openReader(HttpEntity entity) throws IOException {
    Charset charset = ContentType.getOrDefault(entity).getCharset();
    return new InputStreamReader(entity.getContent(), charset);
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

  protected <T> T requestCachedStream(String cacheKey, URI address,
                                      CacheDeserializer<InputStream, T> callback)
      throws IOException {
    Preconditions.checkNotNull(address);
    Preconditions.checkNotNull(callback);

    CachedObject<T> cached = getCachedObject(cacheKey);
    Calendar lastModified = getLastModified(cached);

    try (TimestampedResponse fromServer = requestIfModifiedSince(address, lastModified)) {
      if (fromServer.response != null) {
        try (InputStream stream = fromServer.response.getEntity().getContent()) {
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

  protected <T> T requestCachedReader(String cacheKey, URI address,
                                      CacheDeserializer<Reader, T> callback)
      throws IOException {
    Preconditions.checkNotNull(address);
    Preconditions.checkNotNull(callback);

    CachedObject<T> cached = getCachedObject(cacheKey);
    Calendar lastModified = getLastModified(cached);

    try (TimestampedResponse fromServer = requestIfModifiedSince(address, lastModified)) {
      if (fromServer.response != null) {
        try (Reader reader = openReader(fromServer.response.getEntity())) {
          T value = callback.read(reader);
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

  private static <T> Calendar getLastModified(CachedObject<T> cached) {
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
      response = makeRequest(address, new BasicHeader("If-Modified-Since", HttpDateUtil.format(lastModified)));
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

  /**
   * Builds a complete URI given a URL that specifies the server and a string that is the remainder of the query.
   *
   * @param server  host, port, and optionally part of the path.  For example "http://www.example.com/" or
   *                "https://plos.org/api/".
   * @param address the remainder of the path and query string.  For example "articles/foo.pone.1234567?comments=true"
   * @return a URI to the complete path and query string
   */
  protected static URI buildUri(URL server, String address) {
    URI targetUri;
    try {
      targetUri = new URL(server, Preconditions.checkNotNull(address)).toURI();
    } catch (MalformedURLException | URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
    return targetUri;
  }

}
