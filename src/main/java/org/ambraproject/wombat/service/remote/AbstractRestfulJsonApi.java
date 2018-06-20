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

import com.google.common.collect.ImmutableList;
import org.ambraproject.wombat.util.CacheKey;
import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.util.function.Function;

/**
 * Base implementation for {@link RestfulJsonApi}. Details about the service address, caching, authentication headers,
 * etc., are injected by subclasses.
 */
abstract class AbstractRestfulJsonApi implements RestfulJsonApi {

  @Autowired
  protected JsonService jsonService;
  @Autowired
  protected CachedRemoteService<InputStream> cachedRemoteStreamer;
  @Autowired
  protected CachedRemoteService<Reader> cachedRemoteReader;

  /**
   * @return the base URL to which request addresses will be appended
   */
  protected abstract URL getServerUrl();

  /**
   * @return a string, which is constant and unique for each service, to identify cached values the service
   */
  protected abstract String getCachePrefix();

  /**
   * @return headers to add to every outgoing request to the service
   */
  protected Iterable<? extends Header> getAdditionalHeaders() {
    return ImmutableList.of();
  }

  @FunctionalInterface
  protected static interface RemoteRequest<T> {
    T execute() throws IOException;
  }

  /**
   * Execute a remote request that has been set up by another method. Every invocation by this class to {@link
   * #cachedRemoteStreamer} and {@link #cachedRemoteReader} is wrapped in a call to {@link #makeRemoteRequest}.
   * <p>
   * Subclasses may override this method to add special exception handling. Each override must make a {@code super}
   * call.
   */
  protected <T> T makeRemoteRequest(RemoteRequest<T> requestAction) throws IOException {
    return requestAction.execute();
  }

  @Override
  public final InputStream requestStream(ApiAddress address) throws IOException {
    return makeRemoteRequest(() -> cachedRemoteStreamer.request(buildGet(address)));
  }

  @Override
  public final Reader requestReader(ApiAddress address) throws IOException {
    return makeRemoteRequest(() -> cachedRemoteReader.request(buildGet(address)));
  }

  @Override
  public final <T> T requestObjectForType(ApiAddress address, Type responseType)
      throws IOException {
    CacheKey cacheKey = CacheKey.create(getCachePrefix(), address.getAddress());

    // Just try to cache everything. We may want to narrow this in the future.
    return requestCachedObject(cacheKey, address, responseType);
  }

  @Override
  public <T> T requestObject(ApiAddress address, Class<T> responseClass) throws IOException {
    return requestObjectForType(address, (Type) responseClass);
  }

  private <R extends HttpUriRequest & HttpEntityEnclosingRequest>
  HttpResponse uploadObject(ApiAddress address, Object object, Function<URI, R> requestConstructor)
      throws IOException {
    R request = buildRequest(address, requestConstructor);

    ContentType contentType = ContentType.APPLICATION_JSON;
    if (object != null) {
      String json = jsonService.serialize(object);
      request.setEntity(new StringEntity(json, contentType));
    }

    request.addHeader(HttpHeaders.CONTENT_TYPE, contentType.toString());

    try (CloseableHttpResponse response = cachedRemoteReader.getResponse(request)) {
      //return closed response
      return response;
    }
  }


  @Override
  public final HttpResponse postObject(ApiAddress address, Object object) throws IOException {
    return uploadObject(address, object, HttpPost::new);
  }

  @Override
  public final void putObject(ApiAddress address, Object object) throws IOException {
    uploadObject(address, object, HttpPut::new);
  }


  @Override
  public final void deleteObject(ApiAddress address) throws IOException {
    HttpDelete delete = buildRequest(address, HttpDelete::new);
    try (CloseableHttpResponse ignored = cachedRemoteReader.getResponse(delete)) {
      ignored.close();
    }
  }


  @Override
  public final CloseableHttpResponse getResponse(HttpUriRequest target) throws IOException {
    return makeRemoteRequest(() -> cachedRemoteReader.getResponse(target));
  }

  @Override
  public final <T> T requestCachedStream(CacheKey cacheKey, ApiAddress address,
                                         CacheDeserializer<InputStream, T> callback) throws IOException {
    return makeRemoteRequest(() -> cachedRemoteStreamer.requestCached(cacheKey, buildGet(address), callback));
  }

  @Override
  public final <T> T requestCachedReader(CacheKey cacheKey, ApiAddress address,
                                         CacheDeserializer<Reader, T> callback) throws IOException {
    return makeRemoteRequest(() -> cachedRemoteReader.requestCached(cacheKey, buildGet(address), callback));
  }

  @Override
  public final <T> T requestCachedObject(CacheKey cacheKey, ApiAddress address, Type responseType) throws IOException {
    return makeRemoteRequest(() ->
        (T) jsonService.requestCachedObject(cachedRemoteReader, cacheKey, buildGet(address), responseType));
  }

  @Override
  public final <T> T requestCachedObject(CacheKey cacheKey, ApiAddress address, Class<T> responseClass) throws IOException {
    return requestCachedObject(cacheKey, address, (Type) responseClass);
  }

  protected final HttpGet buildGet(ApiAddress address) {
    return buildRequest(address, HttpGet::new);
  }

  private <R extends HttpUriRequest> R buildRequest(ApiAddress address, Function<URI, R> requestConstructor) {
    URI uri = address.buildUri(this.getServerUrl());
    R request = requestConstructor.apply(uri);
    for (Header header : getAdditionalHeaders()) {
      request.addHeader(header);
    }
    return request;
  }

}
