package org.ambraproject.wombat.config.site;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.ambraproject.wombat.config.RuntimeConfigurationException;
import org.ambraproject.wombat.config.site.url.SiteRequestScheme;
import org.ambraproject.wombat.config.theme.Theme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Site {

  private final String key;
  private final Theme theme;
  private final SiteRequestScheme requestScheme;

  private final String journalKey;
  private final String journalName;

  public Site(String key, Theme theme, SiteRequestScheme requestScheme) {
    this.key = Preconditions.checkNotNull(key);
    this.theme = Preconditions.checkNotNull(theme);
    this.requestScheme = Preconditions.checkNotNull(requestScheme);

    this.journalKey = findJournalKey(theme);
    this.journalName = findJournalName(theme);
  }

  private static final Logger log = LoggerFactory.getLogger(Site.class);

  @VisibleForTesting
  public static final String JOURNAL_KEY_PATH = "journal";
  @VisibleForTesting
  public static final String CONFIG_KEY_FOR_JOURNAL = "journalKey";
  @VisibleForTesting
  public static final String JOURNAL_NAME = "journalName";

  private static String findJournalKey(Theme theme) {
    String journalKey = (String) theme.getConfigMap(JOURNAL_KEY_PATH).get(CONFIG_KEY_FOR_JOURNAL);
    if (Strings.isNullOrEmpty(journalKey)) {
      String message = String.format("The theme %s must provide or inherit a journal key at the path: config/%s",
          theme.getKey(), JOURNAL_KEY_PATH);
      throw new RuntimeConfigurationException(message);
    }
    return journalKey;
  }

  private static String findJournalName(Theme theme) {
    String journalName = (String) theme.getConfigMap(JOURNAL_KEY_PATH).get(JOURNAL_NAME);
    if (Strings.isNullOrEmpty(journalName)) {
      String message = String.format("The theme %s did not provide or inherit a journal name at the path: config/%s",
          theme.getKey(), JOURNAL_KEY_PATH);
      throw new RuntimeException(message);
    }
    return journalName;
  }

  public String getJournalName() {
    return journalName;
  }

  public String getKey() {
    return key;
  }

  public Theme getTheme() {
    return theme;
  }

  public String getJournalKey() {
    return journalKey;
  }

  public SiteRequestScheme getRequestScheme() {
    return requestScheme;
  }

  @Override
  public String toString() {
    return getKey();
  }

  @Override
  public boolean equals(Object o) {
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
  public int hashCode() {
    if (hashValue != 0) return hashValue;
    int result = key.hashCode();
    result = 31 * result + theme.hashCode();
    result = 31 * result + requestScheme.hashCode();
    return hashValue = result;
  }

}
