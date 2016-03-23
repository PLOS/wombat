package org.ambraproject.wombat.model;

public class ArticleComment {

  private String articleDoi;
  private String creatorUserId;
  private String parentCommentId;
  private String title;
  private String body;
  private String competingInterestStatement;

  public ArticleComment(String articleDoi, String creatorUserId, String parentCommentId, String title,
      String body, String competingInterestStatement) {
    this.articleDoi = articleDoi;
    this.creatorUserId = creatorUserId;
    this.parentCommentId = parentCommentId;
    this.title = title;
    this.body = body;
    this.competingInterestStatement = competingInterestStatement;
  }
}
