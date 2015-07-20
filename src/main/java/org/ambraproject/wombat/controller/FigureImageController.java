package org.ambraproject.wombat.controller;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.service.remote.SoaService;
import org.ambraproject.wombat.util.DeserializedJsonUtil;
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
import java.util.List;
import java.util.Map;

@Controller
public class FigureImageController extends WombatController {
  private static final Logger log = LoggerFactory.getLogger(FigureImageController.class);

  @Autowired
  private SoaService soaService;

  /**
   * Forward a response for an asset file from the SOA to the response.
   *
   * @param requestFromClient
   * @param responseToClient
   * @param assetId           the identifier for an asset or asset file
   * @throws IOException
   */
  private void serveAssetFile(HttpServletRequest requestFromClient,
                              HttpServletResponse responseToClient,
                              String assetId)
      throws IOException {
    try (CloseableHttpResponse responseFromService = soaService.requestAsset(assetId,
            HttpMessageUtil.getRequestHeaders(requestFromClient, ASSET_REQUEST_HEADER_WHITELIST))) {
      HttpMessageUtil.copyResponseWithHeaders(responseFromService, responseToClient, ASSET_RESPONSE_HEADER_FILTER);
    } catch (EntityNotFoundException e) {
      throw new NotFoundException(e);
    }
  }

  /**
   * Serve the identified asset file.
   *
   * @param id     an ID for an asset (if {@code unique} is present) or an asset file (if {@code unique} is absent)
   * @param unique if present, assume the asset has a single file and serve that file; else, serve an identified file
   */
  @RequestMapping(name = "asset", value = {"/article/asset", "/*/article/asset"})
  public void serveAsset(HttpServletRequest request,
                         HttpServletResponse response,
                         @SiteParam Site site,
                         @RequestParam(value = "id", required = true) String id,
                         @RequestParam(value = "unique", required = false) String unique)
      throws IOException {
    requireNonemptyParameter(id);

    String assetFileId;
    Map<String, ?> assetFileMetadata;
    try {
      if (!booleanParameter(unique)) {
        // The request directly identifies an asset file.
        assetFileId = id;
        assetFileMetadata = soaService.requestObject("assetfiles/" + id + "?metadata", Map.class);
      } else {
        // The request identifies an asset and asserts that the asset has exactly one file. Get the ID of that file.

        Map<String, Map<String, ?>> assetMetadata = soaService.requestObject("assets/" + id + "?metadata", Map.class);
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

    serveAssetFile(request, response, assetFileId);
  }

  private static final String ORIGINAL_FIGURE = "original";
  private static final ImmutableList<String> ORIGINAL_FIGURE_PATH = ImmutableList.of(ORIGINAL_FIGURE);

  /**
   * Serve the asset file for an identified figure thumbnail.
   */
  @RequestMapping(name = "figureImage", value = {"/article/figure/image", "/*/article/figure/image"})
  public void serveFigureImage(HttpServletRequest request,
                               HttpServletResponse response,
                               @SiteParam Site site,
                               @RequestParam("id") String figureId,
                               @RequestParam("size") String figureSize)
      throws IOException {
    requireNonemptyParameter(figureId);
    Map<String, ?> assetMetadata;
    try {
      assetMetadata = soaService.requestObject("assets/" + figureId + "?figure", Map.class);
    } catch (EntityNotFoundException e) {
      throw new NotFoundException(e);
    }
    validateArticleVisibility(site, (Map<?, ?>) assetMetadata.get("parentArticle"));

    List<String> pathToFigureObject = ORIGINAL_FIGURE.equals(figureSize)
        ? ORIGINAL_FIGURE_PATH : ImmutableList.of("thumbnails", figureSize);
    Map<String, ?> figureObject = (Map<String, ?>) DeserializedJsonUtil.readField(assetMetadata, pathToFigureObject);
    if (figureObject == null) {
      throw new NotFoundException("Not a valid size: " + figureSize);
    }
    String assetFileId = (String) figureObject.get("file");

    serveAssetFile(request, response, assetFileId);
  }

}
