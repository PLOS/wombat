package org.ambraproject.wombat.service.remote;

import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.ambraproject.wombat.util.UriUtil;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;

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
  public InputStream requestStream(String address) throws IOException {
    return cachedRemoteStreamer.request(buildGet(address));
  }

  @Override
  public Reader requestReader(String address) throws IOException {
    return cachedRemoteReader.request(buildGet(address));
  }

  @Override
  public <T> T requestObject(String address, Class<T> responseClass) throws IOException {
    // Just try to cache everything. We may want to narrow this in the future.
    return requestCachedObject("obj:" + address, address, responseClass);
  }

  @Override
  public <T> T requestCachedStream(String cacheKey, String address, CacheDeserializer<InputStream, T> callback) throws IOException {
    return cachedRemoteStreamer.requestCached(cacheKey, buildGet(address), callback);
  }

  @Override
  public <T> T requestCachedReader(String cacheKey, String address, CacheDeserializer<Reader, T> callback) throws IOException {
    return cachedRemoteReader.requestCached(cacheKey, buildGet(address), callback);
  }

  @Override
  public <T> T requestCachedObject(String cacheKey, String address, Class<T> responseClass) throws IOException {
    return jsonService.requestCachedObject(cachedRemoteReader, cacheKey, buildUri(address), responseClass);
  }

  @Override
  public CloseableHttpResponse requestAsset(String assetId, Header... headers)
      throws IOException {
    HttpGet get = buildGet("assetfiles/" + assetId);
    get.setHeaders(headers);
    return cachedRemoteStreamer.getResponse(get);
  }

  @Override
  public CloseableHttpResponse requestFromContentRepo(String bucket, String key, String version) throws IOException {
    String address = String.format("repo/%s/%s/%s", bucket, key, version);
    return cachedRemoteStreamer.getResponse(buildGet(address));
  }

  private HttpGet buildGet(String address) {
    return new HttpGet(buildUri(address));
  }

  private URI buildUri(String address) {
    return UriUtil.concatenate(runtimeConfiguration.getServer(), address);
  }

}
