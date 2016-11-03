package org.ambraproject.wombat.service;

import org.ambraproject.wombat.config.site.Site;

import java.util.Map;

public class DoiToJournalResolutionService {

  private Map<String,Object> journalDoiRegexList = null;

  public String getJournalKeyFromDoi(String doi, Site site) {
    Map<String, Object> journalDoiRegexList = getJournalDoiRegexList(site);
    for (Map.Entry<String, Object> entry : journalDoiRegexList.entrySet()) {
      if (doi.matches(entry.getValue().toString())) {
        return entry.getKey();
      }
    }
    return null;
  }

  private Map<String, Object> getJournalDoiRegexList(Site site) {
    if (journalDoiRegexList != null) {
      return journalDoiRegexList;
    }
    journalDoiRegexList = site.getTheme().getConfigMap("journalDoiRegex");
    return journalDoiRegexList;
  }
}


