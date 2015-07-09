package org.ambraproject.wombat.config.site;

import org.springframework.util.AntPathMatcher;

public class SitePathMatcher extends AntPathMatcher {

  private SiteResolver siteResolver;

  public SitePathMatcher(SiteResolver siteResolver) {
    this.siteResolver = siteResolver;
  }

}
