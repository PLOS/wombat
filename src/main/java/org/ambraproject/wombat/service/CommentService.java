package org.ambraproject.wombat.service;

import java.io.IOException;
import java.util.Map;

public interface CommentService {

  /**
   * Retrieve a comment and wire in additional data needed for display.
   *
   * @param commentId the ID of a comment
   * @return a representation of the comment's content and metadata
   * @throws IOException
   */
  Map<String, Object> getComment(String commentId) throws IOException;

}
