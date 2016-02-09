package org.ambraproject.wombat.service;

import org.ambraproject.wombat.service.remote.ArticleApi;
import org.ambraproject.wombat.service.remote.UserApi;
import org.plos.ned_client.model.IndividualComposite;
import org.plos.ned_client.model.Individualprofile;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommentServiceImpl implements CommentService {

  @Autowired
  private ArticleApi articleApi;
  @Autowired
  private UserApi userApi;

  private static final String REPLIES_KEY = "replies";
  private static final String CREATOR_KEY = "creator";

  /**
   * Iterate over a comment and all of its nested replies, applying a modification to each.
   * <p>
   * A new, deep copy of the map is returned. The map passed as an argument is not modified.
   *
   * @param comment the root comment
   * @return a deep copy with the modification applied to all
   */
  private Map<String, Object> modifyCommentTree(Map<String, Object> comment) {
    Map<String, Object> modified = modifyComment(new HashMap<>(comment));

    List<Map<String, Object>> replies = (List<Map<String, Object>>) modified.remove(REPLIES_KEY);
    List<Map<String, Object>> modifiedReplies = replies.stream()
        .map(this::modifyCommentTree) // recursion (terminal case is when replies is empty)
        .collect(Collectors.toList());
    modified.put(REPLIES_KEY, modifiedReplies);

    return modified;
  }

  /**
   * Modify a raw comment with additional data as needed for display.
   *
   * @param comment raw comment metadata as provided by {@link ArticleApi}
   * @return a copy of the same comment metadata with additional display-tier details added
   */
  private Map<String, Object> modifyComment(Map<String, Object> comment) {
    CommentFormatting.addFormattingFields(comment);
    addCreatorData(comment);
    return comment;
  }

  /**
   * Fetch data about a user from NED and put it in the comment, replacing the NED ID.
   */
  private void addCreatorData(Map<String, Object> comment) {
    Map<String, Object> creator = (Map<String, Object>) comment.remove(CREATOR_KEY);
    String nedId = creator.get("userId").toString();

    IndividualComposite individual;
    try {
      individual = userApi.requestObject("individuals/" + nedId, IndividualComposite.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    Individualprofile profile = individual.getIndividualprofiles().get(0); // TODO?
    comment.put(CREATOR_KEY, profile);
  }

  @Override
  public Map<String, Object> getComment(String commentId) throws IOException {
    Map<String, Object> comment;
    try {
      comment = articleApi.requestObject(String.format("comments/" + commentId), Map.class);
    } catch (EntityNotFoundException enfe) {
      throw new CommentNotFoundException(commentId, enfe);
    }

    return modifyCommentTree(comment);
  }

  @Override
  public List<Map<String, Object>> getArticleComments(String articleDoi) throws IOException {
    List<Map<String, Object>> comments = articleApi.requestObject(String.format("articles/%s?comments", articleDoi), List.class);
    return comments.stream()
        .map(this::modifyCommentTree)
        .collect(Collectors.toList());
  }

}
