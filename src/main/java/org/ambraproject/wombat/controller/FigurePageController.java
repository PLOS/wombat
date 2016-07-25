package org.ambraproject.wombat.controller;

import com.google.common.collect.ImmutableMap;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.ambraproject.wombat.service.ArticleResolutionService;
import org.ambraproject.wombat.service.ArticleService;
import org.ambraproject.wombat.service.ArticleTransformService;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.service.RenderContext;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.ambraproject.wombat.util.DoiSchemeStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
public class FigurePageController extends WombatController {

  @Autowired
  private ArticleApi articleApi;
  @Autowired
  private ArticleService articleService;
  @Autowired
  private ArticleResolutionService articleResolutionService;
  @Autowired
  private ArticleTransformService articleTransformService;


  /**
   * Serve a page listing all figures for an article.
   */
  @RequestMapping(name = "figuresPage", value = "/article/figures")
  public String renderFiguresPage(Model model, @SiteParam Site site,
                                  RequestedDoiVersion articleId)
      throws IOException {
    Map<?, ?> articleMetadata;
    try {
      articleMetadata = articleService.requestArticleMetadata(articleId, true);
    } catch (EntityNotFoundException enfe) {
      throw new ArticleNotFoundException(articleId);
    }
    validateArticleVisibility(site, articleMetadata);
    model.addAttribute("article", articleMetadata);

    RenderContext renderContext = new RenderContext(site, articleId);
    List<Map<String, Object>> figureMetadataList = (List<Map<String, Object>>) articleMetadata.get("figures");
    for (Map<String, Object> figureMetadata : figureMetadataList) {
      figureMetadata = DoiSchemeStripper.strip(figureMetadata);
      transformFigureDescription(renderContext, figureMetadata);
    }

    return site + "/ftl/article/figures";
  }

  /**
   * Serve a page displaying a single figure.
   */
  @RequestMapping(name = "figurePage", value = "/article/figure")
  public String renderFigurePage(Model model, @SiteParam Site site,
                                 RequestedDoiVersion figureId)
      throws IOException {
    Map<String, Object> articleMetadata;
    try {
      articleMetadata = (Map<String, Object>) articleApi.requestObject(
          articleResolutionService.toIngestion(figureId).build(), Map.class);
    } catch (EntityNotFoundException enfe) {
      throw new ArticleNotFoundException(figureId);
    }

    Map<String, Object> figureMetadata = null; // TODO: Wire to versioned services

    Map<String, Object> parentArticle = (Map<String, Object>) figureMetadata.get("parentArticle");
    parentArticle = DoiSchemeStripper.strip(parentArticle);
    validateArticleVisibility(site, parentArticle);
    String parentArticleDoi = (String) parentArticle.get("doi");
    model.addAttribute("article", ImmutableMap.of("doi", parentArticleDoi));

    RenderContext renderContext = new RenderContext(site, RequestedDoiVersion.of(parentArticleDoi));
    transformFigureDescription(renderContext, figureMetadata);
    model.addAttribute("figure", figureMetadata);

    return site + "/ftl/article/figure";
  }

  /**
   * Figure lightbox
   */
  @RequestMapping(name = "lightbox", value = "/article/lightbox")
  public String renderLightbox(Model model, @SiteParam Site site)
          throws IOException {

    return site + "/ftl/article/articleLightbox";
  }

  /**
   * Apply a site's article transformation to a figure's {@code description} member and store the result in a new {@code
   * descriptionHtml} member.
   *
   * @param renderContext the context for the transform which wraps the site object and optional context values
   * @param figureMetadata the figure metadata object (per the service API's JSON response) to be read and added to
   */
  private void transformFigureDescription(RenderContext renderContext, Map<String, Object> figureMetadata) {
    String description = (String) figureMetadata.get("description");
    figureMetadata.put("descriptionHtml", articleTransformService.transformImageDescription(renderContext, description));
  }

}
