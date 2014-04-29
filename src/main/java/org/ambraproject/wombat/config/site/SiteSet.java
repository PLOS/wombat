package org.ambraproject.wombat.config.site;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.ambraproject.wombat.config.theme.Theme;
import org.ambraproject.wombat.service.UnmatchedSiteException;

import java.util.List;
import java.util.Map;

/**
 * Simple wrapper around a map from keys to site objects, for use as a Spring bean.
 */
public class SiteSet {

  private final ImmutableMap<String, Site> sites;

  private SiteSet(Iterable<Site> sites) {
    ImmutableMap.Builder<String, Site> map = ImmutableMap.builder();
    for (Site site : sites) {
      map.put(site.getKey(), site);
    }
    this.sites = map.build();
  }

  public static SiteSet create(Map<String, ? extends Theme> themesForSites) {
    List<Site> sites = Lists.newArrayListWithCapacity(themesForSites.size());
    for (Map.Entry<String, ? extends Theme> entry : themesForSites.entrySet()) {
      String key = entry.getKey();
      Theme theme = entry.getValue();
      Site site = new Site(key, theme);
      sites.add(site);
    }
    return new SiteSet(sites);
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
      throw new UnmatchedSiteException(key);
    }
    return site;
  }

  public ImmutableCollection<Site> getSites() {
    return sites.values();
  }

  public ImmutableSet<String> getSiteKeys() {
    return sites.keySet();
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
