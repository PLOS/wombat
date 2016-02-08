package org.ambraproject.wombat.service.remote;

import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;

public class ArticleApiImpl extends AbstractRestfulJsonApi implements ArticleApi {

  @Autowired
  private RuntimeConfiguration runtimeConfiguration;

  @Override
  public URL getServerUrl() {
    return runtimeConfiguration.getServer();
  }

  @Override
  protected String getCachePrefix() {
    return "article";
  }

  @Override
  public CloseableHttpResponse requestAsset(String assetId, Collection<? extends Header> headers)
      throws IOException {
    HttpGet get = buildGet("assetfiles/" + assetId);
    get.setHeaders(headers.toArray(new Header[headers.size()]));
    return cachedRemoteStreamer.getResponse(get);
  }

}
