package org.ambraproject.wombat.controller;

import com.google.common.base.Preconditions;
import com.google.common.io.Closer;
import org.ambraproject.rhombat.cache.Cache;
import org.ambraproject.wombat.service.ArticleNotFoundException;
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
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

    Map<?, ?> articleMetadata = requestArticleMetadata(articleId);
    String articleHtml;
    try {
      articleHtml = getArticleHtml(articleId, site);
    } catch (EntityNotFoundException enfe) {
      throw new ArticleNotFoundException(articleId);
    }
    model.addAttribute("article", articleMetadata);
    model.addAttribute("articleText", articleHtml);
    requestAuthors(model, articleId);
    requestCorrections(model, articleId);
    requestComments(model, articleId);
    return site + "/ftl/article/article";
  }

  /**
   * Serves a request for a list of all the root-level comments associated with an article.
   *
   * @param model data to pass to the view
   * @param site current site
   * @param articleId specifies the article
   * @return path to the template
   * @throws IOException
   */
  @RequestMapping("/{site}/article/comments")
  public String renderArticleComments(Model model, @PathVariable("site") String site,
                                      @RequestParam("doi") String articleId) throws IOException {
    Map<?, ?> articleMetadata = requestArticleMetadata(articleId);
    model.addAttribute("article", articleMetadata);
    requestComments(model, articleId);
    return site + "/ftl/article/comments";
  }

  /**
   * Serves a request for a list of all the corrections associated with an article.
   *
   * @param model data to pass to the view
   * @param site current site
   * @param articleId specifies the article
   * @return path to the template
   * @throws IOException
   */
  @RequestMapping("/{site}/article/corrections")
  public String renderArticleCorrections(Model model, @PathVariable("site") String site,
      @RequestParam("doi") String articleId) throws IOException {
    Map<?, ?> articleMetadata = requestArticleMetadata(articleId);
    model.addAttribute("article", articleMetadata);
    requestCorrections(model, articleId);
    return site + "/ftl/article/corrections";
  }

  /**
   * Serves a request for an expanded view of a single comment and any replies.
   *
   * @param model data to pass to the view
   * @param site current site
   * @param commentUri specifies the comment
   * @return path to the template
   * @throws IOException
   */
  @RequestMapping("/{site}/article/comment")
  public String renderArticleCommentTree(Model model, @PathVariable("site") String site,
      @RequestParam("uri") String commentUri) throws IOException {
    Map<?, ?> comment = soaService.requestObject(String.format("comments/" + commentUri), Map.class);
    model.addAttribute("comment", comment);
    model.addAttribute("articleDoi", comment.get("articleDoi"));
    return site + "/ftl/article/comment";
  }

  /**
   * Serves a request for an expanded view of a single correction and any replies.
   *
   * @param model data to pass to the view
   * @param site current site
   * @param correctionUri specifies the correction
   * @return path to the template
   * @throws IOException
   */
  @RequestMapping("/{site}/article/correction")
  public String renderArticleCorrectionTree(Model model, @PathVariable("site") String site,
      @RequestParam("uri") String correctionUri) throws IOException {
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
   * @param model data to pass to the view
   * @param site current site
   * @param articleId specifies the article
   * @return path to the template
   * @throws IOException
   */
  @RequestMapping("/{site}/article/authors")
  public String renderArticleAuthors(Model model, @PathVariable("site") String site,
      @RequestParam("doi") String articleId) throws IOException {
    Map<?, ?> articleMetadata = requestArticleMetadata(articleId);
    model.addAttribute("article", articleMetadata);
    List authors = requestAuthors(model, articleId);
    model.addAttribute("correspondingAuthors", getCorrespondingAuthors(authors));
    return site + "/ftl/article/authors";
  }

  /**
   * Extracts the corresponding authors strings (usually emails) from the authors data structure.
   * <p/>
   * Putting this here was a judgement call.  One could make the argument that this logic belongs
   * in Rhino, but it's so simple I elected to keep it here for now.
   *
   * @param authors deserialized JSON for all authors for the article
   * @return list of "corresponding" fields for all authors that possess such a field
   */
  private List<String> getCorrespondingAuthors(List authors) {
    List<String> results = new ArrayList<>();
    for (Object o : authors) {
      Map<?, ?> author = (Map<?, ?>) o;
      if (author.containsKey("corresponding")) {
        results.add((String) author.get("corresponding"));
      }
    }
    return results;
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
      articleMetadata = soaService.requestObject("articles/" + articleId, Map.class);
    } catch (EntityNotFoundException enfe) {
      throw new ArticleNotFoundException(articleId);
    }
    return articleMetadata;
  }

  /**
   * Checks whether any corrections are associated with the given article, and appends them to the model if so.
   *
   * @param model model to be passed to the view
   * @param doi   identifies the article
   * @throws IOException
   */
  private void requestCorrections(Model model, String doi) throws IOException {
    List<?> corrections = soaService.requestObject(String.format("articles/%s?corrections", doi), List.class);
    if (corrections != null && !corrections.isEmpty()) {
      model.addAttribute("articleCorrections", corrections);

      // On the main article page, we only display formal corrections, so we have a separate
      // entry for these.
      List<Map> formalCorrections = new ArrayList<>();
      for (Object o : corrections) {
        Map correction = (Map) o;
        if ("FORMAL_CORRECTION".equals(correction.get("type"))) {
          formalCorrections.add(correction);
        }
      }
      model.addAttribute("formalCorrections", formalCorrections);
    }
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
   * @param doi identifies the article
   * @return the list of authors appended to the model
   * @throws IOException
   */
  private List requestAuthors(Model model, String doi) throws IOException {
    List<?> authors = soaService.requestObject(String.format("articles/%s?authors", doi), List.class);
    if (authors != null && !authors.isEmpty()) {
      model.addAttribute("authors", authors);
    }
    return authors;
  }

  /**
   * Retrieves article XML from the SOA server, transforms it into HTML, and returns it.
   * Result will be stored in memcache.
   *
   * @param articleId identifies the article
   * @param site identifies the journal site
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
      Closer closer = Closer.create();
      try {
        OutputStream outputStream = closer.register(new WriterOutputStream(articleHtml, charset));
        articleTransformService.transform(site, new ByteArrayInputStream(fromServer.result.getBytes()), outputStream);
      } catch (Throwable t) {
        throw closer.rethrow(t);
      } finally {
        closer.close();
      }
      fromServer.result = articleHtml.toString();
      cache.put(cacheKey, fromServer);
      return fromServer.result;

    } else {
      return cached.result;
    }
  }
}
