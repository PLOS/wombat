package org.ambraproject.wombat.model;

public class ArticleCommentFlag {

  private final String creatorAuthId;
  private final String creatorNedId;
  private final String body;
  private final String reasonCode;

  public ArticleCommentFlag(String creatorAuthId, String creatorNedId, String body, String reasonCode) {
    this.creatorAuthId = creatorAuthId;
    this.creatorNedId = creatorNedId;
    this.body = body;
    this.reasonCode = reasonCode;
  }

}
