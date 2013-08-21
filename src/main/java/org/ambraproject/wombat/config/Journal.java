package org.ambraproject.wombat.config;

import com.google.common.base.Preconditions;
import com.google.common.io.Closer;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class Journal {

  private String key;
  private Theme theme;
  private String publicationKey;

  public Journal(String key, Theme theme) {
    this.key = Preconditions.checkNotNull(key);
    this.theme = Preconditions.checkNotNull(theme);
    this.publicationKey = findPublicationKey(key, theme);
  }


  private static final String PUBLICATION_KEY_PATH = "publication_key.txt";

  private static String findPublicationKey(String journalKey, Theme theme) {
    String publicationKey;
    try {
      Closer closer = Closer.create();
      try {
        // The built-in root theme provides an empty file for this value, so the stream is never null
        InputStream stream = closer.register(theme.getStaticResource(PUBLICATION_KEY_PATH));
        publicationKey = IOUtils.toString(stream);
      } catch (Throwable t) {
        throw closer.rethrow(t);
      } finally {
        closer.close();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    publicationKey = publicationKey.trim();
    if (publicationKey.isEmpty()) {
      // We pick up the empty file if the user forgot to provide a publication key in a theme
      String message = String.format("The journal \"%s\" must provide a publication key in its theme at the path: %s",
          journalKey, PUBLICATION_KEY_PATH);
      throw new RuntimeConfigurationException(message);
    }
    return publicationKey;
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
