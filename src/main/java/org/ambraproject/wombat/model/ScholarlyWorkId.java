package org.ambraproject.wombat.model;

import java.util.OptionalInt;

public class ScholarlyWorkId {

  private final String doi;
  private final OptionalInt revisionNumber;

  public ScholarlyWorkId(String doi) {
    this(doi, OptionalInt.empty());
  }

  public ScholarlyWorkId(String doi, OptionalInt revisionNumber) {
    this.doi = doi;
    this.revisionNumber = revisionNumber;
  }

  public String getDoi() {
    return doi;
  }

  public OptionalInt getRevisionNumber() {
    return revisionNumber;
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
