package org.ambraproject.wombat.identity;

import org.ambraproject.wombat.service.ApiAddress;

import java.util.Objects;
import java.util.OptionalInt;

/**
 * A pointer to a particular article ingestion as provided by {@link org.ambraproject.wombat.service.remote.ArticleApi}.
 * To ensure that the DOI is in its canonical form (with regard to URI prefixes and case-sensitivity), the object must
 * be constructed from a string provided by the API, not an end user.
 */
public final class ArticlePointer {

  /**
   * The DOI in its canonical form.
   */
  private final String doi;

  /**
   * The back-end identifier for the version to serve. Always present, regardless of whether the user requested it or
   * not.
   */
  private final int ingestionNumber;

  /**
   * The revision number that was used to find the {@link #ingestionNumber}. This should be empty <em>only</em> if the
   * user requested an ingestion number directly (probably because they were QC'ing it).
   * <p>
   * If the user requested the latest revision by default, this must be populated with that number (as looked up from
   * our back end). Contrast to {@link RequestedDoiVersion}, where the revision number would be empty if the user
   * defaulted to the latest revision.
   */
  private final OptionalInt revisionNumber;

  public ArticlePointer(String doi, int ingestionNumber, OptionalInt revisionNumber) {
    this.doi = Objects.requireNonNull(doi);
    this.ingestionNumber = ingestionNumber;
    this.revisionNumber = revisionNumber;
  }

  public String getDoi() {
    return doi;
  }

  public int getIngestionNumber() {
    return ingestionNumber;
  }

  public OptionalInt getRevisionNumber() {
    return revisionNumber;
  }

  public ApiAddress.Builder asApiAddress() {
    return ApiAddress.builder("articles").embedDoi(doi)
        .addToken("ingestions").addToken(Integer.toString(ingestionNumber));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ArticlePointer that = (ArticlePointer) o;
    if (ingestionNumber != that.ingestionNumber) return false;
    if (!doi.equals(that.doi)) return false;
    return revisionNumber.equals(that.revisionNumber);
  }

  @Override
  public int hashCode() {
    int result = doi.hashCode();
    result = 31 * result + ingestionNumber;
    result = 31 * result + revisionNumber.hashCode();
    return result;
  }
}
