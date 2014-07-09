package org.ambraproject.wombat.service.remote;

import com.google.common.base.Optional;
import org.ambraproject.wombat.util.UrlParamBuilder;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpGet;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class ContentRepoServiceImpl implements ContentRepoService {

  @Autowired
  private SoaService soaService;
  @Autowired
  private CachedRemoteService<InputStream> cachedRemoteStreamer;

  private URI contentRepoAddress;
  private String repoBucketName;

  private void setRepoConfig() throws IOException {
    Map<String,Object> repoConfig = (Map<String, Object>) soaService.requestObject("config?type=repo", Map.class);
    Object address = repoConfig.get("contentRepoAddress");
    if (address != null){
      try {
        contentRepoAddress = new URI(address.toString());
      } catch (URISyntaxException e) {
        throw new RuntimeException("Invalid content repo URI returned from service", e);
      }
    }
    Object bucket = repoConfig.get("repoBucketName");
    if (bucket != null){
      repoBucketName = bucket.toString();
    }

  }

  private URI getContentRepoAddress() throws IOException {
    if (contentRepoAddress == null) {
      setRepoConfig();
      if (contentRepoAddress == null) {
        throw new RuntimeException("No content repo URI returned from service");
      }
    }
    return contentRepoAddress;
  }

  private String getRepoBucketName() throws IOException {
    if (repoBucketName == null) {
      setRepoConfig();
      if (repoBucketName == null) {
        throw new RuntimeException("No repository bucket name returned from service");
      }
    }
    return repoBucketName;
  }

  /**
   * Requests a file from the content repository. Returns the full response.
   *
   * @param key     content repo key
   * @param version content repo version
   * @return the response from the content repo
   * @throws IOException
   * @throws org.ambraproject.wombat.service.EntityNotFoundException if the repository does not provide the file
   */
  @Override
  public AssetServiceResponse request(String key, Optional<Integer> version, Header... headers)
      throws IOException {
    URI contentRepoAddress = getContentRepoAddress();
    if ("file".equals(contentRepoAddress.getScheme())) {
      return requestInDevMode(contentRepoAddress, key, version);
    }

    String contentRepoAddressStr = contentRepoAddress.toString();
    if (contentRepoAddressStr.endsWith("/")) {
      contentRepoAddressStr = contentRepoAddressStr.substring(0, contentRepoAddressStr.length() - 1);
    }
    UrlParamBuilder requestParams = UrlParamBuilder.params().add("key", key);
    if (version.isPresent()) {
      requestParams.add("version", version.get().toString());
    }
    String repoBucketName = getRepoBucketName();
    URI requestAddress = URI.create(String.format("%s/objects/%s?%s",
        contentRepoAddressStr, repoBucketName, requestParams.format()));

    HttpGet get = new HttpGet(requestAddress);
    get.setHeaders(headers);
    return AssetServiceResponse.wrap(cachedRemoteStreamer.getResponse(get));
  }

  private AssetServiceResponse requestInDevMode(URI contentRepoAddress, String key, Optional<Integer> version)
      throws FileNotFoundException {
    File path = new File(contentRepoAddress.getPath(), key);
    return AssetServiceResponse.wrap(path);
  }

}
