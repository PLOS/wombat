/*
 * Copyright (c) 2017 Public Library of Science
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

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
  private final String type;
  private final SiteRequestScheme requestScheme;

  private final String journalKey;
  private final String journalName;

  public Site(String key, Theme theme, SiteRequestScheme requestScheme, String type) {
    this.key = Preconditions.checkNotNull(key);
    this.theme = Preconditions.checkNotNull(theme);
    this.type = type;
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

  public String getType() { return type; }

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
