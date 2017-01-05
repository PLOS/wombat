package org.ambraproject.wombat.config.site;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.ambraproject.wombat.config.RuntimeConfigurationException;
import org.ambraproject.wombat.config.site.url.SiteRequestScheme;
import org.ambraproject.wombat.config.theme.Theme;
import org.ambraproject.wombat.config.theme.ThemeTree;
import org.ambraproject.wombat.service.UnmatchedSiteException;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Simple wrapper around a map from keys to site objects, for use as a Spring bean.
 */
public class SiteSet {

  private final ImmutableBiMap<String, Site> sites;
  private final ImmutableBiMap<String, JournalSite> journalSites;

  private final ImmutableBiMap<String, String> journalKeysToNames;

  @VisibleForTesting // otherwise use SiteSet.create
  public SiteSet(Iterable<Site> siteObjs) {
    ImmutableBiMap.Builder<String, Site> sites = ImmutableBiMap.builder();
    ImmutableBiMap.Builder<String, JournalSite> journalSites = ImmutableBiMap.builder();
    for (Site site : siteObjs) {
      String siteKey = site.getKey();
      sites.put(siteKey, site);
      if (site instanceof JournalSite) {
        journalSites.put(siteKey, (JournalSite) site);
      }
    }
    this.sites = sites.build();
    this.journalSites = journalSites.build();

    this.journalKeysToNames = buildJournalKeysToNames(this.journalSites.values());
  }

  private static ImmutableMultimap<String, JournalSite> groupByJournalKey(Set<JournalSite> sites) {
    ImmutableMultimap.Builder<String, JournalSite> builder = ImmutableMultimap.builder();
    for (JournalSite site : sites) {
      builder.put(site.getJournalKey(), site);
    }
    return builder.build();
  }

  /**
   * Build a map representing the one-to-one relationship between journal keys and journal names.
   * <p/>
   * As a side effect, validates that the relationship actually is one-to-one -- that is, that multiple sites have the
   * same journal key if and only if they have the same journal name. It is easier to obey this constraint if the {@code
   * journalKey} and {@code journalName} config values are always set in the {@code journal.yaml} or {@code
   * journal.json} file of the same theme, which should be a parent of all themes belonging to that journal.
   *
   * @param sites the set of all sites being served
   * @return a map between journal keys and journal names
   * @throws IllegalArgumentException if two sites with the same journal key have unequal journal names or if two sites
   *                                  with the same journal name have unequal journal keys
   */
  private static ImmutableBiMap<String, String> buildJournalKeysToNames(Set<JournalSite> sites) {
    Multimap<String, JournalSite> keysToSites = groupByJournalKey(sites);
    BiMap<String, String> keysToNames = HashBiMap.create(keysToSites.keySet().size());
    for (Map.Entry<String, Collection<JournalSite>> entry : keysToSites.asMap().entrySet()) {
      String journalKey = entry.getKey();
      Iterator<JournalSite> siteIterator = entry.getValue().iterator();
      String journalName = siteIterator.next().getJournalName();
      while (siteIterator.hasNext()) {
        String nextJournalName = siteIterator.next().getJournalName();
        if (!journalName.equals(nextJournalName)) {
          String message = String.format("Inconsistent journal names with key=%s: %s; %s",
              journalKey, journalName, nextJournalName);
          throw new IllegalArgumentException(message);
        }
      }

      if (keysToNames.containsValue(journalName)) {
        String message = String.format("Overloaded journal name (%s) for keys: %s; %s",
            journalName, journalKey, keysToNames.inverse().get(journalName));
        throw new IllegalArgumentException(message);
      }
      keysToNames.put(journalKey, journalName);
    }
    return ImmutableBiMap.copyOf(keysToNames);
  }

  @VisibleForTesting
  public static final String JOURNAL_KEY_PATH = "journal";
  @VisibleForTesting
  public static final String CONFIG_KEY_FOR_JOURNAL = "journalKey";
  @VisibleForTesting
  public static final String JOURNAL_NAME = "journalName";


  private static Site buildSite(String key, Theme theme, SiteRequestScheme requestScheme) {
    Map<String, Object> journalMap = theme.getConfigMap(JOURNAL_KEY_PATH);

    String journalKey = (String) journalMap.get(CONFIG_KEY_FOR_JOURNAL);
    String journalName = (String) journalMap.get(JOURNAL_NAME);

    if (Strings.isNullOrEmpty(journalKey) != Strings.isNullOrEmpty(journalName)) {
      String message = String.format("TODO", theme.getKey());
      throw new RuntimeException(message);
    }

    return Strings.isNullOrEmpty(journalKey)
        ? new Site(key, theme, requestScheme)
        : new JournalSite(key, theme, requestScheme, journalKey, journalName);
  }

  public static SiteSet create(List<Map<String, ?>> siteSpecifications, ThemeTree themeTree) {
    List<Site> sites = Lists.newArrayListWithCapacity(siteSpecifications.size());
    for (Map<String, ?> siteSpec : siteSpecifications) {
      String key = (String) siteSpec.get("key");
      Theme theme = themeTree.getTheme((String) siteSpec.get("theme"));

      Map<String, ?> resolveDefinition = (Map<String, ?>) siteSpec.get("resolve");
      SiteRequestScheme requestScheme = resolveDefinition != null ? parseRequestScheme(resolveDefinition)
          : SiteRequestScheme.builder().setPathToken(key).build();

      sites.add(buildSite(key, theme, requestScheme));
    }
    validateSchemes(sites);
    return new SiteSet(sites);
  }

  private static SiteRequestScheme parseRequestScheme(Map<String, ?> resolveDefinition) {
    SiteRequestScheme.Builder builder = SiteRequestScheme.builder();

    String pathToken = (String) resolveDefinition.get("path");
    if (pathToken != null) {
      builder.setPathToken(pathToken);
    }

    List<Map<String, ?>> headers = (List<Map<String, ?>>) resolveDefinition.get("headers");
    if (headers != null && !headers.isEmpty()) {
      for (Map<String, ?> headerSpec : headers) {
        String headerName = (String) headerSpec.get("name");
        String headerValue = (String) headerSpec.get("value");
        if (headerName == null || headerValue == null) {
          throw new RuntimeConfigurationException("Each entry under \"headers\" must have a \"name\" and \"value\" field");
        }
        builder.requireHeader(headerName, headerValue);
      }
    }

    String hostName = (String) resolveDefinition.get("host");
    if (hostName != null) {
      builder.specifyHost(hostName);
    }

    return builder.build();
  }

  /**
   * @throws RuntimeConfigurationException if there a detectable collision between site config values
   */
  private static void validateSchemes(Collection<Site> sites) {
    Map<String, Site> keys = Maps.newHashMapWithExpectedSize(sites.size());
    Map<SiteRequestScheme, Site> requestSchemes = Maps.newHashMapWithExpectedSize(sites.size());
    for (Site site : sites) {
      Site previous;

      String key = site.getKey();
      previous = keys.put(key, site);
      if (previous != null) {
        throw new RuntimeConfigurationException("Multiple sites have the same key: " + key);
      }

      /*
       * Depends on the equals and hashCode implementations of the SiteRequestPredicate implementations contained in
       * the SiteRequestScheme. If they don't provide good overrides, this check is still safe against false positives
       * but may not catch all error conditions.
       */
      SiteRequestScheme requestScheme = site.getRequestScheme();
      previous = requestSchemes.put(requestScheme, site);
      if (previous != null) {
        String message = String.format("Multiple sites (%s, %s) have the same request scheme: %s",
            key, previous.getKey(), requestScheme);
        throw new RuntimeConfigurationException(message);
      }
    }
  }

  /**
   * Attempts to load a site based on site key.
   *
   * @param key specifies the site
   * @return Site instance matching the key
   * @throws UnmatchedSiteException if no site is found
   */
  public Site getSite(String key) throws UnmatchedSiteException {
    Site site = sites.get(key);
    if (site == null) {
      throw new UnmatchedSiteException("Key not matched to site: " + key);
    }
    return site;
  }

  public JournalSite getJournalSite(String key) throws UnmatchedSiteException {
    JournalSite site = journalSites.get(key);
    if (site == null) {
      throw sites.containsKey(key)
          ? new UnmatchedSiteException("Key is for a journal-neutral site: " + key)
          : new UnmatchedSiteException("Key not matched to site: " + key);
    }
    return site;
  }

  public ImmutableSet<Site> getSites() {
    return sites.values();
  }

  public ImmutableSet<JournalSite> getJournalSites() {
    return journalSites.values();
  }

  /**
   * @return a journal key for a requested journal name
   */
  public String getJournalKeyFromName(String journalName) {
    String journalKey = journalKeysToNames.inverse().get(journalName);
    if (journalKey == null) {
      throw new UnmatchedSiteException("Journal name not matched to key: " + journalName);
    }
    return journalKey;
  }

  public String getJournalNameFromKey(String journalKey) {
    String journalName = journalKeysToNames.get(journalKey);
    if (journalName == null) {
      throw new UnmatchedSiteException("Journal key not matched to name: " + journalKey);
    }
    return journalName;
  }

  public ImmutableSet<String> getSiteKeys() {
    return sites.keySet();
  }

  /**
   * @return a set of all journal keys for this SiteSet.  Note that there may be fewer of these than siteKeys, since a
   * journal can have multiple sites.
   */
  public ImmutableSet<String> getJournalKeys() {
    return journalKeysToNames.keySet();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    return sites.equals(((SiteSet) o).sites);
  }

  @Override
  public int hashCode() {
    return sites.hashCode();
  }

}
