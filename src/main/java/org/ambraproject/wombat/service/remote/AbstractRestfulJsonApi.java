package org.ambraproject.wombat.service.remote;

import com.google.common.collect.ImmutableList;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.util.CacheKey;
import org.ambraproject.wombat.util.HttpMessageUtil;
import org.ambraproject.wombat.util.UriUtil;
import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
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
  public final InputStream requestStream(String address) throws IOException {
    return makeRemoteRequest(() -> cachedRemoteStreamer.request(buildGet(address)));
  }

  @Override
  public final Reader requestReader(String address) throws IOException {
    return makeRemoteRequest(() -> cachedRemoteReader.request(buildGet(address)));
  }

  @Override
  public final InputStream requestStream(HttpUriRequest target) throws IOException {
    return makeRemoteRequest(() -> cachedRemoteStreamer.request(target));
  }

  @Override
  public final Reader requestReader(HttpUriRequest target) throws IOException {
    return makeRemoteRequest(() -> cachedRemoteReader.request(target));
  }

  @Override
  public final <T> T requestObject(String address, Type responseType) throws IOException {
    CacheKey cacheKey = CacheKey.create(getCachePrefix(), address);

    // Just try to cache everything. We may want to narrow this in the future.
    return requestCachedObject(cacheKey, address, responseType);
  }

  @Override
  public final <T> T requestObject(String address, Class<T> responseClass) throws IOException {
    return requestObject(address, (Type) responseClass);
  }


  private static final String APPLICATION_JSON_CONTENT_TYPE = ContentType.APPLICATION_JSON.toString();

  private <R extends HttpUriRequest & HttpEntityEnclosingRequest>
  void uploadObject(String address, Object object, Function<URI, R> requestConstructor)
      throws IOException {
    String json = jsonService.serialize(object);
    R request = buildRequest(address, requestConstructor);
    try {
      request.setEntity(new StringEntity(json));
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    request.addHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_CONTENT_TYPE);

    try (CloseableHttpResponse ignored = cachedRemoteReader.getResponse(request)) {
      ignored.close();
    }
  }


  @Override
  public final void postObject(String address, Object object) throws IOException {
    uploadObject(address, object, HttpPost::new);
  }

  @Override
  public final void putObject(String address, Object object) throws IOException {
    uploadObject(address, object, HttpPut::new);
  }


  @Override
  public final void deleteObject(String address) throws IOException {
    HttpDelete delete = buildRequest(address, HttpDelete::new);
    try (CloseableHttpResponse ignored = cachedRemoteReader.getResponse(delete)) {
      ignored.close();
    }
  }


  @Override
  public final void forwardResponse(HttpUriRequest requestToService, HttpServletResponse responseToClient) throws IOException {
    try (CloseableHttpResponse responseFromService = this.getResponse(requestToService)) {
      HttpMessageUtil.copyResponse(responseFromService, responseToClient);
    } catch (EntityNotFoundException e) {
      responseToClient.setStatus(HttpServletResponse.SC_NOT_FOUND);
    } catch (Exception e) {
      responseToClient.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public final CloseableHttpResponse getResponse(HttpUriRequest target) throws IOException {
    return makeRemoteRequest(() -> cachedRemoteReader.getResponse(target));
  }

  @Override
  public final <T> T requestCachedStream(CacheKey cacheKey, String address,
                                         CacheDeserializer<InputStream, T> callback) throws IOException {
    return makeRemoteRequest(() -> cachedRemoteStreamer.requestCached(cacheKey, buildGet(address), callback));
  }

  @Override
  public final <T> T requestCachedReader(CacheKey cacheKey, String address,
                                         CacheDeserializer<Reader, T> callback) throws IOException {
    return makeRemoteRequest(() -> cachedRemoteReader.requestCached(cacheKey, buildGet(address), callback));
  }

  @Override
  public final <T> T requestCachedObject(CacheKey cacheKey, String address, Type responseType) throws IOException {
    return makeRemoteRequest(() ->
        jsonService.requestCachedObject(cachedRemoteReader, cacheKey, buildGet(address), responseType));
  }

  @Override
  public final <T> T requestCachedObject(CacheKey cacheKey, String address, Class<T> responseClass) throws IOException {
    return requestCachedObject(cacheKey, address, (Type) responseClass);
  }

  protected final HttpGet buildGet(String address) {
    return buildRequest(address, HttpGet::new);
  }

  private <R extends HttpUriRequest> R buildRequest(String address, Function<URI, R> requestConstructor) {
    URI uri = UriUtil.concatenate(this.getServerUrl(), address);
    R request = requestConstructor.apply(uri);
    for (Header header : getAdditionalHeaders()) {
      request.addHeader(header);
    }
    return request;
  }

}
