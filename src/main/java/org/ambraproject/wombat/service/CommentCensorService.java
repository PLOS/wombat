package org.ambraproject.wombat.service;

import org.ambraproject.wombat.config.site.Site;

import java.util.Collection;

public interface CommentCensorService {

  public abstract Collection<String> findCensoredWords(Site site, String content);

}
