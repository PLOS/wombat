package org.ambraproject.wombat.controller;

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import com.google.gson.Gson;
import org.ambraproject.wombat.config.site.RequestMappingContextDictionary;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.site.url.Link;
import org.ambraproject.wombat.model.ArticleComment;
import org.ambraproject.wombat.model.ArticleCommentFlag;
import org.ambraproject.wombat.service.ArticleService;
import org.ambraproject.wombat.service.ArticleTransformService;
import org.ambraproject.wombat.service.CaptchaService;
import org.ambraproject.wombat.service.CitationDownloadService;
import org.ambraproject.wombat.service.CommentFormatting;
import org.ambraproject.wombat.service.CommentValidationService;
import org.ambraproject.wombat.service.EmailMessage;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.service.FreemarkerMailService;
import org.ambraproject.wombat.service.RenderContext;
import org.ambraproject.wombat.service.XmlService;
import org.ambraproject.wombat.service.remote.CachedRemoteService;
import org.ambraproject.wombat.service.remote.JsonService;
import org.ambraproject.wombat.service.remote.ServiceRequestException;
import org.ambraproject.wombat.service.remote.SoaService;
import org.ambraproject.wombat.util.CacheParams;
import org.ambraproject.wombat.util.DoiSchemeStripper;
import org.ambraproject.wombat.util.HttpMessageUtil;
import org.ambraproject.wombat.util.TextUtil;
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
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
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
  private SiteSet siteSet;
  @Autowired
  private SoaService soaService;
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
  private XmlService xmlService;
  @Autowired
  private RequestMappingContextDictionary requestMappingContextDictionary;

  // TODO: this method currently makes 5 backend RPCs, all sequentially. Explore reducing this
  // number, or doing them in parallel, if this is a performance bottleneck.
  @RequestMapping(name = "article", value = "/article")
  public String renderArticle(HttpServletRequest request,
                              Model model,
                              @SiteParam Site site,
                              @RequestParam("id") String articleId)
      throws IOException {
    Map<?, ?> articleMetaData = addCommonModelAttributes(request, model, site, articleId);
    validateArticleVisibility(site, articleMetaData);

    requireNonemptyParameter(articleId);
    RenderContext renderContext = new RenderContext(site);
    renderContext.setArticleId(articleId);

    String articleHtml = getArticleHtml(renderContext);
    model.addAttribute("article", articleMetaData);
    model.addAttribute("articleText", articleHtml);
    model.addAttribute("amendments", fillAmendments(site, articleMetaData));

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
                                      @RequestParam("id") String articleId) throws IOException {
    requireNonemptyParameter(articleId);
    Map<?, ?> articleMetaData = addCommonModelAttributes(request, model, site, articleId);
    validateArticleVisibility(site, articleMetaData);

    List<?> comments = soaService.requestObject(String.format("articles/%s?comments", articleId), List.class);
    model.addAttribute("articleComments", comments);

    return site + "/ftl/article/comment/comments";
  }

  @RequestMapping(name = "articleCommentForm", value = "/article/comments/new")
  public String renderNewCommentForm(HttpServletRequest request, Model model, @SiteParam Site site,
                                     @RequestParam("id") String articleId)
      throws IOException {
    requireNonemptyParameter(articleId);
    Map<?, ?> articleMetaData = addCommonModelAttributes(request, model, site, articleId);
    validateArticleVisibility(site, articleMetaData);
    return site + "/ftl/article/comment/newComment";
  }


  /**
   * Types of related articles that get special display handling.
   */
  private static enum AmendmentType {
    CORRECTION("correction-forward"),
    EOC("expressed-concern"),
    RETRACTION("retraction");

    /**
     * A value of the "type" field of an object in an article's "relatedArticles" list.
     */
    private final String relationshipType;

    private AmendmentType(String relationshipType) {
      this.relationshipType = relationshipType;
    }

    // For use as a key in maps destined for the FreeMarker model
    private String getLabel() {
      return name().toLowerCase();
    }

    private static final int COUNT = values().length;

    private static final ImmutableMap<String, AmendmentType> BY_RELATIONSHIP_TYPE = Maps.uniqueIndex(
        EnumSet.allOf(AmendmentType.class),
        new Function<AmendmentType, String>() {
          @Override
          public String apply(AmendmentType input) {
            return input.relationshipType;
          }
        }
    );
  }

  private Map<String, Collection<Object>> getContainingArticleLists(String doi, Site site) throws IOException {
    List<Map<?, ?>> articleListObjects = soaService.requestObject(String.format("articles/%s?lists", doi), List.class);
    Multimap<String, Object> result = LinkedListMultimap.create(articleListObjects.size());
    for (Map<?, ?> articleListObject : articleListObjects) {
      String listType = Preconditions.checkNotNull((String) articleListObject.get("type"));
      result.put(listType, articleListObject);
    }
    return result.asMap();
  }

  /**
   * Iterate over article categories and extract and sort unique category terms (i.e., the final category term in a
   * given category path)
   *
   * @param articleMetadata
   * @return a sorted list of category terms
   */
  private List<String> getCategoryTerms(Map<?, ?> articleMetadata) {
    List<Map<String, ?>> categories = (List<Map<String, ?>>) articleMetadata.get("categories");
    if (categories == null || categories.isEmpty()) {
      return ImmutableList.of();
    }

    // create a map of terms/weights (effectively removes duplicate terms through the mapping)
    Map<String, Double> termsMap = new HashMap<>();
    for (Map<String, ?> category : categories) {
      String[] categoryTerms = ((String) category.get("path")).split("/");
      String categoryTerm = categoryTerms[categoryTerms.length - 1];
      termsMap.put(categoryTerm, (Double) category.get("weight"));
    }

    // use Guava for sorting, first on weight (descending), then on category term
    Comparator valueComparator = Ordering.natural().reverse().onResultOf(Functions.forMap(termsMap)).compound(Ordering.natural());
    SortedMap<String, Double> sortedTermsMap = ImmutableSortedMap.copyOf(termsMap, valueComparator);

    return new ArrayList<>(sortedTermsMap.keySet());

  }


  /**
   * Check related articles for ones that amend this article. Set them up for special display, and retrieve additional
   * data about those articles from the service tier.
   *
   * @param articleMetadata the article metadata
   * @return a map from amendment type labels to related article objects
   */
  private Map<String, List<Object>> fillAmendments(Site site, Map<?, ?> articleMetadata) throws IOException {
    List<Map<String, ?>> relatedArticles = (List<Map<String, ?>>) articleMetadata.get("relatedArticles");
    if (relatedArticles == null || relatedArticles.isEmpty()) {
      return ImmutableMap.of();
    }
    ListMultimap<String, Object> amendments = LinkedListMultimap.create(AmendmentType.COUNT);
    for (Map<String, ?> relatedArticle : relatedArticles) {
      String relationshipType = (String) relatedArticle.get("type");
      AmendmentType amendmentType = AmendmentType.BY_RELATIONSHIP_TYPE.get(relationshipType);
      if (amendmentType != null) {
        amendments.put(amendmentType.getLabel(), relatedArticle);
      }
    }
    if (amendments.keySet().size() > 1) {
      applyAmendmentPrecedence(amendments);
    }

    for (Object amendmentObj : amendments.values()) {
      Map<String, Object> amendment = (Map<String, Object>) amendmentObj;
      String amendmentId = (String) amendment.get("doi");

      Map<String, ?> amendmentMetadata = (Map<String, ?>) requestArticleMetadata(amendmentId);
      amendment.putAll(amendmentMetadata);

      // Display the body only on non-correction amendments. Would be better if there were configurable per theme.
      String amendmentType = (String) amendment.get("type");
      if (!amendmentType.equals(AmendmentType.CORRECTION.relationshipType)) {
        RenderContext renderContext = new RenderContext(site);
        renderContext.setArticleId(amendmentId);
        String body = getAmendmentBody(renderContext);
        amendment.put("body", body);
      }
    }

    return Multimaps.asMap(amendments);
  }

  /**
   * Add links to cross-published journals to the model.
   * <p>
   * Each journal in which the article was published (according to the supplied article metadata) will be represented in
   * the model, other than the journal belonging to the site being browsed. If that journal is the only one, nothing is
   * added to the model. The journal of original publication (according to the article metadata's eISSN) is added under
   * the named {@code "originalPub"}, and other journals are added as a collection named {@code "crossPub"}.
   *
   * @param request         the contextual request (used to build cross-site links)
   * @param model           the page model into which to insert the link values
   * @param site            the site of the current page request
   * @param articleMetadata metadata for an article being rendered
   * @throws IOException
   */
  private void addCrossPublishedJournals(HttpServletRequest request, Model model, Site site, Map<?, ?> articleMetadata)
      throws IOException {
    final Map<?, ?> publishedJournals = (Map<?, ?>) articleMetadata.get("journals");
    final String eissn = (String) articleMetadata.get("eIssn");
    Collection<Map<String, Object>> crossPublishedJournals;
    Map<String, Object> originalJournal = null;

    if (publishedJournals.size() <= 1) {
      // The article was published in only one journal.
      // Assume it is the one being browsed (validateArticleVisibility would have caught it otherwise).
      crossPublishedJournals = ImmutableList.of();
    } else {
      crossPublishedJournals = Lists.newArrayListWithCapacity(publishedJournals.size() - 1);
      String localJournal = site.getJournalKey();

      for (Map.Entry<?, ?> journalEntry : publishedJournals.entrySet()) {
        String journalKey = (String) journalEntry.getKey();
        if (journalKey.equals(localJournal)) {
          // This is the journal being browsed right now, so don't add a link
          continue;
        }

        // Make a mutable copy to clobber
        Map<String, Object> crossPublishedJournalMetadata = new HashMap<>((Map<? extends String, ?>) journalEntry.getValue());

        // Find the site object (if possible) for the other journal
        String crossPublishedJournalKey = (String) crossPublishedJournalMetadata.get("journalKey");
        Site crossPublishedSite = site.getTheme().resolveForeignJournalKey(siteSet, crossPublishedJournalKey);

        // Set up an href link to the other site's root page.
        // Do not link to handlerName="homePage" because we don't know if the other site has disabled it.
        String homepageLink = Link.toForeignSite(site, crossPublishedSite).toPath("").get(request);
        crossPublishedJournalMetadata.put("href", homepageLink);

        // Look up whether the other site wants its journal title italicized
        // (This isn't a big deal because it's only one value, but if similar display details pile up
        // in the future, it would be better to abstract them out than to handle them all individually here.)
        boolean italicizeTitle = (boolean) crossPublishedSite.getTheme().getConfigMap("journal").get("italicizeTitle");
        crossPublishedJournalMetadata.put("italicizeTitle", italicizeTitle);

        if (eissn.equals(crossPublishedJournalMetadata.get("eIssn"))) {
          originalJournal = crossPublishedJournalMetadata;
        } else {
          crossPublishedJournals.add(crossPublishedJournalMetadata);
        }
      }
    }

    model.addAttribute("crossPub", crossPublishedJournals);
    model.addAttribute("originalPub", originalJournal);
  }

  /**
   * Apply the display logic for different amendment types taking precedence over each other.
   * <p>
   * Retractions take precedence over all else (i.e., don't show them if there is a retraction) and EOCs take precedence
   * over corrections. This logic could conceivably vary between sites (e.g., some journals might want to show all
   * amendments side-by-side), so this is a good candidate for making it controllable through config. But for now,
   * assume that the rules are always the same.
   *
   * @param amendments related article objects, keyed by {@link AmendmentType#getLabel()}.
   */
  private static void applyAmendmentPrecedence(ListMultimap<String, Object> amendments) {
    if (amendments.containsKey(AmendmentType.RETRACTION.getLabel())) {
      amendments.removeAll(AmendmentType.EOC.getLabel());
      amendments.removeAll(AmendmentType.CORRECTION.getLabel());
    } else if (amendments.containsKey(AmendmentType.EOC.getLabel())) {
      amendments.removeAll(AmendmentType.CORRECTION.getLabel());
    }
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
      comment = soaService.requestObject(String.format("comments/" + commentId), Map.class);
    } catch (EntityNotFoundException enfe) {
      throw new NotFoundException(enfe);
    }

    Map<?, ?> parentArticleStub = (Map<?, ?>) comment.get("parentArticle");
    String articleId = (String) parentArticleStub.get("doi");
    Map<?, ?> articleMetadata = addCommonModelAttributes(request, model, site, articleId);
    validateArticleVisibility(site, articleMetadata);

    comment = CommentFormatting.addFormattingFields(comment);
    model.addAttribute("comment", comment);

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
                                  @RequestParam(value = "inReplyTo", required = false) String parentCommentUri) throws IOException {
    Map<String, Object> validationErrors = commentValidationService.validateComment(site,
        commentTitle, commentBody, hasCompetingInterest, ciStatement);
    if (!validationErrors.isEmpty()) {
      return ImmutableMap.of("validationErrors", validationErrors);
    }

    URI forwardedUrl = UriUtil.concatenate(soaService.getServerUrl(), COMMENT_NAMESPACE);
    ArticleComment comment = new ArticleComment(parentArticleDoi, request.getRemoteUser(),
        parentCommentUri, commentTitle, commentBody, ciStatement);

    HttpUriRequest commentPostRequest = createJsonPostRequest(forwardedUrl, comment);
    try (CloseableHttpResponse response = soaService.getResponse(commentPostRequest)) {
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

    URI forwardedUrl = UriUtil.concatenate(soaService.getServerUrl(),
        String.format("%s/%s?flag", COMMENT_NAMESPACE, targetComment));
    ArticleCommentFlag flag = new ArticleCommentFlag(request.getRemoteUser(), flagCommentBody, reasonCode);

    HttpUriRequest commentPostRequest = createJsonPostRequest(forwardedUrl, flag);
    try (CloseableHttpResponse response = soaService.getResponse(commentPostRequest)) {
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
  public String renderArticleAuthors( HttpServletRequest request, Model model, @SiteParam Site site,
                                     @RequestParam("id") String articleId) throws IOException {
      Map<?, ?> articleMetaData = addCommonModelAttributes(request, model, site, articleId);
      validateArticleVisibility(site, articleMetaData);
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
                                     @RequestParam("id") String articleId) throws IOException {
      Map<?, ?> articleMetaData = addCommonModelAttributes(request, model, site, articleId);
      validateArticleVisibility(site, articleMetaData);
      return site + "/ftl/article/metrics";
  }

@RequestMapping(name = "articleMetricsRenovated", value = "/article/metricsRenovated")
public String renderArticleMetricsRenovated(HttpServletRequest request, Model model, @SiteParam Site site,
                                   @RequestParam("id") String articleId) throws IOException {
  enforceDevFeature("metricsRenovated");
  Map<?, ?> articleMetaData = addCommonModelAttributes(request, model, site, articleId);
  validateArticleVisibility(site, articleMetaData);
  requestComments(model, articleId);
  return site + "/ftl/article/metricsRenovated";
}


  @RequestMapping(name = "citationDownloadPage", value = "/article/citation")
  public String renderCitationDownloadPage(HttpServletRequest request, Model model, @SiteParam Site site,
                                           @RequestParam("id") String articleId)
      throws IOException {
    requireNonemptyParameter(articleId);
      Map<?, ?> articleMetaData = addCommonModelAttributes(request, model, site, articleId);
      validateArticleVisibility(site, articleMetaData);
      return site + "/ftl/article/citationDownload";
  }

  @RequestMapping(name = "downloadRisCitation", value = "/article/citation/ris")
  public ResponseEntity<String> serveRisCitationDownload(@SiteParam Site site, @RequestParam("id") String articleId)
      throws IOException {
    return serveCitationDownload(site, articleId, "ris", "application/x-research-info-systems",
        citationDownloadService::buildRisCitation);
  }

  @RequestMapping(name = "downloadBibtexCitation", value = "/article/citation/bibtex")
  public ResponseEntity<String> serveBibtexCitationDownload(@SiteParam Site site, @RequestParam("id") String articleId)
      throws IOException {
    return serveCitationDownload(site, articleId, "bib", "application/x-bibtex",
        citationDownloadService::buildBibtexCitation);
  }

  private ResponseEntity<String> serveCitationDownload(Site site, String articleId,
                                                       String fileExtension, String contentType,
                                                       Function<Map<String, ?>, String> serviceFunction)
      throws IOException {
    requireNonemptyParameter(articleId);
    Map<?, ?> articleMetadata = requestArticleMetadata(articleId);
    validateArticleVisibility(site, articleMetadata);
    String citationBody = serviceFunction.apply((Map<String, ?>) articleMetadata);
    String contentDispositionValue = String.format("attachment; filename=\"%s.%s\"",
            URLEncoder.encode(DoiSchemeStripper.strip((String) articleMetadata.get("doi")), Charsets.UTF_8.toString()),
            fileExtension);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE, contentType);
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
      @RequestParam("id") String articleId) throws IOException {
    requireNonemptyParameter(articleId);
    Map<?, ?> articleMetadata = addCommonModelAttributes(request, model, site, articleId);
    validateArticleVisibility(site, articleMetadata);
    String recaptchaPublicKey = site.getTheme().getConfigMap("captcha").get("publicKey").toString();
    model.addAttribute("recaptchaPublicKey", recaptchaPublicKey);
    return site + "/ftl/article/relatedContent";
  }

  /**
   * Serves as a POST endpoint to submit media curation requests
   *
   * @param model     data passed in from the view
   * @param site      current site
   * @return path to the template
   * @throws IOException
   */
  @RequestMapping(name = "submitMediaCurationRequest", value = "/article/submitMediaCurationRequest", method = RequestMethod.POST)
  public @ResponseBody String submitMediaCurationRequest(HttpServletRequest request, Model model, @SiteParam Site site,
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
   * @param model data passed in from the view
   * @param link link pointing to media content relating to the article
   * @param name name of the user submitting the media curation request
   * @param email email of the user submitting the media curation request
   * @param site current site
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
                                                       @RequestParam("id") String articleId) throws IOException {
    requireNonemptyParameter(articleId);
    Map<?, ?> articleMetadata = requestArticleMetadata(articleId);
    validateArticleVisibility(site, articleMetadata);
    List<ImmutableMap<String, String>> articleFigsAndTables = articleService.getArticleFiguresAndTables(articleMetadata);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    return new ResponseEntity<>(articleFigsAndTables, headers, HttpStatus.OK);
  }

  @RequestMapping(name = "email", value = "/article/email")
  public String renderEmailThisArticle(HttpServletRequest request, Model model, @SiteParam Site site,
      @RequestParam("id") String articleId) throws IOException {
    requireNonemptyParameter(articleId);
    Map<?, ?> articleMetadata = addCommonModelAttributes(request, model, site, articleId);
    validateArticleVisibility(site, articleMetadata);
    model.addAttribute("maxEmails", MAX_TO_EMAILS);
    model.addAttribute("captchaHTML", captchaService.getCaptchaHTML(site));
    return site + "/ftl/article/email";
  }

  /**
   * @param model     data passed in from the view
   * @param site      current site
   * @return path to the template
   * @throws IOException
   */
  @RequestMapping(name = "emailPost", value = "/article/email", method = RequestMethod.POST)
  public String emailArticle(HttpServletRequest request, HttpServletResponse response, Model model,
      @SiteParam Site site,
      @RequestParam("id") String articleId,
      @RequestParam("articleUri") String articleUri,
      @RequestParam("emailToAddresses") String emailToAddresses,
      @RequestParam("emailFrom") String emailFrom,
      @RequestParam("senderName") String senderName,
      @RequestParam("note") String note,
      @RequestParam(RECAPTCHA_CHALLENGE_FIELD) String captchaChallenge,
      @RequestParam(RECAPTCHA_RESPONSE_FIELD) String captchaResponse)
      throws IOException, MessagingException {
    requireNonemptyParameter(articleId);
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

    Map<?, ?> articleMetadata = addCommonModelAttributes(request, model, site, articleId);
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
   * Loads article metadata from the SOA layer.
   *
   * @param articleId DOI identifying the article
   * @return Map of JSON representing the article
   * @throws IOException
   */
  private Map<?, ?> requestArticleMetadata(String articleId) throws IOException {
    Map<?, ?> articleMetadata;
    try {
      articleMetadata = articleService.requestArticleMetadata(articleId, false);
    } catch (EntityNotFoundException enfe) {
      throw new ArticleNotFoundException(articleId);
    }
    return articleMetadata;
  }

  /**
   * Appends additional info about article authors to the model.
   *
   * @param model model to be passed to the view
   * @param doi   identifies the article
   * @return the list of authors appended to the model
   * @throws IOException
   */
  private void requestAuthors(Model model, String doi) throws IOException {
    Map<?,?> allAuthorsData = soaService.requestObject(String.format("articles/%s?authors", doi), Map.class);
    List<?> authors = (List<?>) allAuthorsData.get("authors");
    model.addAttribute("authors", authors);

    // Putting this here was a judgement call.  One could make the argument that this logic belongs
    // in Rhino, but it's so simple I elected to keep it here for now.
    List<String> equalContributors = new ArrayList<>();
    ListMultimap<String, String> authorAffiliationsMap = LinkedListMultimap.create();
    for (Object o : authors) {
      Map<String, Object> author = (Map<String, Object>) o;
      String fullName = (String) author.get("fullName");

      List<String> affiliations = (List<String>) author.get("affiliations");
      for (String affiliation : affiliations) {
        authorAffiliationsMap.put(affiliation, fullName);
      }

      Object obj = author.get("equalContrib");
      if (obj != null && (boolean) obj) {
        equalContributors.add(fullName);
      }

      // remove the footnote marker from the current address
      List<String> currentAddresses = (List<String>) author.get("currentAddresses");
      for (ListIterator<String> iterator = currentAddresses.listIterator(); iterator.hasNext(); ) {
        String currentAddress = iterator.next();
        iterator.set(TextUtil.removeFootnoteMarker(currentAddress));
      }
    }

    //Create comma-separated list of authors per affiliation
    LinkedHashMap<String, String> authorListAffiliationMap = new LinkedHashMap<>();
    for (Map.Entry<String, Collection<String>> affiliation : authorAffiliationsMap.asMap().entrySet()) {
      authorListAffiliationMap.put(affiliation.getKey(), Joiner.on(", ").join(affiliation.getValue()));
    }

    model.addAttribute("authorListAffiliationMap", authorListAffiliationMap);
    model.addAttribute("authorContributions", allAuthorsData.get("authorContributions"));
    model.addAttribute("competingInterests", allAuthorsData.get("competingInterests"));
    model.addAttribute("correspondingAuthors", allAuthorsData.get("correspondingAuthorList"));
    model.addAttribute("equalContributors", equalContributors);
  }

  /**
   * Build the path to request the article XML asset for an article.
   *
   * @return the service path to the correspond article XML asset file
   */
  private static String getArticleXmlAssetPath(RenderContext renderContext) {
    return "articles/" + Preconditions.checkNotNull(renderContext.getArticleId()) + "?xml";
  }

  /**
   * Retrieve and transform the body of an amendment article from its XML file. The returned value is cached.
   *
   * @return the body of the amendment article, transformed into HTML for display in a notice on the amended article
   */
  private String getAmendmentBody(final RenderContext renderContext) throws IOException {

    String cacheKey = "amendmentBody:" + Preconditions.checkNotNull(renderContext.getArticleId());
    String xmlAssetPath = getArticleXmlAssetPath(renderContext);

    return soaService.requestCachedStream(CacheParams.create(cacheKey), xmlAssetPath, stream -> {

      // Extract the "/article/body" element from the amendment XML, not to be confused with the HTML <body> element.
      String bodyXml = xmlService.extractElement(stream, "body");
      try {
        return articleTransformService.transformExcerpt(renderContext, bodyXml, null);
      } catch (TransformerException e) {
        throw new RuntimeException(e);
      }
    });
  }

  /**
   * Retrieves article XML from the SOA server, transforms it into HTML, and returns it. Result will be stored in
   * memcache.
   *
   * @return String of the article HTML
   * @throws IOException
   */
  private String getArticleHtml(final RenderContext renderContext) throws IOException {

    String cacheKey = String.format("html:%s:%s",
        Preconditions.checkNotNull(renderContext.getSite()), renderContext.getArticleId());
    String xmlAssetPath = getArticleXmlAssetPath(renderContext);

    return soaService.requestCachedStream(CacheParams.create(cacheKey), xmlAssetPath, stream -> {
      StringWriter articleHtml = new StringWriter(XFORM_BUFFER_SIZE);
      try (OutputStream outputStream = new WriterOutputStream(articleHtml, charset)) {
        articleTransformService.transform(renderContext, stream, outputStream);
      } catch (TransformerException e) {
        throw new RuntimeException(e);
      }
      return articleHtml.toString();
    });
  }

    private Map<?, ?> addCommonModelAttributes(HttpServletRequest request, Model model, @SiteParam Site site, @RequestParam("id") String articleId) throws IOException {
        Map<?, ?> articleMetadata = requestArticleMetadata(articleId);
        addCrossPublishedJournals(request, model, site, articleMetadata);
        model.addAttribute("article", articleMetadata);
        model.addAttribute("containingLists", getContainingArticleLists(articleId, site));
        model.addAttribute("categoryTerms", getCategoryTerms(articleMetadata));
        requestAuthors(model, articleId);
        return articleMetadata;
    }

}
