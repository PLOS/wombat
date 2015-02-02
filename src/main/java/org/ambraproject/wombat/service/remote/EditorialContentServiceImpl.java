package org.ambraproject.wombat.service.remote;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import org.ambraproject.wombat.util.CacheParams;
import org.ambraproject.wombat.util.UrlParamBuilder;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;

public class EditorialContentServiceImpl implements EditorialContentService {

  @Autowired
  private SoaService soaService;
  @Autowired
  private CachedRemoteService<InputStream> cachedRemoteStreamer;
  @Autowired
  private CachedRemoteService<Reader> cachedRemoteReader;
  @Autowired
  private Gson gson;

  private URI contentRepoAddress;
  private String repoBucketName;

  private void setRepoConfig() throws IOException {
    Map<String, Object> repoConfig = (Map<String, Object>) soaService.requestObject("config?type=repo", Map.class);
    Map<String, Object> editorialConfig = (Map<String, Object>) repoConfig.get("editorial");
    if (editorialConfig == null) throw new RuntimeException("config?type=repo did not provide \"editorial\" config");
    String address = (String) editorialConfig.get("address");
    if (address == null) throw new RuntimeException("config?type=repo did not provide \"editorial.address\"");
    String bucket = (String) editorialConfig.get("bucket");
    if (bucket == null) throw new RuntimeException("config?type=repo did not provide \"editorial.bucket\"");

    try {
      contentRepoAddress = new URI(address);
    } catch (URISyntaxException e) {
      throw new RuntimeException("Invalid content repo URI returned from service", e);
    }
    repoBucketName = bucket;
  }

  private URI getContentRepoAddress() throws IOException {
    if (contentRepoAddress == null) {
      setRepoConfig();
    }
    return Preconditions.checkNotNull(contentRepoAddress);
  }

  private String getRepoBucketName() throws IOException {
    if (repoBucketName == null) {
      setRepoConfig();
      if (repoBucketName == null) {
        throw new RuntimeException("No repository bucket name returned from service");
      }
    }
    return Preconditions.checkNotNull(repoBucketName);
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
  public CloseableHttpResponse request(String key, Optional<Integer> version, Collection<? extends Header> headers)
      throws IOException {
    URI requestAddress = buildUri(key, version, RequestMode.OBJECT);
    HttpGet get = new HttpGet(requestAddress);
    get.setHeaders(headers.toArray(new Header[headers.size()]));
    return cachedRemoteStreamer.getResponse(get);
  }

  private static enum RequestMode {
    OBJECT, METADATA;

    private String getPathComponent() {
      switch (this) {
        case OBJECT:
          return "objects";
        case METADATA:
          return "objects/meta";
        default:
          throw new AssertionError();
      }
    }
  }

  private URI buildUri(String key, Optional<Integer> version, RequestMode mode) throws IOException {
    String contentRepoAddressStr = getContentRepoAddress().toString();
    if (contentRepoAddressStr.endsWith("/")) {
      contentRepoAddressStr = contentRepoAddressStr.substring(0, contentRepoAddressStr.length() - 1);
    }
    UrlParamBuilder requestParams = UrlParamBuilder.params().add("key", key);
    if (version.isPresent()) {
      requestParams.add("version", version.get().toString());
    }
    String repoBucketName = getRepoBucketName();
    return URI.create(String.format("%s/%s/%s?%s",
        contentRepoAddressStr, mode.getPathComponent(), repoBucketName, requestParams.format()));
  }

  @Override
  public <T> T requestCachedReader(CacheParams cacheParams, String key, Optional<Integer> version, CacheDeserializer<Reader, T> callback) throws IOException {
    Preconditions.checkNotNull(callback);
    return cachedRemoteReader.requestCached(cacheParams, new HttpGet(buildUri(key, version, RequestMode.OBJECT)), callback);
  }

  @Override
  public Map<String, Object> requestMetadata(CacheParams cacheParams, String key, Optional<Integer> version) throws IOException {
    return cachedRemoteReader.requestCached(cacheParams, new HttpGet(buildUri(key, version, RequestMode.METADATA)),
        new CacheDeserializer<Reader, Map<String, Object>>() {
          @Override
          public Map<String, Object> read(Reader stream) throws IOException {
            return gson.fromJson(stream, Map.class);
          }
        });
  }

}
