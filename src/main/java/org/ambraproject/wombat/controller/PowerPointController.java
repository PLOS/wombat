package org.ambraproject.wombat.controller;

import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import org.ambraproject.wombat.config.site.RequestMappingContextDictionary;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.config.site.url.Link;
import org.ambraproject.wombat.config.theme.Theme;
import org.ambraproject.wombat.identity.ArticlePointer;
import org.ambraproject.wombat.identity.AssetPointer;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.ambraproject.wombat.service.ArticleResolutionService;
import org.ambraproject.wombat.service.PowerPointService;
import org.apache.poi.hslf.usermodel.SlideShow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Endpoint for downloading PowerPoint slides of figures.
 */
@Controller
public class PowerPointController extends WombatController {
  private static final Logger log = LoggerFactory.getLogger(PowerPointController.class);

  private static final String LOGO_PATH = "resource/img/logo.png";

  @Autowired
  private RequestMappingContextDictionary requestMappingContextDictionary;
  @Autowired
  private PowerPointService powerPointService;
  @Autowired
  private ArticleResolutionService articleResolutionService;
  @Autowired
  private ArticleMetadata.Factory articleMetadataFactory;

  @RequestMapping(name = "powerPoint", value = "/article/figure/powerpoint")
  public void download(HttpServletRequest request, HttpServletResponse response,
                       @SiteParam Site site,
                       RequestedDoiVersion figureId)
      throws IOException {
    AssetPointer assetPointer = articleResolutionService.toParentIngestion(figureId);
    ArticlePointer parentArticleId = assetPointer.getParentArticle();

    List<Map<String, ?>> figureViewList = articleMetadataFactory.get(site, figureId.forDoi(parentArticleId.getDoi()), parentArticleId)
        .validateVisibility()
        .getFigureView();
    Map<String, ?> figureMetadata = findFigureViewFor(figureViewList, assetPointer).orElseThrow(() ->
        new NotFoundException("Asset exists but is not a figure: " + assetPointer.getAssetDoi()));

    final Theme theme = site.getTheme();
    PowerPointService.JournalLogoCallback logoCallback = () -> {
      InputStream stream = theme.getStaticResource(LOGO_PATH);
      if (stream == null) {
        log.warn("Logo file not found at {} for theme: {}", LOGO_PATH, theme.getKey());
      }
      return stream;
    };

    URL articleUrl = buildArticleUrl(request, site, parentArticleId);

    SlideShow powerPointFile = powerPointService.createPowerPointFile(figureMetadata, articleUrl, logoCallback);

    response.setContentType(MediaType.MICROSOFT_POWERPOINT.toString());
    response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + getDownloadFilename(assetPointer));
    try (OutputStream outputStream = response.getOutputStream()) {
      powerPointFile.write(outputStream);
    }
  }

  private Optional<Map<String, ?>> findFigureViewFor(List<Map<String, ?>> figureView, AssetPointer assetPointer) {
    return figureView.stream()
        .filter((Map<String, ?> figure) -> {
          String figureDoi = (String) figure.get("doi");
          return figureDoi.equals(assetPointer.getAssetDoi());
        })
        .findAny();
  }

  /**
   * Build the full URL to the figure's parent article.
   *
   * @param request         the request to download a PowerPoint file associated with a figure
   * @param site            the site to which the request came
   * @param parentArticleId the identity of the figure's parent article
   * @return the URL to view the parent article on the same site
   */
  private URL buildArticleUrl(HttpServletRequest request, Site site, ArticlePointer parentArticleId) {
    String articleUrl = Link.toAbsoluteAddress(site)
        .toPattern(requestMappingContextDictionary, "article")
        .addQueryParameters(parentArticleId.asParameterMap())
        .build().get(request);
    try {
      return new URL(articleUrl);
    } catch (MalformedURLException e) {
      throw new RuntimeException("Built a URL with invalid syntax", e);
    }
  }

  private static String getDownloadFilename(AssetPointer asset) {
    String figureId = asset.getAssetDoi();
    int slashIndex = figureId.lastIndexOf('/');
    String name = figureId.substring(slashIndex + 1);
    return name + ".ppt";
  }

}
