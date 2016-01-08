package org.ambraproject.wombat.service;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.ambraproject.wombat.config.site.Site;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class CommentValidationServiceImpl implements CommentValidationService {

  @Autowired
  private CommentCensorService commentCensorService;

  private void checkForCensoredWords(Map<String, Object> errors, String errorKey,
                                     Site site, String content) {
    Collection<String> censoredWords = commentCensorService.findCensoredWords(site, content);
    if (!censoredWords.isEmpty()) {
      errors.put(errorKey, censoredWords);
    }
  }

  private static final int COMMENT_TITLE_MAX = 500;
  private static final int COMMENT_BODY_MAX = 64000;
  private static final int CI_STATEMENT_MAX = 5000;

  private void checkLength(Map<String, Object> errors, String errorKey,
                           String value, int maxCharacters) {
    if (value != null && value.length() > maxCharacters) {
      Map<String, Object> errorValue = ImmutableMap.<String, Object>builder()
          .put("length", value.length())
          .put("maxLength", maxCharacters)
          .build();
      errors.put(errorKey, errorValue);
    }
  }

  @Override
  public Map<String, Object> validateComment(Site site,
                                             String title, String body,
                                             boolean hasCompetingInterest, String ciStatement) {
    Map<String, Object> errors = new LinkedHashMap<>();
    if (Strings.isNullOrEmpty(title)) {
      errors.put("missingTitle", true);
    }
    if (Strings.isNullOrEmpty(body)) {
      errors.put("missingBody", true);
    }
    if (hasCompetingInterest && Strings.isNullOrEmpty(ciStatement)) {
      errors.put("missingCi", true);
    }

    checkLength(errors, "titleLength", title, COMMENT_TITLE_MAX);
    checkLength(errors, "bodyLength", body, COMMENT_BODY_MAX);
    checkLength(errors, "ciLength", ciStatement, CI_STATEMENT_MAX);

    checkForCensoredWords(errors, "censoredTitle", site, title);
    checkForCensoredWords(errors, "censoredBody", site, body);
    checkForCensoredWords(errors, "censoredCi", site, ciStatement);

    return errors;
  }

  @Override
  public Map<String, Object> validateFlag(String flagComment) {
    Map<String, Object> errors = Maps.newHashMapWithExpectedSize(1);
    if (Strings.isNullOrEmpty(flagComment)) {
      errors.put("missingComment", true);
    }
    checkLength(errors, "commentLength", flagComment, COMMENT_BODY_MAX);
    return errors;
  }

}
