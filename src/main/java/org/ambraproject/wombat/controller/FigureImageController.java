package org.ambraproject.wombat.controller;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.ambraproject.wombat.config.site.RequestMappingContextDictionary;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.config.site.url.Link;
import org.ambraproject.wombat.model.ScholarlyWorkId;
import org.ambraproject.wombat.service.ApiAddress;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.ambraproject.wombat.util.HttpMessageUtil;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Controller
public class FigureImageController extends WombatController {
  private static final Logger log = LoggerFactory.getLogger(FigureImageController.class);

  @Autowired
  private ArticleApi articleApi;
  @Autowired
  private RequestMappingContextDictionary requestMappingContextDictionary;
  @Autowired
  private ScholarlyWorkController scholarlyWorkController;

  /**
   * Forward a response for an asset file from the SOA to the response.
   *
   * @param requestFromClient
   * @param responseToClient
   * @param assetId           the identifier for an asset or asset file
   * @param isDownloadRequest forward Content-Disposition headers with "attachment" value only if {@code true}
   * @throws IOException
   */
  private void serveAssetFile(HttpServletRequest requestFromClient,
                              HttpServletResponse responseToClient,
                              String assetId,
                              boolean isDownloadRequest)
      throws IOException {
    try (CloseableHttpResponse responseFromService = articleApi.requestAsset(assetId,
            HttpMessageUtil.getRequestHeaders(requestFromClient, ASSET_REQUEST_HEADER_WHITELIST))) {
      forwardAssetResponse(responseFromService, responseToClient, isDownloadRequest);
    } catch (EntityNotFoundException e) {
      throw new NotFoundException(e);
    }
  }

  /**
   * Serve the identified asset file.
   *
   * @param id       an ID for an asset (if {@code unique} is present) or an asset file (if {@code unique} is absent)
   * @param unique   if present, assume the asset has a single file and serve that file; else, serve an identified file
   * @param download forward Content-Disposition headers with "attachment" value only if {@code true}
   */
  @RequestMapping(name = "asset", value = "/article/asset")
  public void serveAsset(HttpServletRequest request,
                         HttpServletResponse response,
                         @SiteParam Site site,
                         @RequestParam(value = "id", required = true) String id,
                         @RequestParam(value = "unique", required = false) String unique,
                         @RequestParam(value = "download", required = false) String download)
      throws IOException {
    requireNonemptyParameter(id);

    String assetFileId;
    Map<String, ?> assetFileMetadata;
    try {
      if (!booleanParameter(unique)) {
        // The request directly identifies an asset file.
        assetFileId = id;
        assetFileMetadata = articleApi.requestObject(
            ApiAddress.builder("assetfiles").addToken(id).addParameter("metadata").build(),
            Map.class);
      } else {
        // The request identifies an asset and asserts that the asset has exactly one file. Get the ID of that file.

        Map<String, Map<String, ?>> assetMetadata = articleApi.requestObject(
            ApiAddress.builder("assets").addToken(id).addParameter("metadata").build(),
            Map.class);
        if (assetMetadata.size() != 1) {
          /*
           * The user queried for the unique file of a non-unique asset. Because they might have manually punched in an
           * invalid URL, show a 404 page. Also log a warning in case it was caused by a buggy link.
           */
          log.warn("Received request for unique asset file with ID=\"{}\". More than one associated file ID: {}",
              id, assetMetadata.keySet());
          throw new NotFoundException();
        }

        assetFileId = Iterables.getOnlyElement(assetMetadata.keySet());
        assetFileMetadata = Iterables.getOnlyElement(assetMetadata.values());
      }
    } catch (EntityNotFoundException e) {
      throw new NotFoundException(e);
    }

    Map<?, ?> parentArticleMetadata = (Map<String, ?>) assetFileMetadata.get("parentArticle");
    validateArticleVisibility(site, parentArticleMetadata);

    serveAssetFile(request, response, assetFileId, booleanParameter(download));
  }

  private static final String ORIGINAL_FIGURE = "original";
  private static final ImmutableList<String> ORIGINAL_FIGURE_PATH = ImmutableList.of(ORIGINAL_FIGURE);

  /**
   * Serve the asset file for an identified figure thumbnail.
   */
  @RequestMapping(name = "figureImage", value = "/article/figure/image")
  public String serveFigureImage(HttpServletRequest request,
                                 HttpServletResponse response,
                                 @SiteParam Site site,
                                 @RequestParam("id") String figureId,
                                 @RequestParam("size") String figureSize,
                                 @RequestParam(value = "download", required = false) String download)
      throws IOException {
    requireNonemptyParameter(figureId);
    Map<String, Object> workMetadata = scholarlyWorkController.getWorkMetadata(new ScholarlyWorkId(figureId));
    Map<String, Object> files = (Map<String, Object>) workMetadata.get("files");
    if (files.containsKey(figureSize)) {
      return redirectToWorkFile(request, site, figureId, figureSize, booleanParameter(download));
    } else {
      throw new NotFoundException("Not a valid size: " + figureSize);
    }
  }

  private String redirectToWorkFile(HttpServletRequest request, Site site,
                                    String id, String fileType, boolean isDownload) {
    Link.Factory.PatternBuilder link = Link.toLocalSite(site)
        .toPattern(requestMappingContextDictionary, "workFile")
        .addQueryParameter("id", id)
        .addQueryParameter("fileType", fileType);
    if (isDownload) {
      link = link.addQueryParameter("download", "");
    }
    return link.build().getRedirect(request);
  }

}
