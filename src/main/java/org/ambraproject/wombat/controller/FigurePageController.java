package org.ambraproject.wombat.controller;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Closer;
import org.ambraproject.wombat.service.ArticleNotFoundException;
import org.ambraproject.wombat.service.ArticleTransformService;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.service.SoaService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

@Controller
public class FigurePageController {

  @Autowired
  private SoaService soaService;
  @Autowired
  private ArticleTransformService articleTransformService;

  /**
   * For each figure in {@code article.assets.figures}, apply the site's article transformation to the figure's {@code
   * description} member and store the result in a new {@code descriptionHtml} member.
   *
   * @param site            the key of the site whose article transformation should be applied
   * @param articleMetadata the article metadata object (per the service API's JSON response) whose figure metadata is
   *                        to be read and added to
   */
  private void transformFigureDescriptions(String site, Map<String, Object> articleMetadata) {
    Map<String, Object> assets = (Map<String, Object>) articleMetadata.get("assets");
    List<Map<String, Object>> figureMetadataList = (List<Map<String, Object>>) assets.get("figures");

    for (Map<String, Object> figureMetadata : figureMetadataList) {
      String description = (String) figureMetadata.get("description");
      try {
        String descriptionHtml = articleTransformService.transformExcerpt(site, description, "desc");
        figureMetadata.put("descriptionHtml", descriptionHtml);
      } catch (TransformerException e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Serve a page listing all figures for an article.
   */
  @RequestMapping("/{site}/article/figures")
  public String renderFiguresPage(Model model,
                                  @PathVariable("site") String site,
                                  @RequestParam("doi") String articleId)
      throws IOException {
    Map<String, Object> articleMetadata;
    try {
      articleMetadata = soaService.requestObject("articles/" + articleId, Map.class);
    } catch (EntityNotFoundException enfe) {
      throw new ArticleNotFoundException(articleId);
    }
    model.addAttribute("article", articleMetadata);
    transformFigureDescriptions(site, articleMetadata);

    return site + "/ftl/article/figures";
  }

  /**
   * Serve a page displaying a single figure.
   */
  @RequestMapping("/{site}/article/figure")
  public String renderFigurePage(Model model,
                                 @PathVariable("site") String site,
                                 @RequestParam("id") String figureId)
      throws IOException {
    Map<String, Object> figureMetadata;
    try {
      figureMetadata = soaService.requestObject("assets/" + figureId + "?figure", Map.class);
    } catch (EntityNotFoundException enfe) {
      throw new ArticleNotFoundException(figureId);
    }
    model.addAttribute("figure", figureMetadata);

    String parentArticleId = (String) figureMetadata.get("parentArticleId");
    model.addAttribute("article", ImmutableMap.of("doi", parentArticleId));

    return site + "/ftl/article/figure";
  }

  /**
   * Serve an asset file as the response body. Forward a stream from the SOA.
   */
  @RequestMapping("/{site}/article/asset")
  public void serveAsset(HttpServletResponse response,
                         @PathVariable("site") String site,
                         @RequestParam("id") String assetId)
      throws IOException {
    // TODO: Set response headers

    Closer closer = Closer.create();
    try {
      InputStream assetStream = soaService.requestStream("assetfiles/" + assetId);
      if (assetStream == null) {
        throw new EntityNotFoundException(assetId);
      }
      closer.register(assetStream);
      OutputStream responseStream = closer.register(response.getOutputStream());
      IOUtils.copy(assetStream, responseStream); // buffered
    } catch (Throwable t) {
      throw closer.rethrow(t);
    } finally {
      closer.close();
    }
  }

}
