package org.ambraproject.wombat.config.site.url;

import javax.servlet.http.HttpServletRequest;

/**
 * An attribute of a site definition that (along with others) tells whether a request is for a site.
 * <p>
 * Implementations should be package-private, and publicly exposed through public methods of {@link
 * org.ambraproject.wombat.config.site.url.SiteRequestScheme.Builder}.
 */
interface SiteRequestPredicate {

  /**
   * Check whether a request should be directed to this object's site.
   *
   * @param request a request from the web
   * @return {@code true} if the request is for this object's site; {@code false} otherwise
   */
  public abstract boolean isForSite(HttpServletRequest request);

}
