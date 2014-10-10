package org.ambraproject.wombat.service.remote;

import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.ambraproject.wombat.util.CacheParams;
import org.ambraproject.wombat.util.UriUtil;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;

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
  public InputStream requestStream(String address) throws IOException {
    return cachedRemoteStreamer.request(buildGet(address));
  }

  @Override
  public Reader requestReader(String address) throws IOException {
    return cachedRemoteReader.request(buildGet(address));
  }

  @Override
  public InputStream requestStream(HttpUriRequest target) throws IOException {
    return cachedRemoteStreamer.request(target);
  }

  @Override
  public Reader requestReader(HttpUriRequest target) throws IOException {
    return cachedRemoteReader.request(target);
  }

  @Override
  public <T> T requestObject(String address, Class<T> responseClass) throws IOException {
    // Just try to cache everything. We may want to narrow this in the future.
    return requestCachedObject(CacheParams.create("obj:" + address), address, responseClass);
  }

  @Override
  public void postObject(String address, Object object) throws IOException {
    String json = jsonService.serialize(object);
    HttpPost post = new HttpPost(buildUri(address));
    try {
      post.setEntity(new StringEntity(json));
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }

    try (CloseableHttpResponse ignored = cachedRemoteReader.getResponse(post)) {
    }
  }

  @Override
  public CloseableHttpResponse getResponse(HttpUriRequest target) throws IOException {
    return cachedRemoteReader.getResponse(target);
  }

  @Override
  public <T> T requestCachedStream(CacheParams cacheParams, String address, CacheDeserializer<InputStream, T> callback) throws IOException {
    return cachedRemoteStreamer.requestCached(cacheParams, buildGet(address), callback);
  }

  @Override
  public <T> T requestCachedReader(CacheParams cacheParams, String address, CacheDeserializer<Reader, T> callback) throws IOException {
    return cachedRemoteReader.requestCached(cacheParams, buildGet(address), callback);
  }

  @Override
  public <T> T requestCachedObject(CacheParams cacheParams, String address, Class<T> responseClass) throws IOException {
    return jsonService.requestCachedObject(cachedRemoteReader, cacheParams, buildUri(address), responseClass);
  }

  @Override
  public CloseableHttpResponse requestAsset(String assetId, Header... headers)
      throws IOException {
    HttpGet get = buildGet("assetfiles/" + assetId);
    get.setHeaders(headers);
    return cachedRemoteStreamer.getResponse(get);
  }

  private HttpGet buildGet(String address) {
    return new HttpGet(buildUri(address));
  }

  private URI buildUri(String address) {
    return UriUtil.concatenate(this.getServerUrl(), address);
  }

}
