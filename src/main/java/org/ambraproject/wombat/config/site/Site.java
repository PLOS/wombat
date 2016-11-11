package org.ambraproject.wombat.config.site;

import com.google.common.base.Preconditions;
import org.ambraproject.wombat.config.site.url.SiteRequestScheme;
import org.ambraproject.wombat.config.theme.Theme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Site {

  private final String key;
  private final Theme theme;
  private final SiteRequestScheme requestScheme;

  Site(String key, Theme theme, SiteRequestScheme requestScheme) {
    this.key = Preconditions.checkNotNull(key);
    this.theme = Preconditions.checkNotNull(theme);
    this.requestScheme = Preconditions.checkNotNull(requestScheme);
  }

  private static final Logger log = LoggerFactory.getLogger(Site.class);

  public String getKey() {
    return key;
  }

  public Theme getTheme() {
    return theme;
  }

  public SiteRequestScheme getRequestScheme() {
    return requestScheme;
  }

  public boolean isJournalSpecific() {
    return false;
  }

  @Override
  public String toString() {
    return getKey();
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Site site = (Site) o;

    if (!key.equals(site.key)) return false;
    if (!theme.equals(site.theme)) return false;
    if (!requestScheme.equals(site.requestScheme)) return false;

    return true;
  }

  private transient int hashValue;

  @Override
  public final int hashCode() {
    if (hashValue != 0) return hashValue;
    int result = key.hashCode();
    result = 31 * result + theme.hashCode();
    result = 31 * result + requestScheme.hashCode();
    return hashValue = result;
  }

}
