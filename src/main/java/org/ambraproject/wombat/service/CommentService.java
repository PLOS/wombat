package org.ambraproject.wombat.service;

import org.ambraproject.wombat.identity.RequestedDoiVersion;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface CommentService {

  /**
   * Retrieve a comment and wire in additional data needed for display.
   *
   * @param commentId the ID of a comment
   * @return a representation of the comment's content and metadata
   * @throws IOException
   * @throws CommentNotFoundException if the comment does not exist
   */
  Map<String, Object> getComment(String commentId) throws IOException;

  /**
   * Retrieve all comments belonging to a single parent article and wire in additional data needed for display.
   *
   * @param articleId the article DOI
   * @return a list of representations of the comments' content and metadata
   * @throws IOException
   * @throws EntityNotFoundException if the article does not exist
   */
  List<Map<String, Object>> getArticleComments(RequestedDoiVersion articleId) throws IOException;

  /**
   * Retrieve the most recent comments among all articles from a journal and wire in additional data needed for
   * display.
   *
   * @param journalKey the journal's key
   * @param count      the number of comments to retrieve
   * @return a list of size equal to {@code count} (or less, if there aren't that many comments) containing the most
   * recent comments from the journal
   * @throws IOException
   */
  List<Map<String, Object>> getRecentJournalComments(String journalKey, int count) throws IOException;

  public static class CommentNotFoundException extends RuntimeException {
    public CommentNotFoundException(String commentId, EntityNotFoundException cause) {
      super("No comment with ID: " + commentId, cause);
    }
  }

}
