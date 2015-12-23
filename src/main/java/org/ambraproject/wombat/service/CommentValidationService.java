package org.ambraproject.wombat.service;

import org.ambraproject.wombat.config.site.Site;

import java.util.Map;

public interface CommentValidationService {

  Map<String, Object> validate(Site site, String title, String body, boolean hasCompetingInterest, String ciStatement);

}
