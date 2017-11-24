/*
 * Copyright (c) 2017 Public Library of Science
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package org.ambraproject.wombat.controller;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MoreCollectors;
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
        .validateVisibility("figuresPage");
    model.addAttribute("article", articleMetadata.getIngestionMetadata());
    ArticlePointer articlePointer = articleMetadata.getArticlePointer();

    List<Map<String, ?>> figures = articleMetadata.getFigureView().stream()
        .map((Map<String, ?> figureMetadata) -> {
          String descriptionHtml = getDescriptionHtml(site, articlePointer, figureMetadata);
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
        .collect(MoreCollectors.onlyElement());
    model.addAttribute("figure", figureMetadata);

    String descriptionHtml = getDescriptionHtml(site, articlePointer, figureMetadata);
    model.addAttribute("descriptionHtml", descriptionHtml);

    return site + "/ftl/article/figure";
  }

  private String getDescriptionHtml(Site site, ArticlePointer articlePointer,
                                    Map<String, ?> figureMetadata) {
    String description = (String) figureMetadata.get("description");
    return Strings.isNullOrEmpty(description) ? "" :
        articleTransformService.transformImageDescription(site, articlePointer, description);
  }

  /**
   * Figure lightbox
   */
  @RequestMapping(name = "lightbox", value = "/article/lightbox")
  public String renderLightbox(Model model, @SiteParam Site site) throws IOException {
    return site + "/ftl/article/articleLightbox";
  }

}
