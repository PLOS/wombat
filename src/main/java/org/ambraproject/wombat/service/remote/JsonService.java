/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2013 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
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
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;

/**
 * Abstract class for services that query a ReST backend that returns JSON.
 */
abstract class JsonService extends RemoteService {

  @Autowired
  protected Gson gson;

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
  protected <T> T requestObject(URI uri, Class<T> responseClass) throws IOException {
    Preconditions.checkNotNull(responseClass);
    try (InputStream stream = requestStream(uri)) {
      return deserializeStream(responseClass, stream, uri);
    }
  }

  protected <T> T requestCachedObject(String cacheKey, final URI address, final Class<T> responseClass) throws IOException {
    Preconditions.checkNotNull(responseClass);
    return requestCachedStream(cacheKey, address, new CacheDeserializer<T>() {
      @Override
      public T call(InputStream stream) throws IOException {
        return deserializeStream(responseClass, stream, address);
      }
    });
  }

  /**
   * Serialize the content of a stream to an object.
   *
   * @param responseClass the object type into which to serialize the JSON code
   * @param stream        the source of the JSON code
   * @param source        an object whose {@code toString()} describes where the JSON code came from (for logging only)
   * @param <T>           the type of {@code responseClass}
   * @return the deserialized object
   * @throws IOException
   */
  private <T> T deserializeStream(Class<T> responseClass, InputStream stream, Object source) throws IOException {
    try (Reader reader = new InputStreamReader(stream)) {
      return gson.fromJson(reader, responseClass);
    } catch (JsonSyntaxException e) {
      String message = String.format("Could not deserialize %s from stream at: %s", responseClass.getName(), source);
      throw new RuntimeException(message, e);
    }
  }

}
