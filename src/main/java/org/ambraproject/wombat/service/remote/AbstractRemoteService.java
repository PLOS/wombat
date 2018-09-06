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
import com.google.common.base.Strings;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;

/**
 * Superclass for service beans that make calls to remote APIs.
 */
abstract class AbstractRemoteService<S extends Closeable> implements RemoteService<S> {
  private static final Logger log = LoggerFactory.getLogger(AbstractRemoteService.class);

  private final Optional<HttpClientConnectionManager> connectionManager;

  HttpClientBuilder clientBuilder;
  CloseableHttpClient client;

  protected AbstractRemoteService(HttpClientConnectionManager connectionManager) {
    this.connectionManager = Optional.ofNullable(connectionManager);
    CacheConfig cacheConfig = CacheConfig.
      custom().
      setMaxObjectSize(100000000)
      .build();

    this.clientBuilder = CachingHttpClientBuilder.create()
      .setCacheDir(new File("/tmp/wombat-cache"))
      .setCacheConfig(cacheConfig);
    this.connectionManager.map((mgr)->clientBuilder.setConnectionManager(mgr));
  }

  private CloseableHttpClient createClient() {
    if (client == null) {
      client = clientBuilder.build();
    }
    return client;
  }

  @Override
  public CloseableHttpResponse getResponse(HttpUriRequest target) throws IOException {
    // Don't close the client, as this shuts down the connection pool. Do close every response or its entity stream.
    CloseableHttpClient client = createClient();

    // We want to return an unclosed response, so close the response only if we throw an exception.
    boolean returningResponse = false;
    CloseableHttpResponse response = null;
    try {
      try {
        response = client.execute(target);
      } catch (HttpHostConnectException e) {
        throw new ServiceConnectionException(e);
      }

      StatusLine statusLine = response.getStatusLine();
      int statusCode = statusLine.getStatusCode();
      if (isErrorStatus(statusCode)) {
        URI targetUri = target.getURI();
        String address = targetUri.getPath();
        if (!Strings.isNullOrEmpty(targetUri.getQuery())) {
          address += "?" + targetUri.getQuery();
        }
        if (statusCode == HttpStatus.NOT_FOUND.value()) {
          throw new EntityNotFoundException(address);
        } else {
          String responseBody = IOUtils.toString(response.getEntity().getContent());
          String message = String.format("Request to \"%s\" failed (%d): %s.",
              address, statusCode, statusLine.getReasonPhrase());
          throw new ServiceRequestException(statusCode, message, responseBody);
        }
      }
      returningResponse = true;
      return response;
    } finally {
      if (!returningResponse && response != null) {
        response.close();
      }
    }
  }

  private static boolean isErrorStatus(int statusCode) {
    HttpStatus.Series series = HttpStatus.Series.valueOf(statusCode);
    return (series == HttpStatus.Series.CLIENT_ERROR) || (series == HttpStatus.Series.SERVER_ERROR);
  }

  @Override
  public S request(HttpUriRequest target) throws IOException {
    return open(requestEntity(target));
  }

  /**
   * Get a response and open the response entity.
   *
   * @param target the request to send
   * @return the response entity
   * @throws IOException      on making the request
   * @throws RuntimeException if a response is received but it has no response entity object
   * @see org.apache.http.HttpResponse#getEntity()
   */
  private HttpEntity requestEntity(HttpUriRequest target) throws IOException {
    Preconditions.checkNotNull(target);

    // We must leave the response open if we return a valid stream, but must close it otherwise.
    boolean returningResponse = false;
    CloseableHttpResponse response = null;
    try {
      response = getResponse(target);
      HttpEntity entity = response.getEntity();
      if (entity == null) {
        throw new RuntimeException("No response");
      }
      returningResponse = true;
      return entity;
    } finally {
      if (!returningResponse && response != null) {
        response.close();
      }
    }
  }

}
