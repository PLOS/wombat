package org.ambraproject.wombat.model;

import org.ambraproject.wombat.config.site.SiteSet;
import org.springframework.beans.factory.annotation.Autowired;

public class JournalFilterType implements SearchFilterType {

  @Autowired
  private SiteSet siteSet;

  @Override
  public String getFilterValue(String value) {
    return siteSet.getJournalKey(value);
  }

  @Override
  public String getParameterName() {
    return "filterJournals";
  }
}