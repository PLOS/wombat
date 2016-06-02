package org.ambraproject.wombat.service.remote;

import org.ambraproject.wombat.util.CacheKey;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;

public interface RestfulJsonApi {

  /**
   * Send a REST request and open a stream as the response.
   *
   * @param address the path to which to send the REST request
   * @return a stream to the response
   * @throws java.io.IOException                                     if there is an error connecting to the server
   * @throws NullPointerException                                    if the address is null
   * @throws org.ambraproject.wombat.service.EntityNotFoundException if the object at the address does not exist
   */
  public abstract InputStream requestStream(String address) throws IOException;

  public abstract InputStream requestStream(HttpUriRequest target) throws IOException;

  public abstract Reader requestReader(String address) throws IOException;

  public abstract Reader requestReader(HttpUriRequest target) throws IOException;

  /**
   * Send a REST request and serialize the response to an object. The serialization is controlled by the {@link
   * org.ambraproject.wombat.config.RootConfiguration#gson()} bean.
   *
   * @param address      the path to which to send the REST request
   * @param responseType the object type into which to serialize the JSON response
   * @param <T>          the type of {@code responseClass}
   * @return the response, serialized from JSON into an object
   * @throws IOException                                             if there is an error connecting to the server
   * @throws NullPointerException                                    if either argument is null
   * @throws org.ambraproject.wombat.service.EntityNotFoundException if the object at the address does not exist
   */
  public abstract <T> T requestObject(String address, Type responseType) throws IOException;

  public abstract <T> T requestObject(String address, Class<T> responseClass) throws IOException;

  /**
   * Serialize an object to JSON and POST the JSON to a service path.
   *
   * @param address the path to which to send the REST request
   * @param object  the object to serialize to product JSON
   * @throws IOException
   */
  public abstract void postObject(String address, Object object) throws IOException;

  public abstract void putObject(String address, Object object) throws IOException;

  public abstract void deleteObject(String address) throws IOException;

  /**
   * Get a stream either through a REST request or from the cache. If there is a cached value, and the REST service does
   * not indicate that the value has been modified since the value was inserted into the cache, return that value. Else,
   * query the service for a new stream and convert that stream to a cacheable return value using the provided
   * callback.
   *
   * @param cacheKey the cache parameters object containing the cache key at which to retrieve and store the value
   * @param address     the address to query the SOA service if the value is not cached
   * @param callback    how to deserialize a new value from the stream, to return and insert into the cache
   * @param <T>         the type of value to deserialize and return
   * @return the value from the service or cache
   * @throws IOException
   */
  public abstract <T> T requestCachedStream(CacheKey cacheKey, String address,
                                            CacheDeserializer<InputStream, T> callback) throws IOException;

  public abstract <T> T requestCachedReader(CacheKey cacheKey, String address,
                                            CacheDeserializer<Reader, T> callback) throws IOException;

  /**
   * Serialize an object either through a REST request or from the cache. If there is a cached value, and the REST
   * service does not indicate that the value has been modified since the value was inserted into the cache, return that
   * value. Else, query the service for JSON and deserialize it to an object as usual.
   *
   * @param cacheKey  the cache parameters object containing the cache key at which to retrieve and store the value
   * @param address      the address to query the SOA service if the value is not cached
   * @param responseType the type of object to deserialize
   * @param <T>          the type of {@code responseClass}
   * @return the deserialized object
   * @throws IOException
   */
  public abstract <T> T requestCachedObject(CacheKey cacheKey, String address, Type responseType) throws IOException;

  public abstract <T> T requestCachedObject(CacheKey cacheKey, String address, Class<T> responseClass) throws IOException;

  public abstract CloseableHttpResponse getResponse(HttpUriRequest target) throws IOException;

  /**
   * Forward the remote service response from a given request to a client
   *
   * @return
   * @throws IOException
   */
  public abstract void forwardResponse(HttpUriRequest requestToService,
                                       HttpServletResponse responseToClient) throws IOException;

}
