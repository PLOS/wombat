package org.ambraproject.wombat.identity;

import org.ambraproject.wombat.service.ApiAddress;

import java.util.Objects;

/**
 * A pointer to a particular article ingestion as provided by {@link org.ambraproject.wombat.service.remote.ArticleApi}.
 * To ensure that the DOI is in its canonical form (with regard to URI prefixes and case-sensitivity), the object must
 * be constructed from a string provided by the API, not an end user.
 */
public final class ArticlePointer {

  private final String doi;
  private final int ingestionNumber;

  public ArticlePointer(String doi, int ingestionNumber) {
    this.doi = Objects.requireNonNull(doi);
    this.ingestionNumber = ingestionNumber;
  }

  public String getDoi() {
    return doi;
  }

  public int getIngestionNumber() {
    return ingestionNumber;
  }

  public ApiAddress.Builder asApiAddress() {
    return ApiAddress.builder("articles").embedDoi(doi)
        .addToken("ingestions").addToken(Integer.toString(ingestionNumber));
  }

  @Override
  public boolean equals(Object o) {
    return this == o || o != null && getClass() == o.getClass()
        && ingestionNumber == ((ArticlePointer) o).ingestionNumber
        && doi.equals(((ArticlePointer) o).doi);
  }

  @Override
  public int hashCode() {
    return 31 * doi.hashCode() + ingestionNumber;
  }

}
