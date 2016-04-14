package org.ambraproject.wombat.model;

import com.google.common.base.Preconditions;
import org.ambraproject.wombat.service.ApiAddress;

import java.util.Objects;
import java.util.OptionalInt;

public class ScholarlyWorkId {

  private final String doi;
  private final OptionalInt revisionNumber;

  public ScholarlyWorkId(String doi) {
    this(doi, OptionalInt.empty());
  }

  public ScholarlyWorkId(String doi, Integer revisionNumber) {
    this(doi, (revisionNumber != null) ? OptionalInt.of(revisionNumber) : OptionalInt.empty());
  }

  public ScholarlyWorkId(String doi, OptionalInt revisionNumber) {
    Preconditions.checkArgument(!revisionNumber.isPresent() || revisionNumber.getAsInt() >= 0);
    this.doi = Objects.requireNonNull(doi);
    this.revisionNumber = revisionNumber;
  }

  public String getDoi() {
    return doi;
  }

  public OptionalInt getRevisionNumber() {
    return revisionNumber;
  }

  public ApiAddress appendId(ApiAddress.Builder builder) {
    builder.addToken(doi);
    if (revisionNumber.isPresent()) {
      builder.addParameter("revision", revisionNumber.getAsInt());
    }
    return builder.build();
  }

  @Override
  public String toString() {
    return "ScholarlyWorkId{" +
        "doi='" + doi + '\'' +
        ", revisionNumber=" + revisionNumber +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ScholarlyWorkId that = (ScholarlyWorkId) o;
    return doi.equals(that.doi) && revisionNumber.equals(that.revisionNumber);
  }

  @Override
  public int hashCode() {
    return 31 * doi.hashCode() + revisionNumber.hashCode();
  }
}
