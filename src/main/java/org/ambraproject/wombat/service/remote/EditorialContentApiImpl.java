package org.ambraproject.wombat.service.remote;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import org.ambraproject.wombat.config.ServiceCacheSet;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.freemarker.HtmlElementSubstitution;
import org.ambraproject.wombat.freemarker.HtmlElementTransformation;
import org.ambraproject.wombat.freemarker.SitePageContext;
import org.ambraproject.wombat.service.ApiAddress;
import org.ambraproject.wombat.util.UrlParamBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class EditorialContentApiImpl implements EditorialContentApi {

  @Autowired
  private ArticleApi articleApi;
  @Autowired
  private CachedRemoteService<InputStream> cachedRemoteStreamer;
  @Autowired
  private CachedRemoteService<Reader> cachedRemoteReader;
  @Autowired
  private Gson gson;
  @Autowired
  private SiteSet siteSet;
  @Autowired
  private ServiceCacheSet serviceCacheSet;

  private transient URI contentRepoAddress;
  private transient String repoBucketName;

  private void setRepoConfig() throws IOException {
    Map<String, Object> repoConfig = (Map<String, Object>) articleApi.requestObject(
        ApiAddress.builder("config").addParameter("type", "repo").build(),
        Map.class);
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
  public <T> T requestCachedReader(RemoteCacheKey cacheKey, String key, Optional<Integer> version, CacheDeserializer<Reader, T> callback) throws IOException {
    Preconditions.checkNotNull(callback);
    return cachedRemoteReader.requestCached(cacheKey, new HttpGet(buildUri(key, version, RequestMode.OBJECT)), callback);
  }

  @Override
  public Map<String, Object> requestMetadata(RemoteCacheKey cacheKey, String key, Optional<Integer> version) throws IOException {
    return cachedRemoteReader.requestCached(cacheKey, new HttpGet(buildUri(key, version, RequestMode.METADATA)),
        (Reader stream) -> gson.fromJson(stream, Map.class));
  }

  /**
   * {@inheritDoc}
   * <p/>
   * Applies transforms to HTML attributes and performs substitution of placeholder HTML elements with stored content
   */
  @Override
  public Reader readHtml(final SitePageContext sitePageContext, String pageType, String key,
                         final Set<HtmlElementTransformation> transformations,
                         final Collection<HtmlElementSubstitution> substitutions)
          throws IOException {
    RemoteCacheKey cacheKey = RemoteCacheKey.create(serviceCacheSet.getEditorialContentCache(), pageType, key);
    Optional<Integer> version = Optional.absent();     // TODO May want to support page versioning at some point using fetchHtmlDirective

    String transformedHtml = requestCachedReader(cacheKey, key, version, (Reader htmlReader) -> {
      // It would be nice to feed the reader directly into the parser, but Jsoup's API makes this awkward.
      // The whole document will be in memory anyway, so buffering it into a string is no great performance loss.
      String htmlString = IOUtils.toString(htmlReader);
      Document document = Jsoup.parseBodyFragment(htmlString);

      for (HtmlElementTransformation transformation : transformations) {
        transformation.apply(sitePageContext, siteSet, document);
      }
      for (HtmlElementSubstitution substitution : substitutions) {
        substitution.substitute(document);
      }

      // We received a snippet, which Jsoup has automatically turned into a complete HTML document.
      // We want to return only the transformed snippet, so retrieve it from the body tag.
      return document.getElementsByTag("body").html();
    });
    return new StringReader(transformedHtml);
  }

  /**
   * {@inheritDoc}
   * <p/>
   * Returns a JSON object from a remote service
   */
  @Override
  public Object getJson(String pageType, String key) throws IOException {
    RemoteCacheKey cacheKey = RemoteCacheKey.create(serviceCacheSet.getEditorialContentCache(), pageType, key);
    Optional<Integer> version = Optional.absent();
    Object jsonObj = requestCachedReader(cacheKey, key, version,
        (Reader jsonReader) -> gson.fromJson(jsonReader, Object.class));
    return jsonObj;
  }
}
