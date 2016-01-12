package org.ambraproject.wombat.model;

public class ArticleCommentFlag {

  private final String creatorAuthId;
  private final String body;
  private final String reasonCode;

  public ArticleCommentFlag(String creatorAuthId, String body, String reasonCode) {
    this.creatorAuthId = creatorAuthId;
    this.body = body;
    this.reasonCode = reasonCode;
  }

}
