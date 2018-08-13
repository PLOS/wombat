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

package org.ambraproject.wombat.controller;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.ambraproject.wombat.model.ArticleComment;
import org.ambraproject.wombat.model.ArticleCommentFlag;
import org.ambraproject.wombat.service.CommentService;
import org.ambraproject.wombat.service.CommentValidationService;
import org.ambraproject.wombat.service.HoneypotService;
import org.ambraproject.wombat.service.remote.ApiAddress;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.ambraproject.wombat.service.remote.UserApi;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for rendering an article.
 */
@Controller
public class CommentController extends WombatController {

  private static final Logger log = LoggerFactory.getLogger(CommentController.class);

  @Autowired
  private UserApi userApi;
  @Autowired
  private ArticleApi articleApi;
  @Autowired
  private HoneypotService honeypotService;
  @Autowired
  private CommentValidationService commentValidationService;
  @Autowired
  private CommentService commentService;
  @Autowired
  private ArticleMetadata.Factory articleMetadataFactory;
  @Autowired
  private Gson gson;
  @Autowired
  private RuntimeConfiguration runtimeConfiguration;


  private void addCommentAvailability(Model model) {
    model.addAttribute("areCommentsDisabled", runtimeConfiguration.areCommentsDisabled());
  }

  /**
   * Serves a request for a list of all the root-level comments associated with an article.
   *
   * @param model     data to pass to the view
   * @param site      current site
   * @param articleId specifies the article
   * @return path to the template
   * @throws IOException
   */
  @RequestMapping(name = "articleComments", value = "/article/comments")
  public String renderArticleComments(HttpServletRequest request, Model model, @SiteParam Site site,
                                      RequestedDoiVersion articleId) throws IOException {
    articleMetadataFactory.get(site, articleId)
        .validateVisibility("articleComments")
        .populate(request, model);

    try {
      model.addAttribute("articleComments", commentService.getArticleComments(articleId, site));
    } catch (UserApi.UserApiException e) {
      log.error(e.getMessage(), e);
      model.addAttribute("userApiError", e);
    }

    addCommentAvailability(model);

    return site + "/ftl/article/comment/comments";
  }

  @RequestMapping(name = "articleCommentForm", value = "/article/comments/new")
  public String renderNewCommentForm(HttpServletRequest request, Model model, @SiteParam Site site,
                                     RequestedDoiVersion articleId)
      throws IOException {
    articleMetadataFactory.get(site, articleId)
        .validateVisibility("articleCommentForm")
        .populate(request, model);

    addCommentAvailability(model);
    return site + "/ftl/article/comment/newComment";
  }

  /**
   * @param comment Json representing a comment
   * @return the parent article's doi (latest revision)
   */
  private static String getParentArticleDoiFromComment(Map<String, Object> comment) {
    return (String) (((Map<String, Object>) comment.get("parentArticle")).get("doi"));
  }

  /**
   * @param commentDoi
   * @return Json representing a comment (no user data included)
   * @throws IOException
   */
  private Map<String, Object> getComment(String commentDoi) throws IOException {
    return articleApi.requestObject(ApiAddress.builder("comments").embedDoi(commentDoi).build(), Map.class);
  }

  /**
   * Serves a request for an expanded view of a single comment and any replies.
   *
   * @param model      data to pass to the view
   * @param site       current site
   * @param commentDoi specifies the comment
   * @return path to the template
   * @throws IOException
   */
  @RequestMapping(name = "articleCommentTree", value = "/article/comment")
  public String renderArticleCommentTree(HttpServletRequest request, Model model, @SiteParam Site site,
                                         @RequestParam("id") String commentDoi) throws IOException {
    requireNonemptyParameter(commentDoi);
    Map<String, Object> comment;
    try {
      comment = commentService.getComment(commentDoi, site);
    } catch (CommentService.CommentNotFoundException e) {
      throw new NotFoundException(e);
    } catch (UserApi.UserApiException e) {
      log.error(e.getMessage(), e);
      model.addAttribute("userApiError", e);

      // Get a copy of the comment that is not populated with userApi data.
      // This articleApi call is redundant to one that commentService.getComment would have made before throwing.
      // TODO: Prevent extra articleApi call
      comment = getComment(commentDoi);
    }

    RequestedDoiVersion doi = RequestedDoiVersion.of(getParentArticleDoiFromComment(comment));

    articleMetadataFactory.get(site, doi).validateVisibility("articleCommentTree")
        .populate(request, model);

    model.addAttribute("comment", comment);
    addCommentAvailability(model);
    return site + "/ftl/article/comment/comment";
  }


  private void checkCommentsAreEnabled() {
    if (runtimeConfiguration.areCommentsDisabled()) {
      // TODO: Need a special exception and handler to produce a 400-series response instead of 500?
      throw new RuntimeException("Posting of comments is disabled");
    }
  }

  /**
   * @param parentArticleDoi null if a reply to another comment
   * @param parentCommentUri null if a direct reply to an article
   */
  @RequestMapping(name = "postComment", method = RequestMethod.POST, value = "/article/comments/new")
  @ResponseBody
  public Object receiveNewComment(HttpServletRequest request,
                                  @SiteParam Site site,
                                  @RequestParam("commentTitle") String commentTitle,
                                  @RequestParam("comment") String commentBody,
                                  @RequestParam("isCompetingInterest") boolean hasCompetingInterest,
                                  @RequestParam(value = "authorEmailAddress", required = false) String authorEmailAddress,
                                  @RequestParam(value = "authorName", required = false) String authorName,
                                  @RequestParam(value = "authorPhone", required = false) String authorPhone,
                                  @RequestParam(value = "authorAffiliation", required = false) String authorAffiliation,
                                  @RequestParam(value = "ciStatement", required = false) String ciStatement,
                                  @RequestParam(value = "target", required = false) String parentArticleDoi,
                                  @RequestParam(value = "inReplyTo", required = false) String parentCommentUri)
      throws IOException {

    if (honeypotService.checkHoneypot(request, authorPhone, authorAffiliation)) {
      return ImmutableMap.of("status", "success");
    }

    checkCommentsAreEnabled();

    Map<String, Object> validationErrors = commentValidationService.validateComment(site,
        commentTitle, commentBody, hasCompetingInterest, ciStatement);

    if (!validationErrors.isEmpty()) {
      return ImmutableMap.of("validationErrors", validationErrors);
    }

    if (parentArticleDoi == null) {
      Map<String, Object> comment = getComment(parentCommentUri);
      parentArticleDoi = getParentArticleDoiFromComment(comment);
    }

    ApiAddress address = ApiAddress.builder("articles").embedDoi(parentArticleDoi).addToken("comments").build();

    String authId = request.getRemoteUser();
    final String creatorUserId = authId == null ? null : userApi.getUserIdFromAuthId(authId);
    ArticleComment comment = new ArticleComment(parentArticleDoi, creatorUserId,
        parentCommentUri, commentTitle, commentBody, ciStatement, authorEmailAddress, authorName);

    HttpResponse response = articleApi.postObject(address, comment);
    String responseJson = EntityUtils.toString(response.getEntity());
    Map<String, Object> commentJson = gson.fromJson(responseJson, HashMap.class);
    return ImmutableMap.of("createdCommentUri", commentJson.get("commentUri"));
  }

  @RequestMapping(name = "postCommentFlag", method = RequestMethod.POST, value = "/article/comments/flag")
  @ResponseBody
  public Object receiveCommentFlag(HttpServletRequest request,
                                   @RequestParam("reasonCode") String reasonCode,
                                   @RequestParam("comment") String flagCommentBody,
                                   @RequestParam("target") String targetCommentDoi)
      throws IOException {
    checkCommentsAreEnabled();

    Map<String, Object> validationErrors = commentValidationService.validateFlag(flagCommentBody);
    if (!validationErrors.isEmpty()) {
      return ImmutableMap.of("validationErrors", validationErrors);
    }

    String authId = request.getRemoteUser();
    final String creatorUserId = authId == null ? null : userApi.getUserIdFromAuthId(authId);
    ArticleCommentFlag flag = new ArticleCommentFlag(creatorUserId, flagCommentBody, reasonCode);

    Map<String, Object> comment = getComment(targetCommentDoi);
    String parentArticleDoi = getParentArticleDoiFromComment(comment);

    ApiAddress address = ApiAddress.builder("articles").embedDoi(parentArticleDoi).addToken("comments")
        .embedDoi(targetCommentDoi).addToken("flags").build();

    articleApi.postObject(address, flag);
    return ImmutableMap.of(); // the "201 CREATED" status is all the AJAX client needs
  }

}
