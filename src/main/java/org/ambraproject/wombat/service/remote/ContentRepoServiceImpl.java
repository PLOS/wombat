package org.ambraproject.wombat.service.remote;

import com.google.common.base.Optional;
import org.ambraproject.wombat.util.UrlParamBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;

public class ContentRepoServiceImpl implements ContentRepoService {

  @Autowired
  private SoaService soaService;
  @Autowired
  private CachedRemoteService<InputStream> cachedRemoteStreamer;

  private URI contentRepoAddress;

  private URI getContentRepoAddress() throws IOException {
    if (contentRepoAddress != null) {
      return contentRepoAddress;
    }
    String address;
    try (Reader reader = soaService.requestReader("repo/")) {
      address = IOUtils.toString(reader);
    }
    try {
      return (contentRepoAddress = new URI(address));
    } catch (URISyntaxException e) {
      throw new RuntimeException("Invalid URI returned from service", e);
    }
  }

  /**
   * Requests a file from the content repository. Returns the full response.
   *
   * @param bucket  content repo bucket
   * @param key     content repo key
   * @param version content repo version
   * @return the response from the content repo
   * @throws IOException
   */
  @Override
  public ContentRepoResponse request(String bucket, String key, Optional<Integer> version)
      throws IOException {
    URI contentRepoAddress = getContentRepoAddress();
    if ("file".equals(contentRepoAddress.getScheme())) {
      return requestInDevMode(contentRepoAddress, bucket, key, version);
    }

    String contentRepoAddressStr = contentRepoAddress.toString();
    if (contentRepoAddressStr.endsWith("/")) {
      contentRepoAddressStr = contentRepoAddressStr.substring(0, contentRepoAddressStr.length() - 1);
    }
    UrlParamBuilder requestParams = UrlParamBuilder.params().add("key", key);
    if (version.isPresent()) {
      requestParams.add("version", version.get().toString());
    }
    URI requestAddress = URI.create(String.format("%s/objects/%s?%s",
        contentRepoAddressStr, bucket, requestParams.format()));

    final CloseableHttpResponse response = cachedRemoteStreamer.getResponse(requestAddress);
    return new ContentRepoResponse() {
      @Override
      public Header[] getAllHeaders() {
        return response.getAllHeaders();
      }

      @Override
      public InputStream getStream() throws IOException {
        return response.getEntity().getContent();
      }

      @Override
      public void close() throws IOException {
        response.close();
      }
    };
  }

  private ContentRepoResponse requestInDevMode(URI contentRepoAddress, String bucket, String key, Optional<Integer> version)
      throws FileNotFoundException {
    File path = new File(contentRepoAddress.getPath(), bucket + '/' + key);
    final FileInputStream stream = new FileInputStream(path); // duck under FileNotFoundException
    return new ContentRepoResponse() {
      @Override
      public Header[] getAllHeaders() {
        return new Header[0];
      }

      @Override
      public InputStream getStream() throws IOException {
        return stream;
      }

      @Override
      public void close() throws IOException {
        stream.close();
      }
    };
  }

}
