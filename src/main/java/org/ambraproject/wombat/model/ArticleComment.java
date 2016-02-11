package org.ambraproject.wombat.model;

public class ArticleComment {

  private String articleDoi;
  private String creatorAuthId; // keeping for backward compatibility for now.
  private String creatorNedId;
  private String parentCommentId;
  private String title;
  private String body;
  private String competingInterestStatement;

  public ArticleComment(String articleDoi, String creatorAuthId, String creatorNedId,
      String parentCommentId, String title,
      String body, String competingInterestStatement) {
    this.articleDoi = articleDoi;
    this.creatorAuthId = creatorAuthId;
    this.creatorNedId = creatorNedId;
    this.parentCommentId = parentCommentId;
    this.title = title;
    this.body = body;
    this.competingInterestStatement = competingInterestStatement;
  }
}
