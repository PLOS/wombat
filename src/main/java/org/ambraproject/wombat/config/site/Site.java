package org.ambraproject.wombat.config.site;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.ambraproject.wombat.config.RuntimeConfigurationException;
import org.ambraproject.wombat.config.theme.Theme;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class Site {

  private final String key;
  private final Theme theme;
  private final String journalKey;
  private final SiteRequestPredicate requestPredicate;

  public Site(String key, Theme theme, SiteRequestPredicate requestPredicate) {
    this.requestPredicate = requestPredicate;
    this.key = Preconditions.checkNotNull(key);
    this.theme = Preconditions.checkNotNull(theme);
    this.journalKey = findJournalKey(theme);
  }

  @VisibleForTesting
  public static final String JOURNAL_KEY_PATH = "journal";
  @VisibleForTesting
  public static final String CONFIG_KEY_FOR_JOURNAL = "journalKey";

  private static String findJournalKey(Theme theme) {
    String journalKey;
    try {
      journalKey = (String) theme.getConfigMap(JOURNAL_KEY_PATH).get(CONFIG_KEY_FOR_JOURNAL);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
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

  public boolean isFor(HttpServletRequest request) {
    return requestPredicate.isForSite(Preconditions.checkNotNull(request));
  }

}
