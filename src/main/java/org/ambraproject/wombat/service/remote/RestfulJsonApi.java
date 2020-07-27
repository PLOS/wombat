/*
 * Copyright (c) 2017 Public Library of Science
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package org.ambraproject.wombat.service.remote;

import org.ambraproject.wombat.util.CacheKey;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

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
  public abstract InputStream requestStream(ApiAddress address) throws IOException;

  public abstract Reader requestReader(ApiAddress address) throws IOException;

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
  public abstract <T> T requestObjectForType(ApiAddress address, Type responseType)
      throws IOException;

  public abstract <T> T requestObject(ApiAddress address, Class<T> responseClass) throws IOException;
  public abstract <T> T requestObject(ApiAddress address, Type t) throws IOException;

  /**
   * Serialize an object to JSON and POST the JSON to a service path.
   *
   * @param address the path to which to send the REST request
   * @param object  the object to serialize to product JSON
   * @return HttpResponse
   * @throws IOException
   */
  public abstract HttpResponse postObject(ApiAddress address, Object object) throws IOException;

  public abstract void putObject(ApiAddress address, Object object) throws IOException;

  public abstract void deleteObject(ApiAddress address) throws IOException;

  public abstract CloseableHttpResponse getResponse(HttpUriRequest target) throws IOException;

}
