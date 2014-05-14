package org.ambraproject.wombat.service.remote;

import java.io.IOException;
import java.io.Reader;

public interface FetchHtmlService {

  /**
   * Fetch a block of HTML from a remote source. The returned HTML may not be a complete HTML document.
   * <p/>
   * Typically, one implementing instance will represent one intended context for the HTML (for example, a homepage
   * body) and one service to fetch it from. Implementing classes may apply transformations to the raw HTML that it
   * receives from a service.
   *
   * @param key a key identifying the HTML to fetch
   * @return an HTML block
   * @throws IOException
   */
  public abstract Reader readHtml(String key) throws IOException;

}
