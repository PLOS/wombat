package org.ambraproject.wombat.controller;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.Closer;
import org.ambraproject.wombat.service.ArticleNotFoundException;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

@Controller
public class FigurePageController {

  @Autowired
  private SoaService soaService;

  /*
   * Dereference keys given in "article.figures" to find assets from "article.assets".
   * Set them up in a new object that can be accessed more easily from FreeMarker.
   *
   * TODO: Do this entirely in FreeMarker?
   */
  private static List<?> buildFigureViewList(Map<?, ?> articleMetadata) {
    Map<?, ?> assets = (Map<?, ?>) articleMetadata.get("assets");
    List<Map<?, ?>> figureMetadataList = (List<Map<?, ?>>) articleMetadata.get("figures");

    List<Map<String, ?>> figures = Lists.newArrayListWithCapacity(figureMetadataList.size());
    for (Map<?, ?> figureMetadata : figureMetadataList) {
      String assetId = (String) figureMetadata.get("id");
      Map<?, ?> asset = (Map<?, ?>) assets.get(assetId);
      Object originalAsset = asset.get(figureMetadata.get("original"));

      List<String> thumbnailAssetIds = (List<String>) figureMetadata.get("thumbnails");
      List<Object> thumbnailAssets = Lists.newArrayListWithCapacity(thumbnailAssetIds.size());
      for (String thumbnailAssetId : thumbnailAssetIds) {
        Object thumbnailAsset = asset.get(thumbnailAssetId);
        thumbnailAssets.add(thumbnailAsset);
      }

      Map<String, ?> figure = ImmutableMap.<String, Object>builder()
          .put("id", assetId)
          .put("original", originalAsset)
          .put("thumbnails", thumbnailAssets)
          .build();
      figures.add(figure);
    }

    return figures;
  }

  /**
   * Serve a page listing all figures for an article.
   */
  @RequestMapping("/{journal}/article/figures")
  public String renderFiguresPage(Model model,
                                  @PathVariable("journal") String journal,
                                  @RequestParam("doi") String articleId)
      throws IOException {
    Map<?, ?> articleMetadata;
    try {
      articleMetadata = soaService.requestObject("articles/" + articleId, Map.class);
    } catch (EntityNotFoundException enfe) {
      throw new ArticleNotFoundException(articleId);
    }
    model.addAttribute("article", articleMetadata);
    model.addAttribute("figures", buildFigureViewList(articleMetadata));

    return journal + "/ftl/article/figures";
  }

  /**
   * Serve a page displaying a single figure.
   */
  @RequestMapping("/{journal}/article/figure")
  public String renderFigurePage(Model model,
                                 @PathVariable("journal") String journal,
                                 @RequestParam("id") String figureId)
      throws IOException {
    Map<?, ?> figureMetadata;
    try {
      figureMetadata = soaService.requestObject("assets/" + figureId + "?figure", Map.class);
    } catch (EntityNotFoundException enfe) {
      throw new ArticleNotFoundException(figureId);
    }
    model.addAttribute("figure", figureMetadata);

    String parentArticleId = (String) figureMetadata.get("parentArticleId");
    model.addAttribute("article", ImmutableMap.of("doi", parentArticleId));

    return journal + "/ftl/article/figure";
  }

  /**
   * Serve an asset file as the response body. Forward a stream from the SOA.
   */
  @RequestMapping("/{journal}/article/asset")
  public void serveAsset(HttpServletResponse response,
                         @PathVariable("journal") String journal,
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
