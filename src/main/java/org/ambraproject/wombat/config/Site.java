package org.ambraproject.wombat.config;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class Site {

  private String key;
  private Theme theme;
  private String journalKey;

  public Site(String key, Theme theme) {
    this.key = Preconditions.checkNotNull(key);
    this.theme = Preconditions.checkNotNull(theme);
    this.journalKey = findJournalKey(theme);
  }

  /**
   * Constructor for applications that do not depend on the theme and already know the journalKey.
   *
   * @param key        key of the journal (value in wombat.json)
   * @param journalKey key used for solr (value in journal_key.txt of the corresponding theme)
   */
  public Site(String key, String journalKey) {
    this.key = Preconditions.checkNotNull(key);
    this.journalKey = Preconditions.checkNotNull(journalKey);
  }

  @VisibleForTesting
  static final String JOURNAL_KEY_PATH = "journal.json";
  @VisibleForTesting
  static final String CONFIG_KEY_FOR_JOURNAL = "journalKey";

  private static String findJournalKey(Theme theme) {
    String journalKey = (String) theme.getConfigMap(JOURNAL_KEY_PATH).get(CONFIG_KEY_FOR_JOURNAL);
    if (Strings.isNullOrEmpty(journalKey)) {
      String message = String.format("The theme %s must provide or inherit a journal key at the path: config/%s",
          theme.getKey(), JOURNAL_KEY_PATH);
      throw new RuntimeConfigurationException(message);
    }
    return journalKey;
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

}
