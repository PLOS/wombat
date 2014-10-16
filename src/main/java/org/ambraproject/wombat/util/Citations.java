package org.ambraproject.wombat.util;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

/**
 * Utility methods for building article citations.
 * <p/>
 * Overlaps with some FreeMarker code. It would be good to refactor out duplication.
 */
public class Citations {
  private Citations() {
    throw new AssertionError("Not instantiable");
  }

  private static final int MAX_AUTHORS = 5;

  private static final Joiner COMMA_JOINER = Joiner.on(", ");

  /*
   * TODO: Deduplicate org.ambraproject.wombat.freemarker.AbbreviatedNameDirective
   */
  private static String getAbbreviatedName(Map<String,Object> author) {
    return (String) author.get("fullName"); // TODO: Implement
  }

  /*
   * TODO: Deduplicate org.ambraproject.wombat.freemarker.Iso8601DateDirective
   */
  private static String extractYear(String date) {
    return ""; // TODO Implement
  }

  /*
   * TODO: Deduplicate src/main/webapp/WEB-INF/themes/desktop/ftl/article/citation.ftl
   */
  public static String buildCitation(Map<String, Object> articleMetadata) {
    StringBuilder citation = new StringBuilder();

    List<String> authorNames = Lists.newArrayListWithCapacity(MAX_AUTHORS);
    List<Map<String, Object>> authors = (List<Map<String, Object>>) articleMetadata.get("authors");
    List<String> collaborativeAuthors = (List<String>) articleMetadata.get("collaborativeAuthors");
    for (Map<String, Object> author : authors) {
      if (authorNames.size() >= MAX_AUTHORS) break;
      authorNames.add(getAbbreviatedName(author));
    }
    for (String collaborativeAuthor : collaborativeAuthors) {
      if (authorNames.size() >= MAX_AUTHORS) break;
      authorNames.add(collaborativeAuthor);
    }

    COMMA_JOINER.appendTo(citation, authorNames);
    if (authors.size() + collaborativeAuthors.size() > MAX_AUTHORS) {
      citation.append(", et al.");
    }

    String date = (String) articleMetadata.get("date");
    String year = extractYear(date);
    citation.append(" (").append(year).append(')');

    String title = (String) articleMetadata.get("title"); // TODO: Remove markup
    citation.append(' ').append(title).append('.');

    String journal = (String) articleMetadata.get("journal");
    String volume = (String) articleMetadata.get("volume");
    String issue = (String) articleMetadata.get("issue");
    String eLocationId = (String) articleMetadata.get("eLocationId");
    String pubInfo = String.format("%s %s(%s): %s.", journal, volume, issue, eLocationId);
    citation.append(' ').append(pubInfo);

    String doi = (String) articleMetadata.get("doi");
    citation.append(" doi:").append(doi);

    return citation.toString();
  }

}
