package org.ambraproject.wombat.service;

import com.google.common.base.Strings;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CitationDownloadServiceImpl implements CitationDownloadService {

  /**
   * Return the fields of the author name, separated by commas, in a particular order.
   * <p>
   * Both the RIS and BibTex formats coincidentally use similar formats of the three name fields delimited by commas,
   * but place the fields in a different order.
   */
  private static String formatAuthorName(Map<String, String> authorData, String... fieldOrder) {
    return Stream.of(fieldOrder)
        .map((String fieldKey) -> authorData.get(fieldKey))
        .filter((String namePart) -> !Strings.isNullOrEmpty(namePart))
        .collect(Collectors.joining(", "));
  }


  private static StringBuilder appendRisCitationLine(StringBuilder builder, String key, String value) {
    return builder.append(Objects.requireNonNull(key)).append("  - ").append(Strings.nullToEmpty(value)).append('\n');
  }

  private static String formatPublicationDate(Map<String, ?> articleMetadata, DateTimeFormatter formatter) {
    String dateString = (String) articleMetadata.get("publicationDate");
    LocalDate date = LocalDate.parse(dateString);
    return formatter.format(date);
  }

  private static String extractJournalTitle(Map<String, ?> articleMetadata) {
    Map<String, ?> journalMetadata = (Map<String, ?>) articleMetadata.get("journal");
    return (String) journalMetadata.get("title");
  }

  private static final DateTimeFormatter RIS_DATE_FORMAT = DateTimeFormatter.ofPattern("YYYY/MM/dd");

  @Override
  public String buildRisCitation(Map<String, ?> articleMetadata) {
    StringBuilder citation = new StringBuilder();
    appendRisCitationLine(citation, "TY", "JOUR");
    appendRisCitationLine(citation, "T1", XmlUtil.extractText((String) articleMetadata.get("title")));

    List<Map<String, String>> authors = (List<Map<String, String>>) articleMetadata.get("authors");
    for (Map<String, String> author : authors) {
      appendRisCitationLine(citation, "A1", formatAuthorName(author, "surnames", "givenNames", "suffix"));
    }

    String journalTitle = extractJournalTitle(articleMetadata);

    appendRisCitationLine(citation, "Y1", formatPublicationDate(articleMetadata, RIS_DATE_FORMAT));
    appendRisCitationLine(citation, "N2", XmlUtil.extractText((String) articleMetadata.get("description")));
    appendRisCitationLine(citation, "JF", journalTitle);
    appendRisCitationLine(citation, "JA", journalTitle);
    appendRisCitationLine(citation, "VL", (String) articleMetadata.get("volume"));
    appendRisCitationLine(citation, "IS", (String) articleMetadata.get("issue"));
    appendRisCitationLine(citation, "UR", (String) articleMetadata.get("url"));
    appendRisCitationLine(citation, "SP", (String) articleMetadata.get("eLocationId"));
    appendRisCitationLine(citation, "EP", null); // Always blank (copied from legacy implementation)
    appendRisCitationLine(citation, "PB", (String) articleMetadata.get("publisherName"));
    appendRisCitationLine(citation, "M3", "doi:" + articleMetadata.get("doi"));
    appendRisCitationLine(citation, "ER", null); // Always blank (copied from legacy implementation)

    return citation.toString();
  }


  private static enum BibtexField {
    AUTHOR("author", null) {
      @Override
      protected String extractValue(Map<String, ?> articleMetadata) {
        List<Map<String, String>> authors = (List<Map<String, String>>) articleMetadata.get("authors");
        return authors.stream()
            .map(authorData -> formatAuthorName(authorData, "surnames", "suffix", "givenNames"))
            .collect(Collectors.joining(" AND "));
      }
    },
    JOURNAL("journal", null) {
      @Override
      protected String extractValue(Map<String, ?> articleMetadata) {
        return extractJournalTitle(articleMetadata);
      }
    },
    PUBLISHER("publisher", "publisherName"),
    TITLE("title", "title") {
      @Override
      protected String extractValue(Map<String, ?> articleMetadata) {
        return XmlUtil.extractText(super.extractValue(articleMetadata));
      }
    },
    YEAR("year", null) {
      @Override
      protected String extractValue(Map<String, ?> articleMetadata) {
        return formatPublicationDate(articleMetadata, YEAR_FORMAT);
      }
    },
    MONTH("month", null) {
      @Override
      protected String extractValue(Map<String, ?> articleMetadata) {
        return formatPublicationDate(articleMetadata, MONTH_FORMAT);
      }
    },
    VOLUME("volume", "volume"),
    URL("url", "url"),
    PAGES("pages", "pages"),
    ABSTRACT("abstract", "description") {
      @Override
      protected String extractValue(Map<String, ?> articleMetadata) {
        return XmlUtil.extractText(super.extractValue(articleMetadata));
      }
    },
    NUMBER("number", "issue"),
    DOI("doi", "doi");

    private static final DateTimeFormatter YEAR_FORMAT = DateTimeFormatter.ofPattern("YYYY");
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("MM");

    private final String citationKey;
    private final String metadataKey;

    BibtexField(String citationKey, String metadataKey) {
      this.citationKey = Objects.requireNonNull(citationKey);
      this.metadataKey = metadataKey; // may be null if extractValue is overridden
    }

    protected String extractValue(Map<String, ?> articleMetadata) {
      return (String) articleMetadata.get(Objects.requireNonNull(metadataKey));
    }
  }

  @Override
  public String buildBibtexCitation(Map<String, ?> articleMetadata) {
    String doiName = (String) articleMetadata.get("doi");
    StringBuilder result = new StringBuilder();
    result.append("@article{").append(doiName).append(",\n");

    result.append(EnumSet.allOf(BibtexField.class).stream()
        .map(field -> String.format("    %s = {%s}",
            field.citationKey, Strings.nullToEmpty(field.extractValue(articleMetadata))))
        .collect(Collectors.joining(",\n")));
    result.append("\n}");

    return result.toString();
  }

}

