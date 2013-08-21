package org.ambraproject.wombat.config;

import com.google.common.base.Preconditions;

public class Journal {

  private String key;
  private Theme theme;
  private String publicationKey;

  public Journal(String key, Theme theme) {
    this.key = Preconditions.checkNotNull(key);
    this.theme = Preconditions.checkNotNull(theme);
    this.publicationKey = findPublicationKey(theme);
  }

  private static String findPublicationKey(Theme theme) {
    return null; // TODO Implement
  }


  public String getKey() {
    return key;
  }

  public Theme getTheme() {
    return theme;
  }

  public String getPublicationKey() {
    return publicationKey;
  }

}
