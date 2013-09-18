package org.ambraproject.wombat.controller;

import com.google.common.io.Closer;
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * Controller for rendering an article.
 */
@Controller
public class ArticleController {

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

  @RequestMapping("/{site}/article")
  public String renderArticle(Model model,
                              @PathVariable("site") String site,
                              @RequestParam("doi") String articleId)
      throws IOException {
    String xmlAssetPath = "assetfiles/" + articleId + ".xml";
    Map<?, ?> articleMetadata = requestArticleMetadata(articleId);

    // Can't stream into a FreeMarker template, so transform the whole article into memory
    StringWriter articleHtml = new StringWriter(XFORM_BUFFER_SIZE);

    Closer closer = Closer.create();
    try {
      InputStream articleXml;
      try {
        articleXml = closer.register(new BufferedInputStream(soaService.requestStream(xmlAssetPath)));
      } catch (EntityNotFoundException enfe) {
        throw new ArticleNotFoundException(articleId);
      }
      OutputStream outputStream = closer.register(new WriterOutputStream(articleHtml, charset));
      articleTransformService.transform(site, articleXml, outputStream);
    } catch (Throwable t) {
      throw closer.rethrow(t);
    } finally {
      closer.close();
    }
    model.addAttribute("article", articleMetadata);
    model.addAttribute("articleText", articleHtml.toString());
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
}
