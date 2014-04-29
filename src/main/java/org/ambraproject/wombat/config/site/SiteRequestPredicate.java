package org.ambraproject.wombat.config.site;

import javax.servlet.http.HttpServletRequest;

/**
 * Condition for a request being directed to a {@link org.ambraproject.wombat.config.site.Site}. One implementing object
 * corresponds to one site.
 */
interface SiteRequestPredicate {

  /**
   * Check whether the request is for this object's site.
   *
   * @param request a request from the web
   * @return {@code true} if the request is for this object's site; {@code false} otherwise
   */
  boolean isForSite(HttpServletRequest request);

}
