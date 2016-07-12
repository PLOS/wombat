package org.ambraproject.wombat.service.remote;

import com.google.common.base.Optional;
import org.ambraproject.wombat.freemarker.HtmlElementSubstitution;
import org.ambraproject.wombat.freemarker.HtmlElementTransformation;
import org.ambraproject.wombat.freemarker.SitePageContext;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface EditorialContentApi {

  /**
   * Requests a file from the content repository. Returns the full response.
   *
   * @param key     content repo key
   * @param version content repo version
   * @return the response from the content repo
   * @throws IOException
   * @throws org.ambraproject.wombat.service.EntityNotFoundException if the repository does not provide the file
   */
  public abstract CloseableHttpResponse request(String key, Optional<Integer> version, Collection<? extends Header> headers)
      throws IOException;

  public abstract <T> T requestCachedReader(RemoteCacheKey cacheKey, String key, Optional<Integer> version, CacheDeserializer<Reader, T> callback) throws IOException;

  public abstract Map<String, Object> requestMetadata(RemoteCacheKey cacheKey, String key, Optional<Integer> version) throws IOException;

  /**
   * Fetch a block of HTML from
   * Transform the raw HTML receives from a remote service.
   *
   * @param sitePageContext the site of the context into which the HTML will be inserted
   * @param key             a key identifying the HTML to fetch
   * @param substitutions   substitutions to apply to the HTML
   * @param transformations transformations to apply to the HTML elements
   * @return an HTML block
   * @throws IOException
   */
  public Reader readHtml(SitePageContext sitePageContext, String pageType, String key,
                         Set<HtmlElementTransformation> transformations,
                         Collection<HtmlElementSubstitution> substitutions) throws IOException;

  /**
   * Fetch a JSON object from a remote service.
   *
   * @param key             a key identifying the JSON string to fetch
   * @return an HTML block
   * @throws IOException
   */
  public Object getJson(String pageType, String key) throws IOException;

}
