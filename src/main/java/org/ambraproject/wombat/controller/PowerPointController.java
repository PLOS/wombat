package org.ambraproject.wombat.controller;

import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.config.site.url.Link;
import org.ambraproject.wombat.config.theme.Theme;
import org.ambraproject.wombat.service.ApiAddress;
import org.ambraproject.wombat.service.PowerPointService;
import org.ambraproject.wombat.service.remote.ServiceRequestException;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.apache.poi.hslf.usermodel.SlideShow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Endpoint for downloading PowerPoint slides of figures.
 */
@Controller
public class PowerPointController extends WombatController {
  private static final Logger log = LoggerFactory.getLogger(PowerPointController.class);

  private static final String LOGO_PATH = "resource/img/logo.png";

  @Autowired
  private ArticleApi articleApi;
  @Autowired
  private PowerPointService powerPointService;

  @RequestMapping(name = "powerPoint", value = "/article/figure/powerpoint")
  public void download(HttpServletRequest request, HttpServletResponse response,
                       @SiteParam Site site,
                       @RequestParam(value = "id", required = true) String figureId)
      throws IOException {

    Map<String, Object> figureMetadata;
    try {
      figureMetadata = articleApi.requestObject(
          ApiAddress.builder("assets").addToken(figureId).addParameter("figure").build(), Map.class);
    } catch (ServiceRequestException e) {
      if (e.getStatusCode() == HttpStatus.BAD_REQUEST.value()) {
        /*
         * Probably a request for a non-figure asset. Could easily be caused by an application bug, but technically
         * could just be a bogus URL from the user, so give them a 404.
         *
         * We might need a more explicit way for the service API to say, "that asset exists but isn't a figure", rather
         * than just assuming that's the only thing that causes a 400 status.
         */
        throw new NotFoundException(figureId);
      } else throw e;
    }

    Map<String, Object> parentArticleMetadata = (Map<String, Object>) figureMetadata.get("parentArticle");
    validateArticleVisibility(site, parentArticleMetadata);

    final Theme theme = site.getTheme();
    PowerPointService.JournalLogoCallback logoCallback = new PowerPointService.JournalLogoCallback() {
      @Override
      public InputStream openLogoStream() throws IOException {
        InputStream stream = theme.getStaticResource(LOGO_PATH);
        if (stream == null) {
          log.warn("Logo file not found at {} for theme: {}", LOGO_PATH, theme.getKey());
        }
        return stream;
      }
    };

    String parentArticleDoi = (String) parentArticleMetadata.get("doi");
    URL articleUrl = buildArticleUrl(request, site, parentArticleDoi);

    SlideShow powerPointFile = powerPointService.createPowerPointFile(figureMetadata, articleUrl, logoCallback);

    response.setContentType(MediaType.MICROSOFT_POWERPOINT.toString());
    response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + getDownloadFilename(figureId));
    try (OutputStream outputStream = response.getOutputStream()) {
      powerPointFile.write(outputStream);
    }
  }

  /**
   * Build the full URL to the figure's parent article.
   *
   * @param request          the request to download a PowerPoint file associated with a figure
   * @param site             the site to which the request came
   * @param parentArticleDoi the DOI of the figure's parent article
   * @return the URL to view the parent article on the same site
   */
  private static URL buildArticleUrl(HttpServletRequest request, Site site, String parentArticleDoi) {
    String articleUrl = Link.toAbsoluteAddress(site).toPath("article?id=" + parentArticleDoi).get(request);
    try {
      return new URL(articleUrl);
    } catch (MalformedURLException e) {
      throw new RuntimeException("Built a URL with invalid syntax", e);
    }
  }

  private static String getDownloadFilename(String figureId) {
    int slashIndex = figureId.lastIndexOf('/');
    String name = figureId.substring(slashIndex + 1);
    return name + ".ppt";
  }

}
