package org.ambraproject.wombat.model;

public class RelatedArticle {
  private String doi;
  private String title;

  public String getDoi() {
    return doi;
  }

  public void setDoi(String doi) {
    this.doi = doi;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public RelatedArticle(String doi, String title) {
    this.doi = doi;
    this.title = title;
  }
}
