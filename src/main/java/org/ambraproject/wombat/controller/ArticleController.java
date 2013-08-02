package org.ambraproject.wombat.controller;

import com.google.common.base.Charsets;
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

  private static final Charset CHARSET = Charsets.UTF_8;

  /**
   * Number of bytes we use to buffer responses from the SOA layer.
   */
  private static final int BUFFER_SIZE = 0x8000;

  @Autowired
  private SoaService soaService;

  @Autowired
  private ArticleTransformService articleTransformService;

  @RequestMapping("/{journal}/article")
  public String renderArticle(Model model,
                              @PathVariable("journal") String journal,
                              @RequestParam("doi") String articleId)
      throws IOException {
    String xmlAssetPath = "assetfiles/" + articleId + ".xml";
    Map<?, ?> articleMetadata = requestArticleMetadata(articleId);
    StringWriter articleHtml = new StringWriter(BUFFER_SIZE);
    Closer closer = Closer.create();
    try {
      InputStream articleXml;
      try {
        articleXml = closer.register(new BufferedInputStream(soaService.requestStream(xmlAssetPath)));
      } catch (EntityNotFoundException enfe) {
        throw new ArticleNotFoundException(articleId);
      }
      OutputStream outputStream = closer.register(new WriterOutputStream(articleHtml, CHARSET));
      articleTransformService.transform(journal, articleId, articleXml, outputStream);
    } catch (Throwable t) {
      throw closer.rethrow(t);
    } finally {
      closer.close();
    }
    model.addAttribute("article", articleMetadata);
    model.addAttribute("articleText", articleHtml.toString());
    requestCorrections(model, articleId);
    requestComments(model, articleId);
    return journal + "/ftl/article/article";
  }

  @RequestMapping("/{journal}/article/comments")
  public String renderArticleComments(Model model, @PathVariable("journal") String journal,
                                      @RequestParam("doi") String articleId) throws IOException {
    Map<?, ?> articleMetadata = requestArticleMetadata(articleId);
    model.addAttribute("article", articleMetadata);
    requestComments(model, articleId);
    return journal + "/ftl/article/comments";
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
    List<?> corrections = null;//soaService.requestObject(String.format("articles/%s?corrections", doi), List.class);
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
    List<?> comments = null;//soaService.requestObject(String.format("articles/%s?comments", doi), List.class);
    if (comments != null && !comments.isEmpty()) {
      model.addAttribute("articleComments", comments);
    }
  }
}
