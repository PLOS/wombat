package org.ambraproject.wombat.service;

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
   * Send a REST request and dump the response to a string.
   *
   * @param address the path to which to send the REST request
   * @return the response as a string
   * @throws IOException          if there is an error connecting to the server
   * @throws NullPointerException if the address is null
   */
  public abstract String requestString(String address) throws IOException;

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

}
