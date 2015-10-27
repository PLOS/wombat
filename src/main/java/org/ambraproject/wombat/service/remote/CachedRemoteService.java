package org.ambraproject.wombat.service.remote;

import com.google.common.base.Preconditions;
import org.ambraproject.rhombat.HttpDateUtil;
import org.ambraproject.rhombat.cache.Cache;
import org.ambraproject.wombat.util.CacheParams;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
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
  public S request(HttpUriRequest target) throws IOException {
    return remoteService.request(target);
  }

  @Override // delegate
  public S open(HttpEntity entity) throws IOException {
    return remoteService.open(entity);
  }

  @Override // delegate
  public CloseableHttpResponse getResponse(HttpUriRequest target) throws IOException {
    return remoteService.getResponse(target);
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

  /**
   * Get a value either from the cache or by converting a stream from a REST request or from the cache.
   * <p>
   * If there is a cached value, and the REST service does not indicate that the value has been modified since the value
   * was inserted into the cache, return that value. Else, query the service for a new stream and convert that stream to
   * a cacheable return value using the provided callback.
   *
   * @param cacheParams the cache parameters object containing the cache key at which to retrieve and store the value
   * @param target      the request with which to query the service if the value is not cached
   * @param callback    how to deserialize a new value from the stream, to return and insert into the cache
   * @param <T>         the type of value to deserialize and return
   * @return the value from the service or cache
   * @throws IOException
   */
  public <T> T requestCached(CacheParams cacheParams, HttpUriRequest target, CacheDeserializer<? super S, ? extends T> callback)
      throws IOException {
    Preconditions.checkNotNull(target);
    Preconditions.checkNotNull(callback);
    Preconditions.checkNotNull(cacheParams);

    CachedObject<T> cached = getCachedObject(cacheParams.getCacheKey());
    Calendar lastModified = getLastModified(cached);

    try (TimestampedResponse fromServer = requestIfModifiedSince(target, lastModified)) {
      if (fromServer.response != null) {
        try (S stream = remoteService.open(fromServer.response.getEntity())) {
          T value = callback.read(stream);
          if (fromServer.timestamp != null) {
            if (cacheParams.getTimeToLive().isPresent()) {
              cache.put(cacheParams.getCacheKey(), new CachedObject<>(fromServer.timestamp, value), cacheParams.getTimeToLive().get());
            } else {
              cache.put(cacheParams.getCacheKey(), new CachedObject<>(fromServer.timestamp, value));
            }
          }
          return value;
        }
      } else {
        return cached.object;
      }
    }
  }

  /**
   * Query the cache for a value, with error-handling.
   *
   * @param cacheKey the cache key
   * @param <T>      the type of cached value
   * @return the cached value, wrapped with its timestamp
   */
  private <T> CachedObject<T> getCachedObject(String cacheKey) {
    try {
      return cache.get(Preconditions.checkNotNull(cacheKey));
    } catch (Exception e) {
      // Unexpected, but to degrade gracefully, treat it the same as a cache miss
      log.error("Error accessing cache using key: {}", cacheKey, e);
      return null;
    }
  }

  /**
   * Extract the timestamp from a cached object, or a default value.
   *
   * @param cached the cached object wrapper
   * @param <T>    the type of cached value
   * @return the timestamp
   */
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
   * @param target       the request to send the REST service
   * @param lastModified the object will be returned iff the SOA server indicates that it was modified after this
   *                     timestamp
   * @return a timestamped stream, or a null stream with non-null timestamp
   * @throws IOException
   */
  private TimestampedResponse requestIfModifiedSince(HttpUriRequest target, Calendar lastModified) throws IOException {
    Preconditions.checkNotNull(lastModified);
    CloseableHttpResponse response = null;
    boolean returningStream = false;
    try {
      target.addHeader(HttpHeaders.IF_MODIFIED_SINCE, HttpDateUtil.format(lastModified));
      response = remoteService.getResponse(target);
      Header[] lastModifiedHeaders = response.getHeaders(HttpHeaders.LAST_MODIFIED);
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
      if (statusCode == HttpStatus.OK.value()) {
        TimestampedResponse timestamped = new TimestampedResponse(resultLastModified, response);
        returningStream = true;
        return timestamped;
      } else if (statusCode == HttpStatus.NOT_MODIFIED.value()) {
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
