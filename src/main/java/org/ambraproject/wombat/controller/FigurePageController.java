package org.ambraproject.wombat.controller;

import com.google.common.collect.ImmutableMap;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.identity.ArticlePointer;
import org.ambraproject.wombat.identity.AssetPointer;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.ambraproject.wombat.service.ArticleResolutionService;
import org.ambraproject.wombat.service.ArticleService;
import org.ambraproject.wombat.service.ArticleTransformService;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
  @Autowired
  private ArticleMetadata.Factory articleMetadataFactory;

  /**
   * Serve a page listing all figures for an article.
   */
  @RequestMapping(name = "figuresPage", value = "/article/figures")
  public String renderFiguresPage(Model model, @SiteParam Site site,
                                  RequestedDoiVersion articleId)
      throws IOException {
    ArticleMetadata articleMetadata = articleMetadataFactory.get(site, articleId)
        .validateVisibility();
    model.addAttribute("article", articleMetadata.getIngestionMetadata());

    List<Map<String, ?>> figures = articleMetadata.getFigureView().stream()
        .map((Map<String, ?> figureMetadata) -> {
          String figureDescription = transformFigureDescription(site, figureMetadata);
          return ImmutableMap.<String, Object>builder()
              .putAll(figureMetadata)
              .put("descriptionHtml", figureDescription)
              .build();
        })
        .collect(Collectors.toList());
    model.addAttribute("figures", figures);

    return site + "/ftl/article/figures";
  }

  /**
   * Serve a page displaying a single figure.
   */
  @RequestMapping(name = "figurePage", value = "/article/figure")
  public String renderFigurePage(Model model, @SiteParam Site site,
                                 RequestedDoiVersion figureId)
      throws IOException {
    AssetPointer assetPointer = articleResolutionService.toParentIngestion(figureId);
    model.addAttribute("figurePtr", assetPointer.asParameterMap());

    ArticlePointer articlePointer = assetPointer.getParentArticle();
    RequestedDoiVersion articleId = figureId.forDoi(articlePointer.getDoi());

    ArticleMetadata articleMetadata = articleMetadataFactory.get(site, articleId, articlePointer);
    model.addAttribute("article", articleMetadata.getIngestionMetadata());

    Map<String, ?> figureMetadata = articleMetadata.getFigureView().stream()
        .filter((Map<String, ?> fig) -> fig.get("doi").equals(assetPointer.getAssetDoi()))
        .findAny().orElseThrow(RuntimeException::new);
    model.addAttribute("figure", figureMetadata);
    model.addAttribute("descriptionHtml", transformFigureDescription(site, figureMetadata));

    return site + "/ftl/article/figure";
  }

  /**
   * Figure lightbox
   */
  @RequestMapping(name = "lightbox", value = "/article/lightbox")
  public String renderLightbox(Model model, @SiteParam Site site) throws IOException {
    return site + "/ftl/article/articleLightbox";
  }

  /**
   * Apply a site's article transformation to a figure's {@code description} member and store the result in a new {@code
   * descriptionHtml} member.
   *
   * @param site           the context for the transform
   * @param figureMetadata the figure metadata object (per the service API's JSON response) to be read and added to
   */
  private String transformFigureDescription(Site site, Map<String, ?> figureMetadata) {
    String description = (String) figureMetadata.get("description");
    return articleTransformService.transformImageDescription(site, description);
  }

}
