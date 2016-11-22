package org.ambraproject.wombat.controller;

import com.google.common.collect.ImmutableMap;
import org.ambraproject.wombat.config.site.JournalSite;
import org.ambraproject.wombat.config.site.MappingSiteScope;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteScope;
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
  @MappingSiteScope(SiteScope.JOURNAL_SPECIFIC)
  @RequestMapping(name = "figuresPage", value = "/article/figures")
  public String renderFiguresPage(Model model, JournalSite site,
                                  RequestedDoiVersion articleId)
      throws IOException {
    ArticleMetadata articleMetadata = articleMetadataFactory.get(site, articleId)
        .validateVisibility("figuresPage");
    model.addAttribute("article", articleMetadata.getIngestionMetadata());
    ArticlePointer articlePointer = articleMetadata.getArticlePointer();

    List<Map<String, ?>> figures = articleMetadata.getFigureView().stream()
        .map((Map<String, ?> figureMetadata) -> {
          String description = (String) figureMetadata.get("description");
          String descriptionHtml = articleTransformService.transformImageDescription(site, articlePointer, description);
          return ImmutableMap.<String, Object>builder()
              .putAll(figureMetadata)
              .put("descriptionHtml", descriptionHtml)
              .build();
        })
        .collect(Collectors.toList());
    model.addAttribute("figures", figures);

    return site + "/ftl/article/figures";
  }

  /**
   * Serve a page displaying a single figure.
   */
  @MappingSiteScope(SiteScope.JOURNAL_SPECIFIC)
  @RequestMapping(name = "figurePage", value = "/article/figure")
  public String renderFigurePage(Model model, JournalSite site,
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

    String description = (String) figureMetadata.get("description");
    String descriptionHtml = articleTransformService.transformImageDescription(site, articlePointer, description);
    model.addAttribute("descriptionHtml", descriptionHtml);

    return site + "/ftl/article/figure";
  }

  /**
   * Figure lightbox
   */
  @MappingSiteScope(SiteScope.JOURNAL_SPECIFIC)
  @RequestMapping(name = "lightbox", value = "/article/lightbox")
  public String renderLightbox(Model model, Site site) throws IOException {
    return site + "/ftl/article/articleLightbox";
  }

}
