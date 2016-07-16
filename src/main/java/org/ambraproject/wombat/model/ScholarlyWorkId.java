package org.ambraproject.wombat.model;

import com.google.common.base.Preconditions;

import java.util.Objects;
import java.util.OptionalInt;

public class ScholarlyWorkId {

  private final String doi;
  private final OptionalInt revisionNumber;
  private final OptionalInt ingestionNumber;

  private ScholarlyWorkId(String doi, OptionalInt revisionNumber, OptionalInt ingestionNumber) {
    Preconditions.checkArgument(!(revisionNumber.isPresent() && ingestionNumber.isPresent()));
    Preconditions.checkArgument(!revisionNumber.isPresent() || revisionNumber.getAsInt() >= 0);
    Preconditions.checkArgument(!ingestionNumber.isPresent() || ingestionNumber.getAsInt() >= 0);

    this.doi = Objects.requireNonNull(doi);
    this.revisionNumber = revisionNumber;
    this.ingestionNumber = ingestionNumber;
  }

  public static ScholarlyWorkId of(String doi) {
    return new ScholarlyWorkId(doi, OptionalInt.empty(), OptionalInt.empty());
  }

  public static ScholarlyWorkId ofRevision(String doi, int revisionNumber) {
    return new ScholarlyWorkId(doi, OptionalInt.of(revisionNumber), OptionalInt.empty());
  }

  public static ScholarlyWorkId ofIngestion(String doi, int ingestionNumber) {
    return new ScholarlyWorkId(doi, OptionalInt.empty(), OptionalInt.of(ingestionNumber));
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

    ScholarlyWorkId that = (ScholarlyWorkId) o;

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
