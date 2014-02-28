package org.ambraproject.wombat.service;

import com.google.common.io.Closer;
import org.ambraproject.rhombat.HttpDateUtil;
import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.Calendar;

public class SoaServiceImpl extends JsonService implements SoaService {

  @Autowired
  private RuntimeConfiguration runtimeConfiguration;

  @Override
  public InputStream requestStream(String address) throws IOException {
    return requestStream(buildUri(address));
  }

  @Override
  public <T> T requestObject(String address, Class<T> responseClass) throws IOException {
    return requestObject(buildUri(address), responseClass);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> IfModifiedSinceResult<T> requestObjectIfModifiedSince(String address, Class<T> responseClass,
      Calendar lastModified) throws IOException {
    URI uri = buildUri(address);
    BasicHeader header = new BasicHeader("If-Modified-Since", HttpDateUtil.format(lastModified));

    Closer closer = Closer.create();
    try {
      HttpResponse response = closer.register(makeRequest(uri, header));
      Header[] lastModifiedHeaders = response.getHeaders("Last-Modified");
      if (lastModifiedHeaders.length != 1) {
        throw new RuntimeException("Expecting 1 Last-Modified header, got " + lastModifiedHeaders.length);
      }
      IfModifiedSinceResult<T> result = new IfModifiedSinceResult<>();
      result.lastModified = HttpDateUtil.parse(lastModifiedHeaders[0].getValue());

      if (response.getStatusLine().getStatusCode() == 200) {
        InputStream is = closer.register(new BufferedInputStream(response.getEntity().getContent()));

        // Kind of nasty, but necessary for now since we're only caching article XML, which is a String
        if (responseClass == String.class) {
          result.result = (T) IOUtils.toString(is);
        } else {
          Reader reader = closer.register(new InputStreamReader(is));
          result.result = gson.fromJson(reader, responseClass);
        }
      } else if (response.getStatusLine().getStatusCode() != 304) {
        throw new RuntimeException("Unexpected status code " + response.getStatusLine().getStatusCode());
      }  // else we got a 304, and we want result.result to be null

      return result;
    } catch (Throwable t) {
      throw closer.rethrow(t);
    } finally {
      closer.close();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CloseableHttpResponse requestAsset(String assetId, Header... headers) throws IOException {
    return makeRequest(buildUri("assetfiles/" + assetId), headers);
  }

  private URI buildUri(String address) {
    return buildUri(runtimeConfiguration.getServer(), address);
  }
}
