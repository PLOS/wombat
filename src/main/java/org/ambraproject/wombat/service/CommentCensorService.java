package org.ambraproject.wombat.service;

import org.ambraproject.wombat.config.site.Site;

import java.util.Collection;

public interface CommentCensorService {

  /**
   * Detect censored words within user-submitted text.
   *
   * @param site    the site whose list of censored words should be used
   * @param content the text to search
   * @return a collection of censored words that were found (empty collection if content passes)
   */
  public abstract Collection<String> findCensoredWords(Site site, String content);

}
