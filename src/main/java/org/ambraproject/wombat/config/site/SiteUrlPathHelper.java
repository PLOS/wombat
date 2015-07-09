package org.ambraproject.wombat.config.site;

import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;

public class SiteUrlPathHelper extends UrlPathHelper {

  private SiteResolver siteResolver;

  public SiteUrlPathHelper(SiteResolver siteResolver) {
    this.siteResolver = siteResolver;
  }

  @Override
  public String getLookupPathForRequest(HttpServletRequest request) {
    String lookupPath = super.getLookupPathForRequest(request);
    Site site = siteResolver.resolveSite(request);
    if (site == null) {
      return lookupPath;
    }
    return "[" + site.getKey() + "]" + lookupPath;
  }
}
