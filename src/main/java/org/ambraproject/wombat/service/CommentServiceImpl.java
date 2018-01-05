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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.ambraproject.wombat.service.remote.ApiAddress;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.ambraproject.wombat.service.remote.UserApi;
import org.plos.ned_client.model.IndividualComposite;
import org.plos.ned_client.model.Individualprofile;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
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
  private static final String ARTICLE_KEY = "article";
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
        .findFirst() // Multiple hits may be possible. Using the first is recommended for this API.
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

  private String requestArticleTitle(String articleDoi) {
    try {
      List<Object> revisions = articleApi.requestObject(ApiAddress.builder("articles")
              .embedDoi(articleDoi).addToken("revisions").build(), ArrayList.class);
      if (!revisions.isEmpty()) {
        Map<String, Object> latest = (Map<String, Object>) revisions.get(revisions.size() - 1);
        String title = (String) ((Map<String, Object>) latest.get("ingestion")).get("title");
        return title.replaceAll("\\<.*?>","");
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return articleDoi;
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

  private void addArticleTitle(Collection<Map<String, Object>> rootComments) {
    // Gather up all distinct article DOIs in the forest
    Set<String> articleDois = Collections.synchronizedSet(new HashSet<>());
    rootComments.forEach(rootComment -> modifyCommentTree(rootComment, comment -> {
      Map<String, Object> article = (Map<String, Object>) comment.get(ARTICLE_KEY);
      articleDois.add(article.get("doi").toString());
    }));

    // For each distinct article DOI, make a remote request for the article title
    // (this is the bottleneck that we want to parallelize)
    Map<String, String> titles = articleDois.parallelStream()
        .collect(Collectors.toMap(Function.identity(), this::requestArticleTitle));

    // Insert the title into each comment
    rootComments.forEach(rootComment -> modifyCommentTree(rootComment, comment -> {
      Map<String, Object> article = (Map<String, Object>) comment.get(ARTICLE_KEY);
      String doi = article.get("doi").toString();
      String title = Objects.requireNonNull(titles.get(doi));
      article.put("title", title);
    }));
  }

  @Override
  public Map<String, Object> getComment(String commentDoi, Site site) throws IOException {
    Map<String, Object> comment;
    try {
      final ApiAddress commentUrl = ApiAddress.builder("comments").embedDoi(commentDoi).build();
      comment = articleApi.requestObject(commentUrl, Map.class);
    } catch (EntityNotFoundException enfe) {
      throw new CommentNotFoundException(commentDoi, enfe);
    }

    modifyCommentTree(comment, CommentFormatting::addFormattingFields);
    if (site.getType() == null || !site.getType().equals("preprints")) {
      addCreatorData(ImmutableList.of(comment));
    }
    return comment;
  }

  @Override
  public List<Map<String, Object>> getArticleComments(RequestedDoiVersion articleId, Site site) throws IOException {
    final ApiAddress commentsUrl = ApiAddress.builder("articles").embedDoi(articleId.getDoi())
        .addToken("comments").build();
    List<Map<String, Object>> comments = articleApi.requestObject(commentsUrl, List.class);
    comments.forEach(comment -> modifyCommentTree(comment, CommentFormatting::addFormattingFields));
    if (site.getType() == null || !site.getType().equals("preprints")) {
      addCreatorData(comments);
    }
    return comments;
  }

  @Override
  public List<Map<String, Object>> getRecentJournalComments(String journalKey, int count) throws IOException {
    Preconditions.checkArgument(count >= 0);
    ApiAddress requestAddress = ApiAddress.builder("comments")
        .addParameter("journal", journalKey).addParameter("limit", count)
        .build();
    List<Map<String, Object>> comments = articleApi.requestObject(requestAddress, List.class);
    comments.forEach(comment -> modifyCommentTree(comment, CommentFormatting::addFormattingFields));
    addArticleTitle(comments);
    addCreatorData(comments);
    return comments;
  }

}
