package org.ambraproject.wombat.service.remote;

import com.google.common.base.Preconditions;
import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

public class SoaServiceImpl extends JsonService implements SoaService {

  @Autowired
  private RuntimeConfiguration runtimeConfiguration;

  @Override
  public InputStream requestStream(String address) throws IOException {
    return requestStream(buildUri(address));
  }

  @Override
  public <T> T requestObject(String address, Class<T> responseClass) throws IOException {
    // Just try to cache everything. We may want to narrow this in the future.
    return requestCachedObject("obj:" + address, address, responseClass);
  }

  @Override
  public <T> T requestCachedStream(String cacheKey, String address, CacheDeserializer<? extends T> callback) throws IOException {
    return requestCachedStream(cacheKey, buildUri(address), callback);
  }

  @Override
  public <T> T requestCachedObject(String cacheKey, String address, Class<T> responseClass) throws IOException {
    return requestCachedObject(cacheKey, buildUri(address), responseClass);
  }

  @Override
  public CloseableHttpResponse requestAsset(String assetId, Header... headers)
      throws IOException {
    return makeRequest(buildUri("assetfiles/" + assetId), headers);
  }

  @Override
  public Map<?, ?> requestArticleMetadata(String articleId) throws IOException {
    return requestObject(String.format("articles/%s?excludeCitations=true", articleId), Map.class);
  }

  private URI buildUri(String address) {
    return buildUri(runtimeConfiguration.getServer(), address);
  }

  /**
   * Builds a complete URI given a URL that specifies the server and a string that is the remainder of the query.
   *
   * @param server  host, port, and optionally part of the path.  For example "http://www.example.com/" or
   *                "https://plos.org/api/".
   * @param address the remainder of the path and query string.  For example "articles/foo.pone.1234567?comments=true"
   * @return a URI to the complete path and query string
   */
  private static URI buildUri(URL server, String address) {
    URI targetUri;
    try {
      targetUri = new URL(server, Preconditions.checkNotNull(address)).toURI();
    } catch (MalformedURLException | URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
    return targetUri;
  }

}
