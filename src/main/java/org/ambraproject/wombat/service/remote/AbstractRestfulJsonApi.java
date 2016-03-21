package org.ambraproject.wombat.service.remote;

import com.google.common.collect.ImmutableList;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.util.CacheParams;
import org.ambraproject.wombat.util.HttpMessageUtil;
import org.ambraproject.wombat.util.UriUtil;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.util.function.Function;

/**
 * Base implementation for {@link RestfulJsonApi}. Details about the service address, caching, authentication
 * headers, etc., are injected by subclasses.
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

  @Override
  public final InputStream requestStream(String address) throws IOException {
    return cachedRemoteStreamer.request(buildGet(address));
  }

  @Override
  public final Reader requestReader(String address) throws IOException {
    return cachedRemoteReader.request(buildGet(address));
  }

  @Override
  public final InputStream requestStream(HttpUriRequest target) throws IOException {
    return cachedRemoteStreamer.request(target);
  }

  @Override
  public final Reader requestReader(HttpUriRequest target) throws IOException {
    return cachedRemoteReader.request(target);
  }

  @Override
  public final <T> T requestObject(String address, Class<T> responseClass) throws IOException {
    String keyHash = CacheParams.createKeyHash(address);
    String cacheKey = getCachePrefix() + ":" + keyHash;

    // Just try to cache everything. We may want to narrow this in the future.
    return requestCachedObject(CacheParams.create(cacheKey), address, responseClass);
  }

  @Override
  public final void postObject(String address, Object object) throws IOException {
    String json = jsonService.serialize(object);
    HttpPost post = buildRequest(address, HttpPost::new);
    try {
      post.setEntity(new StringEntity(json));
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    post.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

    try (CloseableHttpResponse ignored = cachedRemoteReader.getResponse(post)) {
    }
  }


  @Override
  public final void putObject(String address, Object object) throws IOException {
    String json = jsonService.serialize(object);
    HttpPut put = buildRequest(address, HttpPut::new);
    try {
      put.setEntity(new StringEntity(json));
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    put.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

    try (CloseableHttpResponse ignored = cachedRemoteReader.getResponse(put)) {
    }
  }


  @Override
  public final void deleteObject(String address) throws IOException {
    HttpDelete delete = buildRequest(address, HttpDelete::new);

    try (CloseableHttpResponse ignored = cachedRemoteReader.getResponse(delete)) {
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
    return cachedRemoteReader.getResponse(target);
  }

  @Override
  public final <T> T requestCachedStream(CacheParams cacheParams, String address,
                                         CacheDeserializer<InputStream, T> callback) throws IOException {
    return cachedRemoteStreamer.requestCached(cacheParams, buildGet(address), callback);
  }

  @Override
  public final <T> T requestCachedReader(CacheParams cacheParams, String address,
                                         CacheDeserializer<Reader, T> callback) throws IOException {
    return cachedRemoteReader.requestCached(cacheParams, buildGet(address), callback);
  }

  @Override
  public final <T> T requestCachedObject(CacheParams cacheParams, String address, Class<T> responseClass) throws IOException {
    return jsonService.requestCachedObject(cachedRemoteReader, cacheParams, buildGet(address), responseClass);
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
