package org.ambraproject.wombat.model;

public class ArticleCommentFlag {

  private final String creatorUserId;
  private final String body;
  private final String reasonCode;

  public ArticleCommentFlag(String creatorUserId, String body, String reasonCode) {
    this.creatorUserId = creatorUserId;
    this.body = body;
    this.reasonCode = reasonCode;
  }

}
