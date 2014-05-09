package org.ambraproject.wombat.config.site.url;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

public interface SiteRequestScheme {

  /**
   * Check whether a request should be directed to this object's site.
   *
   * @param request a request from the web
   * @return {@code true} if the request is for this object's site; {@code false} otherwise
   */
  public abstract boolean isForSite(HttpServletRequest request);

  /**
   * Build a link to a path within a site.
   *
   * @param request a request to the current site
   * @param path    a site-independent path
   * @return a link to that path within the same site
   */
  public abstract String buildLink(HttpServletRequest request, String path);

}
