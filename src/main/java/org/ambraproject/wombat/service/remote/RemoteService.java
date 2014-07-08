package org.ambraproject.wombat.service.remote;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

import java.io.Closeable;
import java.io.IOException;

/**
 * @param <S> the "stream" type opened by this object, typically {@link java.io.InputStream} or {@link java.io.Reader}
 */
public interface RemoteService<S extends Closeable> {

  /**
   * Make a request and open a stream as the response.
   * <p/>
   * The caller must close the stream when it is done reading. This is important for connection pooling.
   *
   * @param target the request to send
   * @return a stream to the response
   * @throws IOException                                             if there is an error connecting to the server
   * @throws NullPointerException                                    if the address is null
   * @throws org.ambraproject.wombat.service.EntityNotFoundException if the object at the address does not exist
   */
  public abstract S request(HttpUriRequest target) throws IOException;

  /*
   * The method {@link #request(URI)} is the primary public method of this interface. The following two methods expose
   * some internals to solve a few special cases, and generally should not be called outside of this package.
   */

  /**
   * Open a stream object of the appropriate type from a response entity.
   *
   * @param entity the response entity
   * @return the stream to the response content
   * @throws IOException
   */
  public abstract S open(HttpEntity entity) throws IOException;

  /**
   * Makes a request to the given URI, checks for response codes 400 or above, and returns the response.
   * <p/>
   * The caller <em>must</em> either close the returned {@code CloseableHttpResponse} object or close the {@code
   * InputStream} to the response body (i.e., {@code CloseableHttpResponse.getEntity().getContent()}). This is very
   * important, because leaving responses hanging open can starve the connection pool and cause horrible timeouts.
   *
   * @param target the request to send
   * @return response from the server
   * @throws IOException                                             if there is an error connecting to the server
   * @throws NullPointerException                                    if the address is null
   * @throws org.ambraproject.wombat.service.EntityNotFoundException if the object at the address does not exist
   */
  public abstract CloseableHttpResponse getResponse(HttpUriRequest target) throws IOException;

}
