/*
 * Copyright (c) 2017 Public Library of Science
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package org.ambraproject.wombat.service;

import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.identity.RequestedDoiVersion;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface CommentService {

  /**
   * Retrieve a comment and wire in additional data needed for display.
   *
   * @param commentDoi the ID of a comment
   * @param site the site
   * @return a representation of the comment's content and metadata
   * @throws IOException
   * @throws CommentNotFoundException if the comment does not exist
   */
  Map<String, Object> getComment(String commentDoi, Site site) throws IOException;

  /**
   * Retrieve all comments belonging to a single parent article and wire in additional data needed for display.
   *
   * @param articleId the article DOI
   * @param site the site
   * @return a list of representations of the comments' content and metadata
   * @throws IOException
   * @throws EntityNotFoundException if the article does not exist
   */
  List<Map<String, Object>> getArticleComments(RequestedDoiVersion articleId, Site site) throws IOException;

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
