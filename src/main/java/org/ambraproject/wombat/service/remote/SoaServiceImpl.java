package org.ambraproject.wombat.service.remote;

import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.util.CacheParams;
import org.ambraproject.wombat.util.HttpMessageUtil;
import org.ambraproject.wombat.util.UriUtil;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
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
import java.util.Collection;

public class SoaServiceImpl implements SoaService {

  @Autowired
  private RuntimeConfiguration runtimeConfiguration;
  @Autowired
  private JsonService jsonService;
  @Autowired
  private CachedRemoteService<InputStream> cachedRemoteStreamer;
  @Autowired
  private CachedRemoteService<Reader> cachedRemoteReader;

  @Override
  public URL getServerUrl() {
    return runtimeConfiguration.getServer();
  }

  @Override
  public InputStream requestStream(SoaRequest request) throws IOException {
    return cachedRemoteStreamer.request(buildGet(request));
  }

  @Override
  public Reader requestReader(SoaRequest request) throws IOException {
    return cachedRemoteReader.request(buildGet(request));
  }

  private InputStream requestStream(HttpUriRequest target) throws IOException {
    return cachedRemoteStreamer.request(target);
  }

  private Reader requestReader(HttpUriRequest target) throws IOException {
    return cachedRemoteReader.request(target);
  }

  @Override
  public <T> T requestObject(SoaRequest request, Class<T> responseClass) throws IOException {
    // Just try to cache everything. We may want to narrow this in the future.
    String keyHash = CacheParams.createKeyHash(request.toString());
    return requestCachedObject(CacheParams.create("obj:" + keyHash), request, responseClass);
  }

  @Override
  public void postObject(SoaRequest request, Object object) throws IOException {
    String json = jsonService.serialize(object);
    HttpPost post = new HttpPost(request.buildUri(this));
    try {
      post.setEntity(new StringEntity(json));
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }

    try (CloseableHttpResponse ignored = cachedRemoteReader.getResponse(post)) {
    }
  }

  @Override
  public void forwardResponse(HttpUriRequest requestToService, HttpServletResponse responseToClient) throws IOException {
      try (CloseableHttpResponse responseFromService = this.getResponse(requestToService)) {
        HttpMessageUtil.copyResponse(responseFromService, responseToClient);
      } catch (EntityNotFoundException e) {
        responseToClient.setStatus(HttpServletResponse.SC_NOT_FOUND);
      } catch (Exception e) {
        responseToClient.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
  }

  @Override
  public CloseableHttpResponse getResponse(HttpUriRequest target) throws IOException {
    return cachedRemoteReader.getResponse(target);
  }

  @Override
  public <T> T requestCachedStream(CacheParams cacheParams, SoaRequest request,
                                   CacheDeserializer<InputStream, T> callback) throws IOException {
    return cachedRemoteStreamer.requestCached(cacheParams, buildGet(request), callback);
  }

  @Override
  public <T> T requestCachedReader(CacheParams cacheParams, SoaRequest request,
                                   CacheDeserializer<Reader, T> callback) throws IOException {
    return cachedRemoteReader.requestCached(cacheParams, buildGet(request), callback);
  }

  @Override
  public <T> T requestCachedObject(CacheParams cacheParams, SoaRequest request, Class<T> responseClass) throws IOException {
    return jsonService.requestCachedObject(cachedRemoteReader, cacheParams, request.buildUri(this), responseClass);
  }

  @Override
  public CloseableHttpResponse requestAsset(String assetId, Collection<? extends Header> headers)
      throws IOException {
    HttpGet get = buildGet(SoaRequest.request("assetfiles/" + assetId).build());
    get.setHeaders(headers.toArray(new Header[headers.size()]));
    return cachedRemoteStreamer.getResponse(get);
  }

  private HttpGet buildGet(SoaRequest request) {
    return new HttpGet(request.buildUri(this));
  }

}
