package org.ambraproject.wombat.config;

import com.google.common.base.Preconditions;
import com.google.common.io.Closer;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class Site {

  private String key;
  private Theme theme;
  private String journalKey;

  public Site(String key, Theme theme) {
    this.key = Preconditions.checkNotNull(key);
    this.theme = Preconditions.checkNotNull(theme);
    this.journalKey = findJournalKey(key, theme);
  }

  /**
   * Constructor for applications that do not depend on the theme and already know the journalKey.
   *
   * @param key key of the journal (value in wombat.json)
   * @param journalKey key used for solr (value in journal_key.txt of the corresponding theme)
   */
  public Site(String key, String journalKey) {
    this.key = Preconditions.checkNotNull(key);
    this.journalKey = Preconditions.checkNotNull(journalKey);
  }

  private static final String JOURNAL_KEY_PATH = "journal_key.txt";

  private static String findJournalKey(String siteKey, Theme theme) {
    String journalKey;
    try {
      Closer closer = Closer.create();
      try {
        // The built-in root theme provides an empty file for this value, so the stream is never null
        InputStream stream = closer.register(theme.getStaticResource(JOURNAL_KEY_PATH));
        journalKey = IOUtils.toString(stream);
      } catch (Throwable t) {
        throw closer.rethrow(t);
      } finally {
        closer.close();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    journalKey = journalKey.trim();
    if (journalKey.isEmpty()) {
      // We pick up the empty file if the user forgot to provide a journal key in a theme
      String message = String.format("The site \"%s\" must provide a journal key in its theme at the path: %s",
          siteKey, JOURNAL_KEY_PATH);
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
