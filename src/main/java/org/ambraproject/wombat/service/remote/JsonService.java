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

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.util.CacheKey;
import org.apache.http.client.methods.HttpUriRequest;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;

/**
 * Abstract class for services that query a ReST backend that returns JSON.
 */
public class JsonService {

  @Autowired
  protected Gson gson;

  public String serialize(Object object) {
    return gson.toJson(object);
  }

  /**
   * Send a ReST request and serialize the response to an object. The serialization is controlled by the {@link
   * org.ambraproject.wombat.config.RootConfiguration#gson()} bean.
   *
   * @param target       the REST request to make
   * @param responseType the object type into which to serialize the JSON response
   * @param <T>          the type of {@code responseClass}
   * @return the response, serialized from JSON into an object
   * @throws IOException             if there is an error connecting to the server
   * @throws NullPointerException    if either argument is null
   * @throws EntityNotFoundException if the object at the address does not exist
   */
  public <T> T requestObject(RemoteService<? extends Reader> remoteService, HttpUriRequest target, Type responseType) throws IOException {
    Preconditions.checkNotNull(responseType);
    try (Reader reader = remoteService.request(target)) {
      return deserializeStream(responseType, reader, target.getURI());
    }
  }

  public <T> T requestObject(RemoteService<? extends Reader> remoteService, HttpUriRequest target, Class<T> responseClass) throws IOException {
    return requestObject(remoteService, target, (Type) responseClass);
  }

  /**
   * Serialize the content of a stream to an object.
   *
   * @param responseType the object type into which to serialize the JSON code
   * @param reader       the source of the JSON code
   * @param source       an object whose {@code toString()} describes where the JSON code came from (for logging only)
   * @return the deserialized object
   * @throws IOException
   */
  private <T> T deserializeStream(Type responseType, Reader reader, Object source) throws IOException {
    T value;
    try {
      value = gson.fromJson(reader, responseType);
    } catch (JsonSyntaxException e) {
      String message = String.format("Could not deserialize %s from stream at: %s. Message: %s",
          responseType.getTypeName(), source, e.getMessage());
      throw new ServiceResponseFormatException(message, e);
    }
    if (value == null) {
      String message = String.format("Could not deserialize %s from null/empty stream at: %s",
          responseType.getTypeName(), source);
      throw new ServiceResponseFormatException(message);
    }
    return value;
  }

}
