package org.ambraproject.wombat.model;

import org.ambraproject.wombat.config.site.SiteSet;
import org.springframework.beans.factory.annotation.Autowired;

public class JournalFilterType implements SearchFilterType {

  @Autowired
  private SiteSet siteSet;

  public static final String JOURNAL_FILTER_MAP_KEY = "journal";

  @Override
  public String getFilterValue(String value) {
    return siteSet.getJournalKeyFromName(value);
  }

  @Override
  public String getParameterName() {
    return "filterJournals";
  }

  public String getFilterMapKey() {
    return JOURNAL_FILTER_MAP_KEY;
  }
}