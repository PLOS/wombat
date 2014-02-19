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

package org.ambraproject.wombat.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.io.Closer;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.ambraproject.wombat.util.TrustingHttpClient;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Abstract class for services that query a ReST backend that returns JSON.
 */
public abstract class JsonService {

  @Autowired
  private RuntimeConfiguration runtimeConfiguration;

  @Autowired
  protected Gson gson;

  /**
   * Send a ReST request and open a stream as the response.
   *
   * @param targetUri the URI to which to send the REST request
   * @return a stream to the response
   * @throws IOException             if there is an error connecting to the server
   * @throws NullPointerException    if the address is null
   * @throws EntityNotFoundException if the object at the address does not exist
   */
  protected InputStream requestStream(URI targetUri) throws IOException {
    HttpResponse response = makeRequest(targetUri);
    HttpEntity entity = response.getEntity();
    if (entity == null) {
      throw new RuntimeException("No response");
    }
    return entity.getContent();
  }

  /**
   * Makes a request to the given URI, checks for response codes 400 or above, and returns the response.
   *
   * @param targetUri the URI to which to send the request
   * @param headers   headers to add to the request, if any
   * @return response from the server
   * @throws IOException             if there is an error connecting to the server
   * @throws NullPointerException    if the address is null
   * @throws EntityNotFoundException if the object at the address does not exist
   */
  protected HttpResponse makeRequest(URI targetUri, Header... headers) throws IOException {
    HttpClient client = (runtimeConfiguration.trustUnsignedServer() && "https".equals(targetUri.getScheme()))
        ? TrustingHttpClient.create() : new DefaultHttpClient();
    HttpGet get = new HttpGet(targetUri);
    for (Header header : headers) {
      get.setHeader(header);
    }
    HttpResponse response = client.execute(get);
    StatusLine statusLine = response.getStatusLine();
    if (statusLine.getStatusCode() >= 400) {
      String address = targetUri.getPath();
      if (!Strings.isNullOrEmpty(targetUri.getQuery())) {
        address += "?" + targetUri.getQuery();
      }
      if (statusLine.getStatusCode() == 404) {
        throw new EntityNotFoundException(address);
      } else {
        String message = String.format("Request to \"%s\" failed (%d): %s",
            address, statusLine.getStatusCode(), statusLine.getReasonPhrase());
        throw new RuntimeException(message);
      }
    }
    return response;
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
  protected <T> T requestObject(URI uri, Class<T> responseClass) throws IOException {
    Preconditions.checkNotNull(responseClass);
    Closer closer = Closer.create();
    try {
      InputStream stream = closer.register(new BufferedInputStream(requestStream(uri)));
      Reader reader = closer.register(new InputStreamReader(stream));
      try {
        return gson.fromJson(reader, responseClass);
      } catch (JsonSyntaxException e) {
        String message = String.format("Could not deserialize %s from stream at: %s", responseClass.getName(), uri);
        throw new RuntimeException(message, e);
      }
    } catch (Throwable t) {
      throw closer.rethrow(t);
    } finally {
      closer.close();
    }
  }

  /**
   * Builds a complete URI given a URL that specifies the server and a string that is the remainder of the query.
   *
   * @param server  host, port, and optionally part of the path.  For example "http://www.example.com/" or
   *                "https://plos.org/api/".
   * @param address the remainder of the path and query string.  For example "articles/foo.pone.1234567?comments=true"
   * @return a URI to the complete path and query string
   */
  protected URI buildUri(URL server, String address) {
    URI targetUri;
    try {
      targetUri = new URL(server, Preconditions.checkNotNull(address)).toURI();
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException(e);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
    return targetUri;
  }
}
