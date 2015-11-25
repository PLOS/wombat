package org.ambraproject.wombat.controller;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
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
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.site.url.Link;
import org.ambraproject.wombat.service.ArticleService;
import org.ambraproject.wombat.service.ArticleTransformService;
import org.ambraproject.wombat.service.CitationDownloadService;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.service.RenderContext;
import org.ambraproject.wombat.service.UnmatchedSiteException;
import org.ambraproject.wombat.service.remote.CacheDeserializer;
import org.ambraproject.wombat.service.remote.SoaService;
import org.ambraproject.wombat.util.CacheParams;
import org.ambraproject.wombat.util.DoiSchemeStripper;
import org.ambraproject.wombat.util.TextUtil;
import org.apache.commons.io.output.WriterOutputStream;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.SortedMap;

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
  private CitationDownloadService citationDownloadService;

  @RequestMapping(name = "article", value = "/article")
  public String renderArticle(HttpServletRequest request,
                              Model model,
                              @SiteParam Site site,
                              @RequestParam("id") String articleId)
      throws IOException {

    // TODO: this method currently makes 5 backend RPCs, all sequentially.
    // Explore reducing this number, or doing them in parallel, if this is
    // a performance bottleneck.

    requireNonemptyParameter(articleId);
    Map<?, ?> articleMetadata = requestArticleMetadata(articleId);
    validateArticleVisibility(site, articleMetadata);
    RenderContext renderContext = new RenderContext(site);
    renderContext.setArticleId(articleId);

    String articleHtml = getArticleHtml(renderContext);
    model.addAttribute("article", articleMetadata);
    model.addAttribute("categoryTerms", getCategoryTerms(articleMetadata));
    model.addAttribute("articleText", articleHtml);
    model.addAttribute("amendments", fillAmendments(site, articleMetadata));
    model.addAttribute("containingLists", getContainingArticleLists(articleId, site));

    addCrossPublishedJournals(request, model, site, articleMetadata);
    requestAuthors(model, articleId);
    requestComments(model, articleId);
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
  public String renderArticleComments(Model model, @SiteParam Site site,
                                      @RequestParam("id") String articleId) throws IOException {
    requireNonemptyParameter(articleId);
    Map<?, ?> articleMetadata = requestArticleMetadata(articleId);
    validateArticleVisibility(site, articleMetadata);
    model.addAttribute("article", articleMetadata);
    requestComments(model, articleId);
    return site + "/ftl/article/comments";
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
        Site crossPublishedSite;
        try {
          crossPublishedSite = site.getTheme().resolveForeignJournalKey(siteSet, crossPublishedJournalKey);
        } catch (UnmatchedSiteException e) {
          // The data may still be valid if the other journal is hosted on a legacy Ambra system
          log.warn("Cross-published journal with no matching site: {}", crossPublishedJournalKey);
          crossPublishedSite = null; // Still show the title, but without the link
        }
        if (crossPublishedSite != null) {
          // Set up an href link to the other site's homepage
          String homepageLink = Link.toForeignSite(site, crossPublishedSite).toPath("/").get(request);
          crossPublishedJournalMetadata.put("href", homepageLink);

          // Look up whether the other site wants its journal title italicized
          // (This isn't a big deal because it's only one value, but if similar display details pile up
          // in the future, it would be better to abstract them out than to handle them all individually here.)
          boolean italicizeTitle = (boolean) crossPublishedSite.getTheme().getConfigMap("journal").get("italicizeTitle");
          crossPublishedJournalMetadata.put("italicizeTitle", italicizeTitle);
        }

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
  public String renderArticleCommentTree(Model model, @SiteParam Site site,
                                         @RequestParam("id") String commentId) throws IOException {
    requireNonemptyParameter(commentId);
    Map<String, Object> comment;
    try {
      comment = soaService.requestObject(String.format("comments/" + commentId), Map.class);
    } catch (EntityNotFoundException enfe) {
      throw new NotFoundException(enfe);
    }
    comment = DoiSchemeStripper.strip(comment, "articleDoi");
    validateArticleVisibility(site, (Map<?, ?>) comment.get("parentArticle"));

    model.addAttribute("comment", comment);
    model.addAttribute("articleDoi", comment.get("articleDoi"));
    return site + "/ftl/article/comment";
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
  public String renderArticleAuthors(Model model, @SiteParam Site site,
                                     @RequestParam("id") String articleId) throws IOException {
    Map<?, ?> articleMetadata = requestArticleMetadata(articleId);
    validateArticleVisibility(site, articleMetadata);
    model.addAttribute("article", articleMetadata);
    requestAuthors(model, articleId);
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
    enforceDevFeature("metricsTab");     // TODO: remove when ready to expose page in prod
    Map<?, ?> articleMetadata = requestArticleMetadata(articleId);
    validateArticleVisibility(site, articleMetadata);
    model.addAttribute("article", articleMetadata);
    model.addAttribute("containingLists", getContainingArticleLists(articleId, site));
    model.addAttribute("categoryTerms", getCategoryTerms(articleMetadata));
    addCrossPublishedJournals(request, model, site, articleMetadata);
    requestAuthors(model, articleId);
    return site + "/ftl/article/metrics";
  }

  @RequestMapping(name = "relatedContent", value = "/article/related")
  public String renderRelatedContent() {
    throw new NotFoundException(); // TODO Implement
  }


  @RequestMapping(name = "citationDownloadPage", value = "/article/citation")
  public String renderCitationDownloadPage(Model model, @SiteParam Site site,
                                           @RequestParam("id") String articleId)
      throws IOException {
    enforceDevFeature("citationDownload"); // TODO: remove when ready to expose page in prod
    requireNonemptyParameter(articleId);
    Map<?, ?> articleMetadata = requestArticleMetadata(articleId);
    validateArticleVisibility(site, articleMetadata);
    requestAuthors(model, (String) articleMetadata.get("doi"));
    model.addAttribute("article", articleMetadata);
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
    enforceDevFeature("citationDownload"); // TODO: remove when ready to expose page in prod
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
   * Checks whether any comments are associated with the given article, and appends them to the model if so.
   *
   * @param model model to be passed to the view
   * @param doi   identifies the article
   * @throws IOException
   */
  private void requestComments(Model model, String doi) throws IOException {
    List<?> comments = soaService.requestObject(String.format("articles/%s?comments", doi), List.class);
    model.addAttribute("articleComments", comments);
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
    List<?> authors = soaService.requestObject(String.format("articles/%s?authors", doi), List.class);
    model.addAttribute("authors", authors);

    // Putting this here was a judgement call.  One could make the argument that this logic belongs
    // in Rhino, but it's so simple I elected to keep it here for now.
    List<String> correspondingAuthors = new ArrayList<>();
    List<String> equalContributors = new ArrayList<>();
    for (Object o : authors) {
      Map<String, Object> author = (Map<String, Object>) o;
      if (author.containsKey("corresponding")) {
        correspondingAuthors.add((String) author.get("corresponding"));
      }
      Object obj = author.get("equalContrib");
      if (obj != null && (boolean) obj) {
        equalContributors.add((String) author.get("fullName"));
      }

      // remove the footnote marker from the current address
      List<String> currentAddresses = (List<String>) author.get("currentAddresses");
      for (ListIterator<String> iterator = currentAddresses.listIterator(); iterator.hasNext(); ) {
        String currentAddress = iterator.next();
        iterator.set(TextUtil.removeFootnoteMarker(currentAddress));
      }
    }

    model.addAttribute("correspondingAuthors", correspondingAuthors);
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

    return soaService.requestCachedStream(CacheParams.create(cacheKey), xmlAssetPath, new CacheDeserializer<InputStream, String>() {
      @Override
      public String read(InputStream stream) throws IOException {
        Document document;
        try {
          DocumentBuilder documentBuilder; // not thread-safe
          try {
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
          } catch (ParserConfigurationException e) {
            throw new RuntimeException(e); // using default configuration; should be impossible
          }

          try {
            document = documentBuilder.parse(stream);
          } catch (SAXException e) {
            throw new RuntimeException("Invalid XML syntax for: " + renderContext.getArticleId(), e);
          }
        } finally {
          stream.close();
        }

        // Extract the "/article/body" element from the amendment XML, not to be confused with the HTML <body> element.
        Node bodyNode = document.getElementsByTagName("body").item(0);

        // Convert XML excerpt to renderable HTML.
        // TODO: Transform without intermediate buffering into String?
        String bodyXml = TextUtil.recoverXml(bodyNode);
        try {
          return articleTransformService.transformExcerpt(renderContext, bodyXml, null);
        } catch (TransformerException e) {
          throw new RuntimeException(e);
        }
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

    return soaService.requestCachedStream(CacheParams.create(cacheKey), xmlAssetPath, new CacheDeserializer<InputStream, String>() {
      @Override
      public String read(InputStream stream) throws IOException {
        StringWriter articleHtml = new StringWriter(XFORM_BUFFER_SIZE);
        try (OutputStream outputStream = new WriterOutputStream(articleHtml, charset)) {
          articleTransformService.transform(renderContext, stream, outputStream);
        } catch (TransformerException e) {
          throw new RuntimeException(e);
        }
        return articleHtml.toString();
      }
    });
  }

}
