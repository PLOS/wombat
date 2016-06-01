/*
 * Copyright (c) 2006-2014 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
   * Serialize an object either through a REST request or from the cache. If there is a cached value, and the REST
   * service does not indicate that the value has been modified since the value was inserted into the cache, return that
   * value. Else, query the service for JSON and deserialize it to an object as usual.
   *
   * @param cacheKey  the cache parameters object containing the cache key at which to retrieve and store the value
   * @param target       the request to make to the SOA service if the value is not cached
   * @param responseType the type of object to deserialize
   * @param <T>          the type of {@code responseClass}
   * @return the deserialized object
   * @throws IOException
   */
  public <T> T requestCachedObject(CachedRemoteService<? extends Reader> remoteService, CacheKey cacheKey,
                                   HttpUriRequest target, final Type responseType)
      throws IOException {
    Preconditions.checkNotNull(responseType);
    return remoteService.requestCached(cacheKey, target,
        (Reader reader) -> deserializeStream(responseType, reader, target.getURI()));
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
