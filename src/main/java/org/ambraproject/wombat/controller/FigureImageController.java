package org.ambraproject.wombat.controller;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.service.SoaService;
import org.ambraproject.wombat.util.DeserializedJsonUtil;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

@Controller
public class FigureImageController extends WombatController {
  private static final Logger log = LoggerFactory.getLogger(FigureImageController.class);

  @Autowired
  private SoaService soaService;

  // Inconsistent with equals. See Javadoc for java.util.SortedSet.
  private static ImmutableSet<String> caseInsensitiveImmutableSet(String... strings) {
    return ImmutableSortedSet.copyOf(String.CASE_INSENSITIVE_ORDER, Arrays.asList(strings));
  }

  /**
   * Names of headers that, on an asset request from the client, should be passed through on our request to the service
   * tier.
   */
  private static final ImmutableSet<String> ASSET_REQUEST_HEADER_WHITELIST = caseInsensitiveImmutableSet("X-Proxy-Capabilities");

  /**
   * Names of headers that, in an asset response from the service tier, should be passed through to the client.
   */
  private static final ImmutableSet<String> ASSET_RESPONSE_HEADER_WHITELIST = caseInsensitiveImmutableSet(
      "Content-Type", "Content-Disposition", "X-Reproxy-URL", "X-Reproxy-Cache-For");

  /**
   * Copy headers from an asset request if they are whitelisted. Effectively translating from a Spring model into an
   * Apache model.
   *
   * @param request a request
   * @return its headers
   */
  private static Header[] copyHeaders(HttpServletRequest request) {
    Enumeration headerNames = request.getHeaderNames();
    List<Header> headers = Lists.newArrayList();
    while (headerNames.hasMoreElements()) {
      String headerName = (String) headerNames.nextElement();
      if (ASSET_REQUEST_HEADER_WHITELIST.contains(headerName)) {
        String headerValue = request.getHeader(headerName);
        headers.add(new BasicHeader(headerName, headerValue));
      }
    }
    return headers.toArray(new Header[headers.size()]);
  }

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
    try (CloseableHttpResponse responseFromService = soaService.requestAsset(assetId, copyHeaders(requestFromClient))) {

      /*
       * Repeat all headers from the service to the client. This propagates (at minimum) the "content-type" and
       * "content-disposition" headers, and headers that control reproxying.
       */
      for (Header headerFromService : responseFromService.getAllHeaders()) {
        String name = headerFromService.getName();
        if (ASSET_RESPONSE_HEADER_WHITELIST.contains(name)) {
          responseToClient.setHeader(name, headerFromService.getValue());
        }
      }

      try (InputStream assetStream = responseFromService.getEntity().getContent()) {
        if (assetStream == null) {
          throw new EntityNotFoundException(assetId);
        }

        /*
         * In a reproxying case, the asset stream might be empty. It might be a performance win to look ahead and avoid
         * opening an output stream if there's nothing to send, but for now just let IOUtils.copy handle it regardless.
         */
        try (OutputStream responseStream = responseToClient.getOutputStream()) {
          IOUtils.copy(assetStream, responseStream); // buffered
        }
      }
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
  @RequestMapping("/{site}/article/asset")
  public void serveAsset(HttpServletRequest request,
                         HttpServletResponse response,
                         @PathVariable("site") String site,
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
  @RequestMapping("/{site}/article/figure/image")
  public void serveFigureImage(HttpServletRequest request,
                               HttpServletResponse response,
                               @PathVariable("site") String site,
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
