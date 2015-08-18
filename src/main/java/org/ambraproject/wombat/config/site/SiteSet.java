package org.ambraproject.wombat.config.site;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.ambraproject.wombat.config.RuntimeConfigurationException;
import org.ambraproject.wombat.config.site.url.SiteRequestScheme;
import org.ambraproject.wombat.config.theme.Theme;
import org.ambraproject.wombat.config.theme.ThemeTree;
import org.ambraproject.wombat.service.UnmatchedSiteException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Simple wrapper around a map from keys to site objects, for use as a Spring bean.
 */
public class SiteSet {

  private final ImmutableBiMap<String, Site> sites;

  @VisibleForTesting // otherwise use SiteSet.create
  public SiteSet(Iterable<Site> sites) {
    ImmutableBiMap.Builder<String, Site> map = ImmutableBiMap.builder();
    for (Site site : sites) {
      map.put(site.getKey(), site);
    }
    this.sites = map.build();
  }

  public static SiteSet create(List<Map<String, ?>> siteSpecifications, ThemeTree themeTree) {
    List<Site> sites = Lists.newArrayListWithCapacity(siteSpecifications.size());
    for (Map<String, ?> siteSpec : siteSpecifications) {
      String key = (String) siteSpec.get("key");
      Theme theme = themeTree.getTheme((String) siteSpec.get("theme"));

      Map<String, ?> resolveDefinition = (Map<String, ?>) siteSpec.get("resolve");
      SiteRequestScheme requestScheme = resolveDefinition != null ? parseRequestScheme(resolveDefinition)
          : SiteRequestScheme.builder().setPathToken(key).build();

      sites.add(new Site(key, theme, requestScheme));
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
   * @param journalKey specifies the journal
   * @return Site List containing any sites that match the journalKey
   * @throws UnmatchedSiteException if no journal is found
   */
  public ArrayList<Site> getSites(String journalKey) throws UnmatchedSiteException, IOException {
    ArrayList<Site> sitesToReturn = new ArrayList<>();
    for (Site site : sites.values()) {
      if (site.getJournalKey().equals(journalKey)) {
        sitesToReturn.add(site);
      }
    }
    if (sitesToReturn.isEmpty()) {
      throw new UnmatchedSiteException("Journal key not matched to any journal: " + journalKey);
    }
    return sitesToReturn;
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

  public ImmutableSet<Site> getSites() {
    return sites.values();
  }

  public ImmutableSet<String> getSiteKeys() {
    return sites.keySet();
  }

  /**
   * @return a set of all journal keys for this SiteSet.  Note that there may be fewer of
   *     these than siteKeys, since a journal can have multiple sites.
   */
  public ImmutableSet<String> getJournalKeys() {
    ImmutableSet.Builder<String> result = ImmutableSet.builder();
    for (Site site : sites.values()) {
      result.add(site.getJournalKey());
    }
    return result.build();
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
