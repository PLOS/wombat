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

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.ambraproject.wombat.config.site.RequestMappingContextDictionary;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.site.url.Link;
import org.ambraproject.wombat.identity.ArticlePointer;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.ambraproject.wombat.model.ArticleComment;
import org.ambraproject.wombat.model.ArticleCommentFlag;
import org.ambraproject.wombat.model.EmailMessage;
import org.ambraproject.wombat.model.Reference;
import org.ambraproject.wombat.service.ArticleTransformService;
import org.ambraproject.wombat.service.CaptchaService;
import org.ambraproject.wombat.service.CitationDownloadService;
import org.ambraproject.wombat.service.CommentService;
import org.ambraproject.wombat.service.CommentValidationService;
import org.ambraproject.wombat.service.DoiToJournalResolutionService;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.service.FreemarkerMailService;
import org.ambraproject.wombat.service.ParseXmlService;
import org.ambraproject.wombat.service.remote.ApiAddress;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.ambraproject.wombat.service.remote.CachedRemoteService;
import org.ambraproject.wombat.service.remote.CorpusContentApi;
import org.ambraproject.wombat.service.remote.JsonService;
import org.ambraproject.wombat.service.remote.orcid.OrcidApi;
import org.ambraproject.wombat.service.remote.ServiceRequestException;
import org.ambraproject.wombat.service.remote.SolrUndefinedException;
import org.ambraproject.wombat.service.remote.UserApi;
import org.ambraproject.wombat.service.remote.orcid.OrcidAuthenticationTokenExpiredException;
import org.ambraproject.wombat.service.remote.orcid.OrcidAuthenticationTokenReusedException;
import org.apache.commons.io.output.WriterOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;
import org.w3c.dom.Document;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Controller for rendering an article.
 */
@Controller
public class ArticleController extends WombatController {

  private static final Logger log = LoggerFactory.getLogger(ArticleController.class);

  /**
   * Initial size (in bytes) of buffer that holds transformed article HTML before passing it to the model.
   */
  private static final int XFORM_BUFFER_SIZE = 0x8000;
  private static final int MAX_TO_EMAILS = 5;

  @Autowired
  private Charset charset;
  @Autowired
  private UserApi userApi;
  @Autowired
  private ArticleApi articleApi;
  @Autowired
  private CorpusContentApi corpusContentApi;
  @Autowired
  private ArticleTransformService articleTransformService;
  @Autowired
  private CachedRemoteService<Reader> cachedRemoteReader;
  @Autowired
  private CitationDownloadService citationDownloadService;
  @Autowired
  private JsonService jsonService;
  @Autowired
  private CaptchaService captchaService;
  @Autowired
  private FreeMarkerConfig freeMarkerConfig;
  @Autowired
  private FreemarkerMailService freemarkerMailService;
  @Autowired
  private JavaMailSender javaMailSender;
  @Autowired
  private CommentValidationService commentValidationService;
  @Autowired
  private CommentService commentService;
  @Autowired
  private ArticleMetadata.Factory articleMetadataFactory;
  @Autowired
  private ParseXmlService parseXmlService;
  @Autowired
  private Gson gson;
  @Autowired
  private RuntimeConfiguration runtimeConfiguration;
  @Autowired
  private SiteSet siteSet;
  @Autowired
  private RequestMappingContextDictionary requestMappingContextDictionary;
  @Autowired
  private DoiToJournalResolutionService doiToJournalResolutionService;
  @Autowired
  private OrcidApi orcidApi;

  // TODO: this method currently makes 5 backend RPCs, all sequentially. Explore reducing this
  // number, or doing them in parallel, if this is a performance bottleneck.
  @RequestMapping(name = "article", value = "/article")
  public String renderArticle(HttpServletRequest request,
                              Model model,
                              @SiteParam Site site,
                              RequestedDoiVersion articleId)
      throws IOException {
    ArticlePointer articlePointer = articleMetadataFactory.get(site, articleId)
        .validateVisibility("article")
        .populate(request, model)
        .fillAmendments(model)
        .getArticlePointer();

    XmlContent xmlContent = getXmlContent(site, articlePointer, request);
    model.addAttribute("articleText", xmlContent.html);
    model.addAttribute("references", xmlContent.references);

    return site + "/ftl/article/article";
  }

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

    model.addAttribute("captchaHtml", captchaService.getCaptchaHtml(site, Optional.of("clean")));
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
    model.addAttribute("captchaHtml", captchaService.getCaptchaHtml(site, Optional.of("clean")));
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

    // honeypot for bot.
    // authorPhone and authorAffiliation are fake parameters, which are present in the form, but hidden via CSS.
    // A bot will likely fill one or both of these. But a human will not see those fields to fill them.
    // If any of these parameters are non-empty, then mark it as a bot, and do not proceed further.
    // However, return success response to avoid alarming the bot.

    if (authorPhone != null && !authorPhone.isEmpty() || authorAffiliation != null && !authorAffiliation.isEmpty()) {
      log.warn("bot trapped in honeypot: {}", request.getRemoteAddr());
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


  /**
   * Serves a request for the "about the authors" page for an article.
   *
   * @param model     data to pass to the view
   * @param site      current site
   * @param articleId specifies the article
   * @return path to the template
   * @throws IOException
   */
  @RequestMapping(name = "articleAuthors", value = "/article/authors")
  public String renderArticleAuthors(HttpServletRequest request, Model model, @SiteParam Site site,
                                     RequestedDoiVersion articleId) throws IOException {
    articleMetadataFactory.get(site, articleId)
        .validateVisibility("articleAuthors")
        .populate(request, model);
    return site + "/ftl/article/authors";
  }

  /**
   * Serves the article metrics tab content for an article.
   *
   * @param model     data to pass to the view
   * @param site      current site
   * @param articleId specifies the article
   * @return path to the template
   * @throws IOException
   */
  @RequestMapping(name = "articleMetrics", value = "/article/metrics")
  public String renderArticleMetrics(HttpServletRequest request, Model model, @SiteParam Site site,
                                     RequestedDoiVersion articleId) throws IOException {
    articleMetadataFactory.get(site, articleId)
        .validateVisibility("articleMetrics")
        .populate(request, model);
    return site + "/ftl/article/metrics";
  }

  @RequestMapping(name = "citationDownloadPage", value = "/article/citation")
  public String renderCitationDownloadPage(HttpServletRequest request, Model model, @SiteParam Site site,
                                           RequestedDoiVersion articleId)
      throws IOException {
    articleMetadataFactory.get(site, articleId)
        .validateVisibility("citationDownloadPage")
        .populate(request, model);
    return site + "/ftl/article/citationDownload";
  }

  @RequestMapping(name = "downloadRisCitation", value = "/article/citation/ris", produces = "application/x-research-info-systems;charset=UTF-8")
  public ResponseEntity<String> serveRisCitationDownload(@SiteParam Site site, RequestedDoiVersion articleId)
      throws IOException {
    return serveCitationDownload(site, "downloadRisCitation", articleId, "ris",
        citationDownloadService::buildRisCitation);
  }

  @RequestMapping(name = "downloadBibtexCitation", value = "/article/citation/bibtex", produces = "application/x-bibtex;charset=UTF-8")
  public ResponseEntity<String> serveBibtexCitationDownload(@SiteParam Site site, RequestedDoiVersion articleId)
      throws IOException {
    return serveCitationDownload(site, "downloadBibtexCitation", articleId, "bib",
        citationDownloadService::buildBibtexCitation);
  }

  private ResponseEntity<String> serveCitationDownload(Site site, String handlerName,
                                                       RequestedDoiVersion articleId,
                                                       String fileExtension,
                                                       Function<Map<String, ?>, String> serviceFunction)
      throws IOException {
    ArticleMetadata articleMetadata = articleMetadataFactory.get(site, articleId)
        .validateVisibility(handlerName);
    Map<String, Object> combinedMetadata = new HashMap<>();
    combinedMetadata.putAll(articleMetadata.getIngestionMetadata());
    combinedMetadata.putAll(articleMetadata.getAuthors());

    String citationBody = serviceFunction.apply(combinedMetadata);
    String contentDispositionValue = String.format("attachment; filename=\"%s.%s\"",
        URLEncoder.encode((String) combinedMetadata.get("doi"), Charsets.UTF_8.toString()),
        fileExtension);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_DISPOSITION, contentDispositionValue);
    return new ResponseEntity<>(citationBody, headers, HttpStatus.OK);
  }


  /**
   * Serves the related content tab content for an article.
   *
   * @param model     data to pass to the view
   * @param site      current site
   * @param articleId specifies the article
   * @return path to the template
   * @throws IOException
   */
  @RequestMapping(name = "articleRelatedContent", value = "/article/related")
  public String renderArticleRelatedContent(HttpServletRequest request, Model model, @SiteParam Site site,
                                            RequestedDoiVersion articleId) throws IOException {
    articleMetadataFactory.get(site, articleId)
        .validateVisibility("articleRelatedContent")
        .populate(request, model);
    String recaptchaPublicKey = (String) site.getTheme().getConfigMap("captcha").get("publicKey");
    model.addAttribute("recaptchaPublicKey", recaptchaPublicKey);
    return site + "/ftl/article/relatedContent";
  }

  /**
   * Serves as a POST endpoint to submit media curation requests
   *
   * @param model data passed in from the view
   * @param site  current site
   * @return path to the template
   * @throws IOException
   */
  @RequestMapping(name = "submitMediaCurationRequest", value = "/article/submitMediaCurationRequest", method = RequestMethod.POST)
  @ResponseBody
  public String submitMediaCurationRequest(HttpServletRequest request, Model model, @SiteParam Site site,
                                           @RequestParam("doi") String doi,
                                           @RequestParam("link") String link,
                                           @RequestParam("comment") String comment,
                                           @RequestParam("title") String title,
                                           @RequestParam("publishedOn") String publishedOn,
                                           @RequestParam("name") String name,
                                           @RequestParam("email") String email)
      throws IOException {
    requireNonemptyParameter(doi);

    if (!link.matches("^\\w+://.*")) {
      link = "http://" + link;
    }

    if (!validateMediaCurationInput(model, link, name, email, title, publishedOn)) {
      model.addAttribute("formError", "Invalid values have been submitted.");
      //return model for error reporting
      return jsonService.serialize(model);
    }

    String linkComment = name + ", " + email + "\n" + comment;

    List<NameValuePair> params = new ArrayList<>();
    params.add(new BasicNameValuePair("doi", doi.replaceFirst("info:doi/", "")));
    params.add(new BasicNameValuePair("link", link));
    params.add(new BasicNameValuePair("comment", linkComment));
    params.add(new BasicNameValuePair("title", title));
    params.add(new BasicNameValuePair("publishedOn", publishedOn));

    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");

    String mediaCurationUrl = (String) site.getTheme().getConfigMap("mediaCuration").get("mediaCurationUrl");
    if (mediaCurationUrl == null) {
      throw new RuntimeException("Media curation URL is not configured");
    }

    HttpPost httpPost = new HttpPost(mediaCurationUrl);
    httpPost.setEntity(entity);
    StatusLine statusLine = null;
    try (CloseableHttpResponse response = cachedRemoteReader.getResponse(httpPost)) {
      statusLine = response.getStatusLine();
    } catch (ServiceRequestException e) {
      //This exception is thrown when the submitted link is already present for the article.
      if (e.getStatusCode() == HttpStatus.CONFLICT.value()
          && e.getResponseBody().equals("The link already exists")) {
        model.addAttribute("formError", "This link has already been submitted. Please submit a different link");
        model.addAttribute("isValid", false);
      } else {
        throw new RuntimeException(e);
      }
    } finally {
      httpPost.releaseConnection();
    }

    if (statusLine != null && statusLine.getStatusCode() != HttpStatus.CREATED.value()) {
      throw new RuntimeException("bad response from media curation server: " + statusLine);
    }

    return jsonService.serialize(model);
  }

  /**
   * Validate the input from the form
   *
   * @param model data passed in from the view
   * @param link  link pointing to media content relating to the article
   * @param name  name of the user submitting the media curation request
   * @param email email of the user submitting the media curation request
   * @return true if everything is ok
   */

  private boolean validateMediaCurationInput(Model model, String link, String name,
                                             String email, String title, String publishedOn)
      throws IOException {

    boolean isValid = true;

    UrlValidator urlValidator = new UrlValidator(new String[]{"http", "https"});

    if (StringUtils.isBlank(link)) {
      model.addAttribute("linkError", "This field is required.");
      isValid = false;
    } else if (!urlValidator.isValid(link)) {
      model.addAttribute("linkError", "Invalid Media link URL");
      isValid = false;
    }

    if (StringUtils.isBlank(name)) {
      model.addAttribute("nameError", "This field is required.");
      isValid = false;
    }

    if (StringUtils.isBlank(title)) {
      model.addAttribute("titleError", "This field is required.");
      isValid = false;
    }

    if (StringUtils.isBlank(publishedOn)) {
      model.addAttribute("publishedOnError", "This field is required.");
      isValid = false;
    } else {
      try {
        LocalDate.parse(publishedOn);
      } catch (DateTimeParseException e) {
        model.addAttribute("publishedOnError", "Invalid Date Format, should be YYYY-MM-DD");
        isValid = false;
      }
    }

    if (StringUtils.isBlank(email)) {
      model.addAttribute("emailError", "This field is required.");
      isValid = false;
    } else if (!EmailValidator.getInstance().isValid(email)) {
      model.addAttribute("emailError", "Invalid e-mail address");
      isValid = false;
    }

    model.addAttribute("isValid", isValid);
    return isValid;
  }

  /*
   * Returns a list of figures and tables of a given article; main usage is the figshare tile on the Metrics
   * tab
   *
   * @param site current site
   * @param articleId DOI identifying the article
   * @return a list of figures and tables of a given article
   * @throws IOException
   */
  @RequestMapping(name = "articleFigsAndTables", value = "/article/assets/figsAndTables")
  public ResponseEntity<List> listArticleFiguresAndTables(@SiteParam Site site,
                                                          RequestedDoiVersion articleId) throws IOException {
    List<Map<String, ?>> figureView = articleMetadataFactory.get(site, articleId)
        .validateVisibility("articleFigsAndTables")
        .getFigureView();

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    return new ResponseEntity<>(figureView, headers, HttpStatus.OK);
  }

  @RequestMapping(name = "uploadPreprintRevision", value = "/article/uploadPreprintRevision")
  public String uploadPreprintRevision(HttpServletRequest request, Model model, @SiteParam Site site,
                                       @RequestParam("state") String state,
                                       @RequestParam("code") String code) throws IOException, URISyntaxException {
    final byte[] decodedState = Base64.getDecoder().decode(state);
    final String decodedJson = URLDecoder.decode(new String(decodedState), "UTF-8");
    Map<String, Object> stateJson = gson.fromJson(decodedJson, HashMap.class);

    String correspondingAuthorOrcidId = (String) stateJson.get("orcid_id");
    String authenticatedOrcidId = "";

    try {
      authenticatedOrcidId = orcidApi.getOrcidIdFromAuthorizationCode(site, code);
    } catch (OrcidAuthenticationTokenExpiredException | OrcidAuthenticationTokenReusedException e) {
      model.addAttribute("orcidAuthenticationError", e.getMessage());
    }

    boolean isError = true;
    if (correspondingAuthorOrcidId.equals(authenticatedOrcidId)) {
      model.addAttribute("orcidId", correspondingAuthorOrcidId);
      isError = false;
    } else if (!Strings.isNullOrEmpty(authenticatedOrcidId)) {
      model.addAttribute("orcidAuthenticationError", "ORCID IDs do not match. " +
          "Corresponding author ORCID ID must be used.");
    }

    if (isError) {
      final RequestedDoiVersion articleId = RequestedDoiVersion.of((String) stateJson.get("doi"));
      return renderArticle(request, model, site, articleId);
    } else {
      return site + "/ftl/article/uploadPreprintRevision";
    }
  }

  @RequestMapping(name = "email", value = "/article/email")
  public String renderEmailThisArticle(HttpServletRequest request, Model model, @SiteParam Site site,
                                       RequestedDoiVersion articleId) throws IOException {
    articleMetadataFactory.get(site, articleId)
        .validateVisibility("email")
        .populate(request, model);
    model.addAttribute("maxEmails", MAX_TO_EMAILS);
    model.addAttribute("captchaHTML", captchaService.getCaptchaHtml(site, Optional.empty()));
    return site + "/ftl/article/email";
  }

  /**
   * @param model data passed in from the view
   * @param site  current site
   * @return path to the template
   * @throws IOException
   */
  @RequestMapping(name = "emailPost", value = "/article/email", method = RequestMethod.POST)
  public String emailArticle(HttpServletRequest request, HttpServletResponse response, Model model,
                             @SiteParam Site site,
                             RequestedDoiVersion articleId,
                             @RequestParam("articleUri") String articleUri,
                             @RequestParam("emailToAddresses") String emailToAddresses,
                             @RequestParam("emailFrom") String emailFrom,
                             @RequestParam("senderName") String senderName,
                             @RequestParam("note") String note,
                             @RequestParam(RECAPTCHA_CHALLENGE_FIELD) String captchaChallenge,
                             @RequestParam(RECAPTCHA_RESPONSE_FIELD) String captchaResponse)
      throws IOException, MessagingException {
    requireNonemptyParameter(articleUri);

    model.addAttribute("emailToAddresses", emailToAddresses);
    model.addAttribute("emailFrom", emailFrom);
    model.addAttribute("senderName", senderName);
    model.addAttribute("note", note);
    model.addAttribute("articleUri", articleUri);

    List<InternetAddress> toAddresses = Splitter.on(CharMatcher.anyOf("\n\r")).omitEmptyStrings()
        .splitToList(emailToAddresses).stream()
        .map(email -> EmailMessage.createAddress(null /*name*/, email))
        .collect(Collectors.toList());

    Set<String> errors = validateEmailArticleInput(toAddresses, emailFrom, senderName,
        captchaChallenge, captchaResponse, site, request);
    if (applyValidation(response, model, errors)) {
      return renderEmailThisArticle(request, model, site, articleId);
    }

    Map<String, ?> articleMetadata = articleMetadataFactory.get(site, articleId)
        .validateVisibility("emailPost")
        .getIngestionMetadata();

    String title = articleMetadata.get("title").toString();
    model.addAttribute("title", title);
    model.addAttribute("description", articleMetadata.get("description"));
    model.addAttribute("journalName", site.getJournalName());
    Multipart content = freemarkerMailService.createContent(site, "emailThisArticle", model);

    EmailMessage message = EmailMessage.builder()
        .addToEmailAddresses(toAddresses)
        .setSenderAddress(EmailMessage.createAddress(senderName, emailFrom))
        .setSubject("An Article from " + site.getJournalName() + ": " + title)
        .setContent(content)
        .setEncoding(freeMarkerConfig.getConfiguration().getDefaultEncoding())
        .build();

    message.send(javaMailSender);

    response.setStatus(HttpStatus.CREATED.value());
    return site + "/ftl/article/emailSuccess";
  }

  private Set<String> validateEmailArticleInput(List<InternetAddress> emailToAddresses,
                                                String emailFrom, String senderName, String captchaChallenge, String captchaResponse,
                                                Site site, HttpServletRequest request) throws IOException {

    Set<String> errors = new HashSet<>();
    if (StringUtils.isBlank(emailFrom)) {
      errors.add("emailFromMissing");
    } else if (!EmailValidator.getInstance().isValid(emailFrom)) {
      errors.add("emailFromInvalid");
    }

    if (emailToAddresses.isEmpty()) {
      errors.add("emailToAddressesMissing");
    } else if (emailToAddresses.size() > MAX_TO_EMAILS) {
      errors.add("tooManyEmailToAddresses");
    } else if (emailToAddresses.stream()
        .noneMatch(email -> EmailValidator.getInstance().isValid(email.toString()))) {
      errors.add("emailToAddressesInvalid");
    }

    if (StringUtils.isBlank(senderName)) {
      errors.add("senderNameMissing");
    }

    if (!captchaService.validateCaptcha(site, request.getRemoteAddr(), captchaChallenge, captchaResponse)) {
      errors.add("captchaError");
    }

    return errors;
  }


  private static class XmlContent implements Serializable {
    private final String html;
    private final ImmutableList<Reference> references;

    private XmlContent(String html, List<Reference> references) {
      this.html = Objects.requireNonNull(html);
      this.references = ImmutableList.copyOf(references);
    }
  }

  /**
   * Gets article xml from cache if it exists; otherwise, gets it from rhino and caches it. Then it parses the
   * references and does html transform
   *
   * @param articlePointer
   * @param request
   * @return an XmlContent containing the list of references and article html
   * @throws IOException
   */
  private XmlContent getXmlContent(Site site, ArticlePointer articlePointer,
                                   HttpServletRequest request) throws IOException {
    return corpusContentApi.readManuscript(articlePointer, site, "html", (InputStream stream) -> {
      byte[] xml = ByteStreams.toByteArray(stream);
      final Document document = parseXmlService.getDocument(new ByteArrayInputStream(xml));

      List<Reference> references = parseXmlService.parseArticleReferences(document,
          doi -> getLinkText(site, request, doi));

      StringWriter articleHtml = new StringWriter(XFORM_BUFFER_SIZE);
      try (OutputStream outputStream = new WriterOutputStream(articleHtml, charset)) {
        articleTransformService.transformArticle(site, articlePointer, references,
            new ByteArrayInputStream(xml), outputStream);
      }

      return new XmlContent(articleHtml.toString(), references);
    });
  }

  private String getLinkText(Site site, HttpServletRequest request, String doi) throws IOException {
    String citationJournalKey;
    try {
      citationJournalKey = doiToJournalResolutionService.getJournalKeyFromDoi(doi, site);
    } catch (SolrUndefinedException | EntityNotFoundException e) {
      // If we can't look it up in Solr, fail quietly, the same as though no match was found.
      log.error("Solr is undefined or returning errors on query.");
      citationJournalKey = null;
    }
    String linkText = null;
    if (citationJournalKey != null) {
      linkText = Link.toForeignSite(site, citationJournalKey, siteSet)
          .toPattern(requestMappingContextDictionary, "article")
          .addQueryParameter("id", doi)
          .build()
          .get(request);
    }
    return linkText;
  }
}
