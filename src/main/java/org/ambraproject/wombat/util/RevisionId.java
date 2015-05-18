package org.ambraproject.wombat.util;

import com.google.common.base.Preconditions;
import org.ambraproject.wombat.service.remote.SoaRequest;

/**
 * Identifier for an article revision.
 */
public class RevisionId {

  private final String articleId;
  private final int revisionNumber;

  private RevisionId(String articleId, int revisionNumber) {
    this.articleId = Preconditions.checkNotNull(articleId);
    this.revisionNumber = revisionNumber;
  }

  public static RevisionId create(String articleId, int revisionNumber) {
    return new RevisionId(articleId, revisionNumber);
  }


  public String getArticleId() {
    return articleId;
  }

  public int getRevisionNumber() {
    return revisionNumber;
  }


  public SoaRequest.Builder makeSoaRequest(String path) {
    SoaRequest.Builder builder = SoaRequest.request(path);
    builder.addParameter("id", articleId);
    builder.addParameter("r", Integer.toString(revisionNumber));
    return builder;
  }

  public String getCacheKey() {
    return CacheParams.createKeyHash(articleId, String.valueOf(revisionNumber));
  }


  @Override
  public String toString() {
    // Resembles a URL param for human-friendliness. Don't actually put these into any URLs.
    return String.format("(id = %s, r = %d)", articleId, revisionNumber);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    RevisionId that = (RevisionId) o;

    if (!articleId.equals(that.articleId)) return false;
    if (revisionNumber != that.revisionNumber) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = articleId.hashCode();
    result = 31 * result + revisionNumber;
    return result;
  }
}
