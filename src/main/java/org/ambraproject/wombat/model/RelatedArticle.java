package org.ambraproject.wombat.model;

import java.time.LocalDate;

public class RelatedArticle {
  private final String doi;
  private final String title;
  private final LocalDate publicationDate;

  public String getDoi() {
    return doi;
  }

  public String getTitle() {
    return title;
  }

  public LocalDate getPublicationDate() {
    return publicationDate;
  }

  public RelatedArticle(String doi, String title, LocalDate publicationDate) {
    this.doi = doi;
    this.title = title;
    this.publicationDate = publicationDate;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    RelatedArticle that = (RelatedArticle) o;

    if (!doi.equals(that.doi)) return false;
    if (!title.equals(that.title)) return false;
    return publicationDate.equals(that.publicationDate);

  }

  @Override
  public int hashCode() {
    int result = doi.hashCode();
    result = 31 * result + title.hashCode();
    result = 31 * result + publicationDate.hashCode();
    return result;
  }
}
