package org.ambraproject.wombat.controller;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import org.ambraproject.rhombat.cache.Cache;
import org.ambraproject.wombat.service.ArticleTransformService;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.service.SoaService;
import org.apache.commons.io.output.WriterOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * Controller for rendering an article.
 */
@Controller
public class ArticleController extends WombatController {

  /**
   * Initial size (in bytes) of buffer that holds transformed article HTML before passing it to the model.
   */
  private static final int XFORM_BUFFER_SIZE = 0x8000;

  @Autowired
  private Charset charset;
  @Autowired
  private SoaService soaService;
  @Autowired
  private ArticleTransformService articleTransformService;
  @Autowired
  private Cache cache;

  @RequestMapping("/{site}/article")
  public String renderArticle(Model model,
                              @PathVariable("site") String site,
                              @RequestParam("doi") String articleId)
      throws IOException {

    // TODO: this method currently makes 5 backend RPCs, all sequentially.
    // Explore reducing this number, or doing them in parallel, if this is
    // a performance bottleneck.

    requireNonemptyParameter(articleId);
    Map<?, ?> articleMetadata = requestArticleMetadata(articleId);
    String articleHtml;
    try {
      articleHtml = getArticleHtml(articleId, site);
    } catch (EntityNotFoundException enfe) {
      throw new ArticleNotFoundException(articleId);
    }
    model.addAttribute("article", articleMetadata);
    model.addAttribute("articleText", articleHtml);
    model.addAttribute("amendments", fillAmendments(articleMetadata));
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
  @RequestMapping("/{site}/article/comments")
  public String renderArticleComments(Model model, @PathVariable("site") String site,
                                      @RequestParam("doi") String articleId) throws IOException {
    requireNonemptyParameter(articleId);
    Map<?, ?> articleMetadata = requestArticleMetadata(articleId);
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
        });
  }

  /**
   * Check related articles for ones that amend this article and return them for special display.
   *
   * @param articleMetadata the article metadata
   * @return a map from amendment type labels to related article objects
   */
  private static Map<String, List<Object>> fillAmendments(Map<?, ?> articleMetadata) {
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
    return Multimaps.asMap(amendments);
  }

  /**
   * Apply the display logic for different amendment types taking precedence over each other.
   * <p/>
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
   * @param model      data to pass to the view
   * @param site       current site
   * @param commentUri specifies the comment
   * @return path to the template
   * @throws IOException
   */
  @RequestMapping("/{site}/article/comment")
  public String renderArticleCommentTree(Model model, @PathVariable("site") String site,
                                         @RequestParam("uri") String commentUri) throws IOException {
    requireNonemptyParameter(commentUri);
    Map<?, ?> comment = soaService.requestObject(String.format("comments/" + commentUri), Map.class);
    model.addAttribute("comment", comment);
    model.addAttribute("articleDoi", comment.get("articleDoi"));
    return site + "/ftl/article/comment";
  }

  /**
   * Serves a request for an expanded view of a single correction and any replies.
   *
   * @param model         data to pass to the view
   * @param site          current site
   * @param correctionUri specifies the correction
   * @return path to the template
   * @throws IOException
   */
  @RequestMapping("/{site}/article/correction")
  public String renderArticleCorrectionTree(Model model, @PathVariable("site") String site,
                                            @RequestParam("uri") String correctionUri) throws IOException {
    requireNonemptyParameter(correctionUri);
    Map<?, ?> correction = soaService.requestObject(String.format("corrections/" + correctionUri), Map.class);

    // Currently we use the same UI for both a comment and a correction, and they
    // share the same backend representations.  This may not always be the case.
    model.addAttribute("comment", correction);
    model.addAttribute("articleDoi", correction.get("articleDoi"));
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
  @RequestMapping("/{site}/article/authors")
  public String renderArticleAuthors(Model model, @PathVariable("site") String site,
                                     @RequestParam("doi") String articleId) throws IOException {
    Map<?, ?> articleMetadata = requestArticleMetadata(articleId);
    model.addAttribute("article", articleMetadata);
    requestAuthors(model, articleId);
    return site + "/ftl/article/authors";
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
      articleMetadata = soaService.requestArticleMetadata(articleId);
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
    if (comments != null && !comments.isEmpty()) {
      model.addAttribute("articleComments", comments);
    }
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
      Map<?, ?> author = (Map<?, ?>) o;
      if (author.containsKey("corresponding")) {
        correspondingAuthors.add((String) author.get("corresponding"));
      }
      Object obj = author.get("equalContrib");
      if (obj != null && (boolean) obj) {
        equalContributors.add((String) author.get("fullName"));
      }
    }
    model.addAttribute("correspondingAuthors", correspondingAuthors);
    model.addAttribute("equalContributors", equalContributors);
  }

  /**
   * Retrieves article XML from the SOA server, transforms it into HTML, and returns it. Result will be stored in
   * memcache.
   *
   * @param articleId identifies the article
   * @param site      identifies the journal site
   * @return String of the article HTML
   * @throws IOException
   */
  private String getArticleHtml(String articleId, String site) throws IOException {
    Preconditions.checkNotNull(articleId);
    Preconditions.checkNotNull(site);
    String cacheKey = "html:" + articleId;
    SoaService.IfModifiedSinceResult<String> cached = cache.get(cacheKey);
    Calendar lastModified;
    if (cached == null) {
      lastModified = Calendar.getInstance();
      lastModified.setTimeInMillis(0);  // Set to beginning of epoch since it's not in the cache
    } else {
      lastModified = cached.lastModified;
    }

    String xmlAssetPath = "assetfiles/" + articleId + ".xml";
    SoaService.IfModifiedSinceResult<String> fromServer = soaService.requestObjectIfModifiedSince(xmlAssetPath,
        String.class, lastModified);
    if (fromServer.result != null) {
      StringWriter articleHtml = new StringWriter(XFORM_BUFFER_SIZE);
      try (OutputStream outputStream = new WriterOutputStream(articleHtml, charset)) {
        articleTransformService.transform(site, new ByteArrayInputStream(fromServer.result.getBytes()), outputStream);
      } catch (TransformerException e) {
        throw new RuntimeException(e);
      }
      fromServer.result = articleHtml.toString();
      cache.put(cacheKey, fromServer);
      return fromServer.result;

    } else {
      return cached.result;
    }
  }
}
