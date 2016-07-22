package org.ambraproject.wombat.controller;

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import org.ambraproject.wombat.config.RemoteCacheSpace;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.model.ArticleComment;
import org.ambraproject.wombat.model.ArticleCommentFlag;
import org.ambraproject.wombat.model.ScholarlyWorkId;
import org.ambraproject.wombat.service.ApiAddress;
import org.ambraproject.wombat.service.ArticleService;
import org.ambraproject.wombat.service.ArticleTransformService;
import org.ambraproject.wombat.service.CaptchaService;
import org.ambraproject.wombat.service.CitationDownloadService;
import org.ambraproject.wombat.service.CommentService;
import org.ambraproject.wombat.service.CommentValidationService;
import org.ambraproject.wombat.service.EmailMessage;
import org.ambraproject.wombat.service.FreemarkerMailService;
import org.ambraproject.wombat.service.RenderContext;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.ambraproject.wombat.service.remote.CachedRemoteService;
import org.ambraproject.wombat.service.remote.CorpusContentApi;
import org.ambraproject.wombat.service.remote.JsonService;
import org.ambraproject.wombat.service.remote.ServiceRequestException;
import org.ambraproject.wombat.service.remote.UserApi;
import org.ambraproject.wombat.util.DoiSchemeStripper;
import org.ambraproject.wombat.util.HttpMessageUtil;
import org.ambraproject.wombat.util.UriUtil;
import org.apache.commons.io.output.WriterOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
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

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

  private static final String COMMENT_NAMESPACE = "comments";

  @Autowired
  private Charset charset;
  @Autowired
  private UserApi userApi;
  @Autowired
  private ArticleApi articleApi;
  @Autowired
  private CorpusContentApi corpusContentApi;
  @Autowired
  private ArticleService articleService;
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

  // TODO: this method currently makes 5 backend RPCs, all sequentially. Explore reducing this
  // number, or doing them in parallel, if this is a performance bottleneck.
  @RequestMapping(name = "article", value = "/article")
  public String renderArticle(HttpServletRequest request,
                              Model model,
                              @SiteParam Site site,
                              ScholarlyWorkId workId)
      throws IOException {
    articleMetadataFactory.get(site, workId)
        .validateVisibility()
        .populate(request, model)
        .fillAmendments(model);

    RenderContext renderContext = new RenderContext(site, workId);

    String articleHtml = getArticleHtml(renderContext);
    model.addAttribute("articleText", articleHtml);


    return site + "/ftl/article/article";
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
                                      ScholarlyWorkId articleId) throws IOException {
    articleMetadataFactory.get(site, articleId)
        .validateVisibility()
        .populate(request, model);

    try {
      model.addAttribute("articleComments", commentService.getArticleComments(articleId));
    } catch (UserApi.UserApiException e) {
      log.error(e.getMessage(), e);
      model.addAttribute("userApiError", e);
    }

    return site + "/ftl/article/comment/comments";
  }

  @RequestMapping(name = "articleCommentForm", value = "/article/comments/new")
  public String renderNewCommentForm(HttpServletRequest request, Model model, @SiteParam Site site,
                                     ScholarlyWorkId articleId)
      throws IOException {
    articleMetadataFactory.get(site, articleId)
        .validateVisibility()
        .populate(request, model);

    model.addAttribute("captchaHtml", captchaService.getCaptchaHtml(site, Optional.of("clean")));
    return site + "/ftl/article/comment/newComment";
  }


  /**
   * Serves a request for an expanded view of a single comment and any replies.
   *
   * @param model     data to pass to the view
   * @param site      current site
   * @param commentId specifies the comment
   * @return path to the template
   * @throws IOException
   */
  @RequestMapping(name = "articleCommentTree", value = "/article/comment")
  public String renderArticleCommentTree(HttpServletRequest request, Model model, @SiteParam Site site,
                                         @RequestParam("id") String commentId) throws IOException {
    requireNonemptyParameter(commentId);
    Map<String, Object> comment;
    try {
      comment = commentService.getComment(commentId);
    } catch (CommentService.CommentNotFoundException e) {
      throw new NotFoundException(e);
    } catch (UserApi.UserApiException e) {
      log.error(e.getMessage(), e);
      model.addAttribute("userApiError", e);

      // Get a copy of the comment that is not populated with userApi data.
      // This articleApi call is redundant to one that commentService.getComment would have made before throwing.
      // TODO: Prevent extra articleApi call
      comment = articleApi.requestObject(ApiAddress.builder("comments").addToken(commentId).build(), Map.class);
    }

    Map<?, ?> parentArticleStub = (Map<?, ?>) comment.get("parentArticle");
    ScholarlyWorkId articleId = ScholarlyWorkId.of((String) parentArticleStub.get("doi")); // latest revision

    articleMetadataFactory.get(site, articleId)
        .validateVisibility()
        .populate(request, model);

    model.addAttribute("comment", comment);
    model.addAttribute("captchaHtml", captchaService.getCaptchaHtml(site, Optional.of("clean")));
    return site + "/ftl/article/comment/comment";
  }

  private static HttpUriRequest createJsonPostRequest(URI target, Object body) {
    String json = new Gson().toJson(body);
    HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
    RequestBuilder reqBuilder = RequestBuilder.create("POST").setUri(target).setEntity(entity);
    return reqBuilder.build();
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
                                  @RequestParam(value = "ciStatement", required = false) String ciStatement,
                                  @RequestParam(value = "target", required = false) String parentArticleDoi,
                                  @RequestParam(value = "inReplyTo", required = false) String parentCommentUri,
                                  @RequestParam(RECAPTCHA_CHALLENGE_FIELD) String captchaChallenge,
                                  @RequestParam(RECAPTCHA_RESPONSE_FIELD) String captchaResponse)
      throws IOException {
    Map<String, Object> validationErrors = commentValidationService.validateComment(site,
        commentTitle, commentBody, hasCompetingInterest, ciStatement);

    if (validationErrors.isEmpty()) {
      // Submit Captcha for validation only if there are no other errors.
      // Otherwise, the user's valid Captcha response would be wasted when they resubmit the comment.
      if (!captchaService.validateCaptcha(site, request.getRemoteAddr(), captchaChallenge, captchaResponse)) {
        validationErrors.put("captchaValidationFailure", true);
      }
    }

    if (!validationErrors.isEmpty()) {
      return ImmutableMap.of("validationErrors", validationErrors);
    }

    URI forwardedUrl = UriUtil.concatenate(articleApi.getServerUrl(), COMMENT_NAMESPACE);

    String authId = request.getRemoteUser();
    ArticleComment comment = new ArticleComment(parentArticleDoi, userApi.getUserIdFromAuthId(authId),
        parentCommentUri, commentTitle, commentBody, ciStatement);

    HttpUriRequest commentPostRequest = createJsonPostRequest(forwardedUrl, comment);
    try (CloseableHttpResponse response = articleApi.getResponse(commentPostRequest)) {
      String createdCommentUri = HttpMessageUtil.readResponse(response);
      return ImmutableMap.of("createdCommentUri", createdCommentUri);
    }
  }

  @RequestMapping(name = "postCommentFlag", method = RequestMethod.POST, value = "/article/comments/flag")
  @ResponseBody
  public Object receiveCommentFlag(HttpServletRequest request, @SiteParam Site site,
                                   @RequestParam("reasonCode") String reasonCode,
                                   @RequestParam("comment") String flagCommentBody,
                                   @RequestParam("target") String targetComment)
      throws IOException {
    Map<String, Object> validationErrors = commentValidationService.validateFlag(flagCommentBody);
    if (!validationErrors.isEmpty()) {
      return ImmutableMap.of("validationErrors", validationErrors);
    }

    URI forwardedUrl = UriUtil.concatenate(articleApi.getServerUrl(),
        String.format("%s/%s?flags", COMMENT_NAMESPACE, targetComment));
    String authId = request.getRemoteUser();
    ArticleCommentFlag flag = new ArticleCommentFlag(userApi.getUserIdFromAuthId(authId), flagCommentBody, reasonCode);

    HttpUriRequest commentPostRequest = createJsonPostRequest(forwardedUrl, flag);
    try (CloseableHttpResponse response = articleApi.getResponse(commentPostRequest)) {
      return ImmutableMap.of(); // the "201 CREATED" status is all the AJAX client needs
    }
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
                                     ScholarlyWorkId articleId) throws IOException {
    articleMetadataFactory.get(site, articleId)
        .validateVisibility()
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
                                     ScholarlyWorkId articleId) throws IOException {
    articleMetadataFactory.get(site, articleId)
        .validateVisibility()
        .populate(request, model);
    return site + "/ftl/article/metrics";
  }

  @RequestMapping(name = "citationDownloadPage", value = "/article/citation")
  public String renderCitationDownloadPage(HttpServletRequest request, Model model, @SiteParam Site site,
                                           ScholarlyWorkId articleId)
      throws IOException {
    articleMetadataFactory.get(site, articleId)
        .validateVisibility()
        .populate(request, model);
    return site + "/ftl/article/citationDownload";
  }

  @RequestMapping(name = "downloadRisCitation", value = "/article/citation/ris", produces = "application/x-research-info-systems;charset=UTF-8")
  public ResponseEntity<String> serveRisCitationDownload(@SiteParam Site site, ScholarlyWorkId articleId)
      throws IOException {
    return serveCitationDownload(site, articleId, "ris",
        citationDownloadService::buildRisCitation);
  }

  @RequestMapping(name = "downloadBibtexCitation", value = "/article/citation/bibtex", produces = "application/x-bibtex;charset=UTF-8")
  public ResponseEntity<String> serveBibtexCitationDownload(@SiteParam Site site, ScholarlyWorkId articleId)
      throws IOException {
    return serveCitationDownload(site, articleId, "bib",
        citationDownloadService::buildBibtexCitation);
  }

  private ResponseEntity<String> serveCitationDownload(Site site,
                                                       ScholarlyWorkId articleId,
                                                       String fileExtension,
                                                       Function<Map<String, ?>, String> serviceFunction)
      throws IOException {
    Map<String, ?> articleMetadata = articleMetadataFactory.get(site, articleId)
        .validateVisibility()
        .getIngestionMetadata();

    String citationBody = serviceFunction.apply(articleMetadata);
    String contentDispositionValue = String.format("attachment; filename=\"%s.%s\"",
        URLEncoder.encode(DoiSchemeStripper.strip((String) articleMetadata.get("doi")), Charsets.UTF_8.toString()),
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
                                            ScholarlyWorkId articleId) throws IOException {
    articleMetadataFactory.get(site, articleId)
        .validateVisibility()
        .populate(request, model);
    String recaptchaPublicKey = site.getTheme().getConfigMap("captcha").get("publicKey").toString();
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
  public
  @ResponseBody
  String submitMediaCurationRequest(HttpServletRequest request, Model model, @SiteParam Site site,
                                    @RequestParam("doi") String doi,
                                    @RequestParam("link") String link,
                                    @RequestParam("comment") String comment,
                                    @RequestParam("name") String name,
                                    @RequestParam("email") String email,
                                    @RequestParam(RECAPTCHA_CHALLENGE_FIELD) String captchaChallenge,
                                    @RequestParam(RECAPTCHA_RESPONSE_FIELD) String captchaResponse)
      throws IOException {
    requireNonemptyParameter(doi);

    if (!validateMediaCurationInput(model, link, name, email, captchaChallenge,
        captchaResponse, site, request)) {
      model.addAttribute("formError", "Invalid values have been submitted.");
      //return model for error reporting
      return jsonService.serialize(model);
    }

    String linkComment = name + ", " + email + "\n" + comment;

    List<NameValuePair> params = new ArrayList<>();
    params.add(new BasicNameValuePair("doi", doi.replaceFirst("info:doi/", "")));
    params.add(new BasicNameValuePair("link", link));
    params.add(new BasicNameValuePair("comment", linkComment));
    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");

    String mediaCurationUrl = site.getTheme().getConfigMap("mediaCuration").get("mediaCurationUrl").toString();

    if (mediaCurationUrl != null) {
      HttpPost httpPost = new HttpPost(mediaCurationUrl);
      httpPost.setEntity(entity);
      StatusLine statusLine = null;
      try (CloseableHttpResponse response = cachedRemoteReader.getResponse(httpPost)) {
        statusLine = response.getStatusLine();
      } catch (ServiceRequestException e) {
        //This exception is thrown when the submitted link is already present for the article.
        if (e.getStatusCode() == HttpStatus.BAD_REQUEST.value()
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
   * @param site  current site
   * @return true if everything is ok
   */
  private boolean validateMediaCurationInput(Model model, String link, String name,
                                             String email, String captchaChallenge, String captchaResponse, Site site,
                                             HttpServletRequest request) throws IOException {

    boolean isValid = true;

    UrlValidator urlValidator = new UrlValidator();

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

    if (StringUtils.isBlank(email)) {
      model.addAttribute("emailError", "This field is required.");
      isValid = false;
    } else if (!EmailValidator.getInstance().isValid(email)) {
      model.addAttribute("emailError", "Invalid e-mail address");
      isValid = false;
    }

    if (!captchaService.validateCaptcha(site, request.getRemoteAddr(), captchaChallenge, captchaResponse)) {
      model.addAttribute("captchaError", "Verification is incorrect. Please try again.");
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
                                                          ScholarlyWorkId articleId) throws IOException {
    Map<String, ?> articleMetadata = articleMetadataFactory.get(site, articleId)
        .validateVisibility()
        .getIngestionMetadata();
    List<ImmutableMap<String, String>> articleFigsAndTables = articleService.getArticleFiguresAndTables(articleMetadata);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    return new ResponseEntity<>(articleFigsAndTables, headers, HttpStatus.OK);
  }

  @RequestMapping(name = "email", value = "/article/email")
  public String renderEmailThisArticle(HttpServletRequest request, Model model, @SiteParam Site site,
                                       ScholarlyWorkId articleId) throws IOException {
    articleMetadataFactory.get(site, articleId)
        .validateVisibility()
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
                             ScholarlyWorkId articleId,
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
        .validateVisibility()
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

  /**
   * Retrieves article XML from the SOA server, transforms it into HTML, and returns it. Result will be stored in
   * memcache.
   *
   * @return String of the article HTML
   * @throws IOException
   */
  private String getArticleHtml(final RenderContext renderContext) throws IOException {
    return corpusContentApi.readManuscript(renderContext, RemoteCacheSpace.ARTICLE_HTML,
        (InputStream stream) -> {
          StringWriter articleHtml = new StringWriter(XFORM_BUFFER_SIZE);
          try (OutputStream outputStream = new WriterOutputStream(articleHtml, charset)) {
            articleTransformService.transform(renderContext, stream, outputStream);
          } catch (TransformerException e) {
            throw new RuntimeException(e);
          }
          return articleHtml.toString();
        });
  }

}
