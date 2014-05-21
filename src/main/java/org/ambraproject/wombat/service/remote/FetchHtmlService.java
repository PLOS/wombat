package org.ambraproject.wombat.service.remote;

import org.ambraproject.wombat.freemarker.FetchHtmlDirective.ElementSubstitution;
import org.ambraproject.wombat.freemarker.SitePageContext;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;

public interface FetchHtmlService {

  /**
   * Fetch a block of HTML from a remote source. The returned HTML may not be a complete HTML document.
   * <p/>
   * Typically, one implementing instance will represent one intended context for the HTML (for example, a homepage
   * body) and one service to fetch it from. Implementing classes may apply transformations to the raw HTML that it
   * receives from a service.
   *
   * @param sitePageContext the site of the context into which the HTML will be inserted
   * @param key             a key identifying the HTML to fetch
   * @param substitutions   substitutions to apply to the HTML (implementations that do not parse HTML may require this
   *                        argument to be empty)
   * @return an HTML block
   * @throws IOException
   */
  public abstract Reader readHtml(SitePageContext sitePageContext, String key, Collection<ElementSubstitution> substitutions)
      throws IOException;

}
