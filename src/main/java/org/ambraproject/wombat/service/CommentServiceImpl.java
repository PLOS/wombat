package org.ambraproject.wombat.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.ambraproject.wombat.model.ScholarlyWorkId;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.ambraproject.wombat.service.remote.UserApi;
import org.plos.ned_client.model.IndividualComposite;
import org.plos.ned_client.model.Individualprofile;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CommentServiceImpl implements CommentService {

  @Autowired
  private ArticleApi articleApi;
  @Autowired
  private UserApi userApi;

  private static final String REPLIES_KEY = "replies";
  private static final String CREATOR_KEY = "creator";
  //private static final String USER_PROFILE_ID_KEY = "userProfileID";

  /**
   * Iterate over a comment and all of its nested replies, applying a modification in place to each.
   *
   * @param comment      the root comment
   * @param modification a visitor that modifies each map of comment metadata in the tree
   */
  private static void modifyCommentTree(Map<String, Object> comment,
                                        Consumer<Map<String, Object>> modification) {
    modification.accept(comment);

    List<Map<String, Object>> replies = (List<Map<String, Object>>) comment.get(REPLIES_KEY);
    if (replies != null) {
      replies.forEach(reply -> modifyCommentTree(reply, modification)); // recursion (terminal case is when replies is empty)
    }
  }

  /**
   * The value of {@link Individualprofile#getSource()} that indicates that the profile is in use by this system.
   */
  private static final String AMBRA_SOURCE = "Ambra";

  /**
   * Extract the profile in use by this system.
   */
  // TODO: Move to a public service or util class if needed elsewhere
  private static Individualprofile getAmbraProfile(IndividualComposite individualComposite) {
    return individualComposite.getIndividualprofiles().stream()
        .filter((Individualprofile profile) -> AMBRA_SOURCE.equals(profile.getSource()))
        .findFirst()
        .orElseThrow(() -> new RuntimeException(
            "An IndividualComposite does not have an Individualprofile with source named " + AMBRA_SOURCE));
  }

  private Individualprofile requestProfile(String userId) {
    IndividualComposite individual;
    try {
      individual = userApi.requestObject(ApiAddress.builder("individuals").addToken(userId).build(),
          IndividualComposite.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return getAmbraProfile(individual);
  }

  /**
   * Add display data related to comment creators to all comments. The argument may be a "forest" of multiple roots,
   * each with a tree of replies. Creator data will be added to all comments in each tree.
   *
   * @param rootComments a collection of root-level comments
   */
  private void addCreatorData(Collection<Map<String, Object>> rootComments) {
    // Gather up all distinct user IDs in the forest
    Set<String> userIds = Collections.synchronizedSet(new HashSet<>());
    rootComments.forEach(rootComment -> modifyCommentTree(rootComment, comment -> {
      Map<String, Object> creator = (Map<String, Object>) comment.get(CREATOR_KEY);
      userIds.add(creator.get("userId").toString());
    }));

    // For each distinct user ID, make a remote request for the profile data
    // (this is the bottleneck that we want to parallelize)
    Map<String, Individualprofile> profiles = userIds.parallelStream()
        .collect(Collectors.toMap(Function.identity(), this::requestProfile));

    // Insert the profile data into each comment
    rootComments.forEach(rootComment -> modifyCommentTree(rootComment, comment -> {
      Map<String, Object> creator = (Map<String, Object>) comment.remove(CREATOR_KEY);
      String userId = creator.get("userId").toString();
      Individualprofile profile = Objects.requireNonNull(profiles.get(userId));
      comment.put(CREATOR_KEY, profile);
    }));
  }

  @Override
  public Map<String, Object> getComment(String commentId) throws IOException {
    Map<String, Object> comment;
    try {
      comment = articleApi.requestObject(ApiAddress.builder("comments").addToken(commentId).build(), Map.class);
    } catch (EntityNotFoundException enfe) {
      throw new CommentNotFoundException(commentId, enfe);
    }

    modifyCommentTree(comment, CommentFormatting::addFormattingFields);
    addCreatorData(ImmutableList.of(comment));
    return comment;
  }

  @Override
  public List<Map<String, Object>> getArticleComments(ScholarlyWorkId articleId) throws IOException {
    List<Map<String, Object>> comments = articleApi.requestObject(
        ApiAddress.builder("articles").embedDoi(articleId.getDoi()).addToken("comments").build(),
        List.class);
    comments.forEach(comment -> modifyCommentTree(comment, CommentFormatting::addFormattingFields));
    addCreatorData(comments);
    return comments;
  }

  @Override
  public List<Map<String, Object>> getRecentJournalComments(String journalKey, int count) throws IOException {
    Preconditions.checkArgument(count >= 0);
    ApiAddress requestAddress = ApiAddress.builder("journals").addToken(journalKey)
        .addParameter("comments").addParameter("limit", count)
        .build();
    List<Map<String, Object>> comments = articleApi.requestObject(requestAddress, List.class);
    comments.forEach(comment -> modifyCommentTree(comment, CommentFormatting::addFormattingFields));
    addCreatorData(comments);
    return comments;
  }

}
