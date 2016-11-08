package org.ambraproject.wombat.service;

import com.google.common.collect.ImmutableList;
import org.ambraproject.wombat.config.site.Site;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DoiToJournalResolutionService {

  private final Map<Site, ImmutableList<DoiJournalRegex>> patternCache = new ConcurrentHashMap<>();

  private class DoiJournalRegex {
    private final String journalKey;
    private final Pattern pattern;

    public DoiJournalRegex(String journalKey, Pattern pattern) {
      this.journalKey = Objects.requireNonNull(journalKey);
      this.pattern = Objects.requireNonNull(pattern);
    }
  }

  public String getJournalKeyFromDoi(String doi, Site site) {
    ImmutableList<DoiJournalRegex> doiJournalRegices
        = patternCache.computeIfAbsent(site, this::getFromConfig);

    for (DoiJournalRegex doiJournalRegex : doiJournalRegices) {
      Pattern pattern = doiJournalRegex.pattern;
      Matcher matcher = pattern.matcher(doi);
      if (matcher.matches()) {
        return doiJournalRegex.journalKey;
      }
    }
    return null;
  }


  private ImmutableList<DoiJournalRegex> getFromConfig(Site site) {
    List<Map<String, String>> patternMaps = (List<Map<String, String>>) site.getTheme()
        .getConfigMap("journalDoiRegex").get("regexList");
    List<DoiJournalRegex> regices = patternMaps.stream().map(regexMap -> {
      String journalKey = regexMap.get("journalKey");
      String pattern = regexMap.get("pattern");
      return new DoiJournalRegex(journalKey, Pattern.compile(pattern));
    }).collect(Collectors.toList());
    return ImmutableList.copyOf(regices);
  }
}


