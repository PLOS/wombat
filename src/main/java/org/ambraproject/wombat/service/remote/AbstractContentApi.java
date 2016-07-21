package org.ambraproject.wombat.service.remote;

import com.google.gson.Gson;
import org.ambraproject.wombat.service.ApiAddress;
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
import java.util.Objects;

public abstract class AbstractContentApi implements ContentApi {

  @Autowired
  private ArticleApi articleApi;
  @Autowired
  private CachedRemoteService<InputStream> cachedRemoteStreamer;
  @Autowired
  private CachedRemoteService<Reader> cachedRemoteReader;
  @Autowired
  protected Gson gson;

  private static class RepoConfig {
    private final String address;
    private final String bucketName;

    private RepoConfig(URI address, String bucketName) {
      this.address = sanitizeAddress(address);
      this.bucketName = Objects.requireNonNull(bucketName);
    }

    private static String sanitizeAddress(URI address) {
      String addressString = address.toString();
      return addressString.endsWith("/") ? addressString.substring(0, addressString.length() - 1) : addressString;
    }
  }

  private static final ApiAddress REPO_CONFIG_ADDRESS = ApiAddress.builder("config").addParameter("type", "repo").build();

  private transient RepoConfig repoConfig;

  private RepoConfig getRepoConfig() throws IOException {
    if (this.repoConfig != null) return repoConfig;
    final String repoConfigKey = getRepoConfigKey();
    Map<String, Object> serverRepoInfo = (Map<String, Object>) articleApi.requestObject(REPO_CONFIG_ADDRESS, Map.class);
    Map<String, Object> repoInfo = (Map<String, Object>) serverRepoInfo.get(repoConfigKey);
    if (repoInfo == null) {
      throw new RuntimeException(String.format("config?type=repo did not provide \"%s\" config", repoConfigKey));
    }
    String address = (String) repoInfo.get("address");
    if (address == null) {
      throw new RuntimeException(String.format("config?type=repo did not provide \"%s.address\"", repoConfigKey));
    }
    String bucket = (String) repoInfo.get("bucket");
    if (bucket == null) {
      throw new RuntimeException(String.format("config?type=repo did not provide \"%s.bucket\"", repoConfigKey));
    }

    URI contentRepoAddress;
    try {
      contentRepoAddress = new URI(address);
    } catch (URISyntaxException e) {
      throw new RuntimeException("Invalid content repo URI returned from service", e);
    }
    return this.repoConfig = new RepoConfig(contentRepoAddress, bucket);
  }

  protected abstract String getRepoConfigKey();

  @Override
  public final CloseableHttpResponse request(ContentKey key, Collection<? extends Header> headers)
      throws IOException {
    URI requestAddress = buildUri(key, RequestMode.OBJECT);
    HttpGet get = new HttpGet(requestAddress);
    get.setHeaders(headers.toArray(new Header[headers.size()]));
    return cachedRemoteStreamer.getResponse(get);
  }

  /**
   * Designates whether a request to the Content Repo service is for an object (file content) or metadata (JSON).
   */
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

  private URI buildUri(ContentKey key, RequestMode mode) throws IOException {
    RepoConfig repoConfig = getRepoConfig();
    UrlParamBuilder requestParams = UrlParamBuilder.params();
    key.setParameters(requestParams);
    String repoBucketName = repoConfig.bucketName;
    return URI.create(String.format("%s/%s/%s?%s",
        repoConfig.address, mode.getPathComponent(), repoBucketName, requestParams.format()));
  }

  protected final <T> T requestCachedReader(RemoteCacheKey cacheKey, ContentKey key, CacheDeserializer<Reader, T> callback) throws IOException {
    HttpGet target = new HttpGet(buildUri(key, RequestMode.OBJECT));
    return cachedRemoteReader.requestCached(cacheKey, target, callback);
  }

  protected final <T> T requestCachedStream(RemoteCacheKey cacheKey, ContentKey key, CacheDeserializer<InputStream, T> callback) throws IOException {
    HttpGet target = new HttpGet(buildUri(key, RequestMode.OBJECT));
    return cachedRemoteStreamer.requestCached(cacheKey, target, callback);
  }

  @Override
  public final Map<String, Object> requestMetadata(RemoteCacheKey cacheKey, ContentKey key) throws IOException {
    return cachedRemoteReader.requestCached(cacheKey, new HttpGet(buildUri(key, RequestMode.METADATA)),
        (Reader stream) -> gson.fromJson(stream, Map.class));
  }

}
