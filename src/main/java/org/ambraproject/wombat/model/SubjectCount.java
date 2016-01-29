package org.ambraproject.wombat.model;

/**
 * Simple class wrapping the category -> count map returned by Solr subject searches.
 * Also allows tracking of article counts per subject.
 */
public class SubjectCount implements Comparable<SubjectCount> {
  public String subject;
  public Long articleCount;
  public Long childCount;

  public SubjectCount(String subject, Long articleCount) {
    this.subject = subject;
    this.articleCount = articleCount;
  }

  public SubjectCount(String subject, Long articleCount, Long childCount) {
    this.subject = subject;
    this.articleCount = articleCount;
    this.childCount = childCount;
  }

  public String getSubject() {
    return subject;
  }

   public Long getChildCount() {
    return childCount;
  }

  public Long getArticleCount() {
    return articleCount;
  }

  public int compareTo(SubjectCount that) {
    return subject.compareTo(that.subject);
  }
}
