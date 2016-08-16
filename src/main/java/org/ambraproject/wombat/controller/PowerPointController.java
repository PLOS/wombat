package org.ambraproject.wombat.controller;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteSource;
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
import org.ambraproject.wombat.service.ArticleService;
import org.ambraproject.wombat.service.PowerPointDownload;
import org.ambraproject.wombat.service.remote.ContentKey;
import org.ambraproject.wombat.service.remote.CorpusContentApi;
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
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Endpoint for downloading PowerPoint slides of figures.
 */
@Controller
public class PowerPointController extends WombatController {
  private static final Logger log = LoggerFactory.getLogger(PowerPointController.class);

  @Autowired
  private RequestMappingContextDictionary requestMappingContextDictionary;
  @Autowired
  private ArticleService articleService;
  @Autowired
  private ArticleResolutionService articleResolutionService;
  @Autowired
  private ArticleMetadata.Factory articleMetadataFactory;
  @Autowired
  private CorpusContentApi corpusContentApi;

  @RequestMapping(name = "powerPoint", value = "/article/figure/powerpoint")
  public void download(HttpServletRequest request, HttpServletResponse response,
                       @SiteParam Site site,
                       RequestedDoiVersion figureId)
      throws IOException {
    AssetPointer assetPointer = articleResolutionService.toParentIngestion(figureId);
    ArticlePointer parentArticleId = assetPointer.getParentArticle();

    ArticleMetadata parentArticle = articleMetadataFactory.get(site, figureId.forDoi(parentArticleId.getDoi()), parentArticleId)
        .validateVisibility();
    List<Map<String, ?>> figureViewList = parentArticle
        .getFigureView();
    Map<String, ?> figureMetadata = findFigureViewFor(figureViewList, assetPointer).orElseThrow(() ->
        new NotFoundException("Asset exists but is not a figure: " + assetPointer.getAssetDoi()));

    String figureTitle = (String) figureMetadata.get("title");
    String figureDescription = (String) figureMetadata.get("description");

    URL articleUrl = buildArticleUrl(request, site, parentArticleId);
    ByteSource imageFileSource = getImageFile(assetPointer);
    ByteSource logoSource = new LogoSource(site.getTheme());

    Map<String, ?> parentArticleMetadata = parentArticle.getIngestionMetadata();
    List<Map<String, ?>> parentArticleAuthors = (List<Map<String, ?>>) parentArticle.getAuthors().get("authors");

    SlideShow powerPointFile = new PowerPointDownload(parentArticleMetadata, parentArticleAuthors, articleUrl,
        figureTitle, figureDescription, imageFileSource, logoSource)
        .createPowerPointFile();

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

  private static final String IMAGE_SIZE = "medium";

  private ByteSource getImageFile(AssetPointer assetId) throws IOException {
    Map<String, ?> files = articleService.getItemFiles(assetId);
    Map<String, ?> file = (Map<String, ?>) files.get(IMAGE_SIZE);
    ContentKey key = ContentKey.createForUuid((String) file.get("crepoKey"),
        UUID.fromString((String) file.get("crepoUuid")));
    return new ByteSource() {
      @Override
      public InputStream openStream() throws IOException {
        return corpusContentApi.request(key, ImmutableList.of()).getEntity().getContent();
      }
    };
  }

  private static class LogoSource extends ByteSource {
    private final Theme theme;

    private LogoSource(Theme theme) {
      this.theme = Objects.requireNonNull(theme);
    }

    private static final String LOGO_PATH = "resource/img/logo.png";

    @Override
    public InputStream openStream() throws IOException {
      InputStream stream = theme.getStaticResource(LOGO_PATH);
      if (stream == null) {
        throw new PowerPointDownload.JournalHasNoLogoException(
            String.format("Logo file not found at %s for theme: %s", LOGO_PATH, theme.getKey()));
      }
      return stream;
    }
  }

}
