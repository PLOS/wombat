package org.ambraproject.wombat.config.site;

import org.ambraproject.wombat.util.HttpDebug;

import javax.servlet.http.HttpServletRequest;

/**
 * Indicates that a request could not be resolved to a site.
 */
public class UnresolvedSiteException extends RuntimeException {

  public UnresolvedSiteException(HttpServletRequest request) {
    super(HttpDebug.dump(request));
  }

}
