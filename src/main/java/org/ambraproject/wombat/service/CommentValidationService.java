package org.ambraproject.wombat.service;

import org.ambraproject.wombat.config.site.Site;

import java.util.Map;

public interface CommentValidationService {

  /**
   * Validate a comment submission.
   * <p>
   * The error keys are mapped to front-end code. The map's values describe the error in a way that varies between keys.
   * The value is {@code true} if there is nothing to describe.
   * <p>
   * This is pretty tightly coupled to the front end. Refactoring welcome.
   *
   * @return validation errors, or an empty map if comment passes
   */
  public abstract Map<String, Object> validateComment(Site site, String title, String body,
                                                      boolean hasCompetingInterest, String ciStatement);

  public abstract Map<String, Object> validateFlag(String flagComment);

}
