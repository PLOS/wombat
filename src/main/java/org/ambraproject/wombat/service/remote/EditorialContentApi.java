package org.ambraproject.wombat.service.remote;

import org.ambraproject.wombat.freemarker.HtmlElementSubstitution;
import org.ambraproject.wombat.freemarker.HtmlElementTransformation;
import org.ambraproject.wombat.freemarker.SitePageContext;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.Set;

public interface EditorialContentApi extends ContentApi {

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
