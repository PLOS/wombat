package org.ambraproject.wombat.service;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.IOException;
import java.io.InputStream;

/**
 * A service for retrieving data from the SOA's RESTful server.
 * <p/>
 * Each {@code address} argument for this service's methods must be formatted as a path to be appended to the root
 * server URL. For example, if the server is hosted at {@code http://example.com/api/v1/} and you want to send a request
 * to {@code http://example.com/api/v1/config}, then you would call {@code requestStream("config")}.
 */
public interface SoaService {

  /**
   * Send a REST request and open a stream as the response.
   *
   * @param address the path to which to send the REST request
   * @return a stream to the response
   * @throws IOException             if there is an error connecting to the server
   * @throws NullPointerException    if the address is null
   * @throws EntityNotFoundException if the object at the address does not exist
   */
  public abstract InputStream requestStream(String address) throws IOException;

  /**
   * Send a REST request and serialize the response to an object. The serialization is controlled by the {@link
   * org.ambraproject.wombat.config.SpringConfiguration#gson()} bean.
   *
   * @param address       the path to which to send the REST request
   * @param responseClass the object type into which to serialize the JSON response
   * @param <T>           the type of {@code responseClass}
   * @return the response, serialized from JSON into an object
   * @throws IOException             if there is an error connecting to the server
   * @throws NullPointerException    if either argument is null
   * @throws EntityNotFoundException if the object at the address does not exist
   */
  public abstract <T> T requestObject(String address, Class<T> responseClass) throws IOException;

  public static interface CacheCallback<T> {
    public abstract T call(InputStream stream) throws IOException;
  }

  /**
   * Get a stream either through a REST request or from the cache. If there is a cached value, and the REST service does
   * not indicate that the value has been modified since the value was inserted into the cache, return that value. Else,
   * query the service for a new stream and convert that stream to a cacheable return value using the provided
   * callback.
   *
   * @param cacheKey the cache key at which to retrieve and store the value
   * @param address  the address to query the SOA service if the value is not cached
   * @param callback how to deserialize a new value from the stream, to return and insert into the cache
   * @param <T>      the type of value to deserialize and return
   * @return the value from the service or cache
   * @throws IOException
   */
  public abstract <T> T requestCachedStream(String cacheKey, String address, CacheCallback<? extends T> callback) throws IOException;

  /**
   * Serialize an object either through a REST request or from the cache. If there is a cached value, and the REST
   * service does not indicate that the value has been modified since the value was inserted into the cache, return that
   * value. Else, query the service for JSON and deserialize it to an object as usual.
   *
   * @param cacheKey      the cache key at which to retrieve and store the value
   * @param address       the address to query the SOA service if the value is not cached
   * @param responseClass the type of object to deserialize
   * @param <T>           the type of {@code responseClass}
   * @return the deserialized object
   * @throws IOException
   */
  public abstract <T> T requestCachedObject(String cacheKey, String address, Class<T> responseClass) throws IOException;

  /**
   * Requests an asset, returning both the headers and stream.
   * <p/>
   * The caller <em>must</em> either close the returned {@code CloseableHttpResponse} object or close the {@code
   * InputStream} to the response body. This is very important, because leaving responses hanging open can starve the
   * connection pool and cause horrible timeouts.
   *
   * @param assetId the asset ID within the SOA service's "assetfiles/" namespace
   * @return
   * @throws IOException
   */
  public abstract CloseableHttpResponse requestAsset(String assetId, Header... headers) throws IOException;

}
