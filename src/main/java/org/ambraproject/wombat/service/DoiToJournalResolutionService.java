package org.ambraproject.wombat.service;

import org.ambraproject.wombat.config.site.Site;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DoiToJournalResolutionService {

  public String getJournalKeyFromDoi(String doi, Site site) {
    ArrayList<Map<String, String>> journalDoiRegexList
        = (ArrayList<Map<String, String>>) site.getTheme().getConfigMap("journalDoiRegex").get("regexList");
    for (Map<String, String> journalDoiMap : journalDoiRegexList) {
      Pattern doiPattern = Pattern.compile(journalDoiMap.get("pattern"));
      Matcher doiMatcher = doiPattern.matcher(doi);
      if (doiMatcher.find()) {
        return journalDoiMap.get("journalKey");
      }
    }
    return null;
  }
}


