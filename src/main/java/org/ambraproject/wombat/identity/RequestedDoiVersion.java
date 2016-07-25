package org.ambraproject.wombat.identity;

import com.google.common.base.Preconditions;

import java.util.Objects;
import java.util.OptionalInt;

/**
 * A DOI requested by the end user, which may indicate a version, either by revision number or ingestion number. The DOI
 * is in unsanitized form (with regard to both URI prefixes and case-sensitivity) and may not exactly match keys used by
 * the service API until it has been resolved in an API request..
 */
public class RequestedDoiVersion {

  private final String doi;
  private final OptionalInt revisionNumber;
  private final OptionalInt ingestionNumber;

  private RequestedDoiVersion(String doi, OptionalInt revisionNumber, OptionalInt ingestionNumber) {
    Preconditions.checkArgument(!(revisionNumber.isPresent() && ingestionNumber.isPresent()));
    Preconditions.checkArgument(!revisionNumber.isPresent() || revisionNumber.getAsInt() >= 0);
    Preconditions.checkArgument(!ingestionNumber.isPresent() || ingestionNumber.getAsInt() >= 0);

    this.doi = Objects.requireNonNull(doi);
    this.revisionNumber = revisionNumber;
    this.ingestionNumber = ingestionNumber;
  }

  public static RequestedDoiVersion of(String doi) {
    return new RequestedDoiVersion(doi, OptionalInt.empty(), OptionalInt.empty());
  }

  public static RequestedDoiVersion ofRevision(String doi, int revisionNumber) {
    return new RequestedDoiVersion(doi, OptionalInt.of(revisionNumber), OptionalInt.empty());
  }

  public static RequestedDoiVersion ofIngestion(String doi, int ingestionNumber) {
    return new RequestedDoiVersion(doi, OptionalInt.empty(), OptionalInt.of(ingestionNumber));
  }


  public String getDoi() {
    return doi;
  }

  public OptionalInt getRevisionNumber() {
    return revisionNumber;
  }

  public OptionalInt getIngestionNumber() {
    return ingestionNumber;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    RequestedDoiVersion that = (RequestedDoiVersion) o;

    if (!doi.equals(that.doi)) return false;
    if (!revisionNumber.equals(that.revisionNumber)) return false;
    return ingestionNumber.equals(that.ingestionNumber);

  }

  @Override
  public int hashCode() {
    int result = doi.hashCode();
    result = 31 * result + revisionNumber.hashCode();
    result = 31 * result + ingestionNumber.hashCode();
    return result;
  }
}
