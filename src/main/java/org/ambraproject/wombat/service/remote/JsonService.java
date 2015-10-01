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
import org.ambraproject.wombat.util.CacheParams;
import org.apache.http.client.methods.HttpGet;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;

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
   * org.ambraproject.wombat.config.SpringConfiguration#gson()} bean.
   *
   * @param uri           the URI to which to send the REST request
   * @param responseClass the object type into which to serialize the JSON response
   * @param <T>           the type of {@code responseClass}
   * @return the response, serialized from JSON into an object
   * @throws IOException             if there is an error connecting to the server
   * @throws NullPointerException    if either argument is null
   * @throws EntityNotFoundException if the object at the address does not exist
   */
  public <T> T requestObject(RemoteService<? extends Reader> remoteService, URI uri, Class<T> responseClass) throws IOException {
    Preconditions.checkNotNull(responseClass);
    try (Reader reader = remoteService.request(new HttpGet(uri))) {
      return deserializeStream(responseClass, reader, uri);
    }
  }

  /**
   * Serialize an object either through a REST request or from the cache. If there is a cached value, and the REST
   * service does not indicate that the value has been modified since the value was inserted into the cache, return that
   * value. Else, query the service for JSON and deserialize it to an object as usual.
   *
   * @param cacheParams   the cache parameters object containing the cache key at which to retrieve and store the value
   * @param address       the address to query the SOA service if the value is not cached
   * @param responseClass the type of object to deserialize
   * @param <T>           the type of {@code responseClass}
   * @return the deserialized object
   * @throws IOException
   */
  public <T> T requestCachedObject(CachedRemoteService<? extends Reader> remoteService, CacheParams cacheParams,
                                   final URI address, final Class<T> responseClass)
      throws IOException {
    Preconditions.checkNotNull(responseClass);
    return remoteService.requestCached(cacheParams, new HttpGet(address),
        new CacheDeserializer<Reader, T>() {
          @Override
          public T read(Reader reader) throws IOException {
            return deserializeStream(responseClass, reader, address);
          }
        }
    );
  }

  /**
   * Serialize the content of a stream to an object.
   *
   * @param responseClass the object type into which to serialize the JSON code
   * @param reader        the source of the JSON code
   * @param source        an object whose {@code toString()} describes where the JSON code came from (for logging only)
   * @return the deserialized object
   * @throws IOException
   */
  private <T> T deserializeStream(Class<T> responseClass, Reader reader, Object source) throws IOException {
    T value;
    try {
      value = gson.fromJson(reader, responseClass);
    } catch (JsonSyntaxException e) {
      String message = String.format("Could not deserialize %s from stream at: %s. Message: %s",
          responseClass.getName(), source, e.getMessage());
      throw new RuntimeException(message, e);
    }
    if (value == null) {
      String message = String.format("Could not deserialize %s from null/empty stream at: %s",
          responseClass.getName(), source);
      throw new RuntimeException(message);
    }
    return value;
  }

}
