package org.ambraproject.wombat.config;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.io.IOException;

public class Site {

  private String key;
  private Theme theme;
  private String journalKey;
  private String gaCode;

  public Site(String key, Theme theme) {
    this.key = Preconditions.checkNotNull(key);
    this.theme = Preconditions.checkNotNull(theme);
    this.journalKey = findJournalConfigValue(CONFIG_KEY_FOR_JOURNAL, theme, true);
    gaCode = findJournalConfigValue("gaCode", theme, false);
  }

  /**
   * Constructor for applications that do not depend on the theme and already know the journalKey.
   *
   * @param key        key of the journal (value in wombat.yaml)
   * @param journalKey key used for solr (value in journal_key.txt of the corresponding theme)
   */
  public Site(String key, String journalKey) {
    this.key = Preconditions.checkNotNull(key);
    this.journalKey = Preconditions.checkNotNull(journalKey);
  }

  @VisibleForTesting
  static final String JOURNAL_KEY_PATH = "journal";
  @VisibleForTesting
  static final String CONFIG_KEY_FOR_JOURNAL = "journalKey";

  private static String findJournalConfigValue(String key, Theme theme, boolean required) {
    String value;
    try {
      value = (String) theme.getConfigMap(JOURNAL_KEY_PATH).get(key);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    if (required && Strings.isNullOrEmpty(value)) {
      String message = String.format("The theme %s must provide or inherit %s at the path: config/%s",
          theme.getKey(), key, JOURNAL_KEY_PATH);
      throw new RuntimeConfigurationException(message);
    }
    return value;
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

  /**
   * @return the Google Analytics code for this site, or null if none is defined.
   */
  public String getGoogleAnalyticsCode() {
    return gaCode;
  }
}
