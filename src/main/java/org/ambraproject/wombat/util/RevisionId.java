package org.ambraproject.wombat.util;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.ambraproject.wombat.service.remote.SoaRequest;

/**
 * Identifier for an article revision. May either identify a revision explicitly, or refer only to an article key and
 * mean the latest revision.
 */
public class RevisionId {

  private final String articleId;
  private final Optional<Integer> revisionNumber;

  private RevisionId(String articleId, Integer revisionNumber) {
    this.articleId = Preconditions.checkNotNull(articleId);

    Preconditions.checkArgument(revisionNumber == null || isValidRevisionNumber(revisionNumber));
    this.revisionNumber = Optional.fromNullable(revisionNumber);
  }

  private static boolean isValidRevisionNumber(int revisionNumber) {
    // Subject to change? It's a UX concern whether these are numbered from 0 or 1.
    return revisionNumber > 0;
  }

  public static RevisionId create(String articleId) {
    return new RevisionId(articleId, null);
  }

  public static RevisionId create(String articleId, int revisionNumber) {
    return new RevisionId(articleId, revisionNumber);
  }

  public static RevisionId parse(String articleId, String revisionNumber) {
    if (Strings.isNullOrEmpty(articleId)) {
      throw new InvalidRevisionIdException("articleId not provided");
    }

    Integer numberValue;
    if (Strings.isNullOrEmpty(revisionNumber)) {
      numberValue = null;
    } else {
      try {
        numberValue = Integer.parseInt(revisionNumber);
      } catch (NumberFormatException e) {
        throw new InvalidRevisionIdException("revisionNumber is not a number", e);
      }

      if (!isValidRevisionNumber(numberValue)) {
        throw new InvalidRevisionIdException("revisionNumber is not a valid number");
      }
    }

    return new RevisionId(articleId, numberValue);
  }

  public static class InvalidRevisionIdException extends RuntimeException {
    private InvalidRevisionIdException(String message) {
      super(message);
    }

    private InvalidRevisionIdException(String message, Throwable cause) {
      super(message, cause);
    }
  }


  public String getArticleId() {
    return articleId;
  }

  public Optional<Integer> getRevisionNumber() {
    return revisionNumber;
  }


  public SoaRequest.Builder makeSoaRequest(String path) {
    SoaRequest.Builder builder = SoaRequest.request(path);
    builder.addParameter("id", articleId);
    if (revisionNumber.isPresent()) {
      builder.addParameter("r", revisionNumber.get().toString());
    }
    return builder;
  }

  public String getCacheKey() {
    return CacheParams.createKeyHash(articleId, String.valueOf(revisionNumber.orNull()));
  }


  @Override
  public String toString() {
    // Resembles a URL param for human-friendliness. Don't actually put these into any URLs.
    return String.format("(id = %s, r = %s)", articleId, revisionNumber.orNull());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    RevisionId that = (RevisionId) o;

    if (!articleId.equals(that.articleId)) return false;
    if (!revisionNumber.equals(that.revisionNumber)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = articleId.hashCode();
    result = 31 * result + revisionNumber.hashCode();
    return result;
  }
}
