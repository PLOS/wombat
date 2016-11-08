package org.ambraproject.wombat.service;

import org.ambraproject.wombat.config.site.Site;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DoiToJournalResolutionService {

  private List<Map<Site, Pattern>> patternCache = new ArrayList<>();

  public String getJournalKeyFromDoi(String doi, Site site) {
    Map<Site, Pattern> journalDoiMap;
    if (siteIsInCache(site)) {
      journalDoiMap = getFromCache(site);
    } else {
      journalDoiMap = getFromConfig(site);
    }

    if (journalDoiMap != null) {
      Pattern doiPattern = journalDoiMap.get(site);
      Matcher doiMatcher = doiPattern.matcher(doi);
      if (doiMatcher.find()) {
        return journalDoiMap.keySet().iterator().next().getJournalKey();
      }
    }
    return null;
  }

  private boolean siteIsInCache(Site site) {
    boolean siteIsInCache = false;
    for (Map<Site, Pattern> patternCacheEntry : patternCache) {
      if (patternCacheEntry.containsKey(site)) {
        siteIsInCache = true;
        break;
      }
    }
    return siteIsInCache;
  }

  private Map<Site, Pattern> getFromConfig(Site site) {
    List<Map<String, String>> patternMaps = (List<Map<String, String>>) site.getTheme()
        .getConfigMap("journalDoiRegex").get("regexList");
    String journalKey = site.getJournalKey();
    for (Map<String, String> patternMap : patternMaps) {
      String journalKeyFromConfig = patternMap.values().iterator().next();
      if (journalKey.equals(journalKeyFromConfig)) {
        putIntoCache(site, Pattern.compile(patternMap.get("pattern")));
        return getFromCache(site);
      }
    }
    return null;
  }

  private Map<Site, Pattern> getFromCache(Site site) {
    Map<Site, Pattern> patternMap = null;
    for (Map<Site, Pattern> patternCacheEntry : patternCache) {
      if (patternCacheEntry.containsKey(site)) {
        patternMap = patternCacheEntry;
        break;
      }
    }
    return patternMap;
  }

  private void putIntoCache(Site site, Pattern pattern) {
    if (!siteIsInCache(site)) {
      HashMap<Site, Pattern> patternMap = new HashMap<>();
      patternMap.put(site, pattern);
      getPatternCache().add(patternMap);
    }
  }

  private List<Map<Site, Pattern>> getPatternCache() {
    return patternCache;
  }
}


