package org.ambraproject.wombat.controller;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;
import com.google.common.io.Closer;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.service.SoaService;
import org.ambraproject.wombat.util.DeserializedJsonUtil;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHeader;
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
   * @param responseToClient
   * @param assetId
   * @throws IOException
   */
  private void serveAssetFile(HttpServletRequest requestFromClient,
                              HttpServletResponse responseToClient,
                              String assetId)
      throws IOException {
    HttpResponse responseFromService;
    try {
      responseFromService = soaService.requestAsset(assetId, copyHeaders(requestFromClient));
    } catch (EntityNotFoundException e) {
      throw new NotFoundException(e);
    }

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

    Closer closer = Closer.create();
    try {
      InputStream assetStream = responseFromService.getEntity().getContent();
      if (assetStream == null) {
        throw new EntityNotFoundException(assetId);
      }
      closer.register(assetStream);
      /*
       * In a reproxying case, the asset stream might be empty. It might be a performance win to look ahead and avoid
       * opening an output stream if there's nothing to send, but for now just let IOUtils.copy handle it regardless.
       */

      OutputStream responseStream = closer.register(responseToClient.getOutputStream());
      IOUtils.copy(assetStream, responseStream); // buffered
    } catch (Throwable t) {
      throw closer.rethrow(t);
    } finally {
      closer.close();
    }
  }

  /**
   * Serve the identified asset file.
   */
  @RequestMapping("/{site}/article/asset")
  public void serveAsset(HttpServletRequest request,
                         HttpServletResponse response,
                         @PathVariable("site") String site,
                         @RequestParam("id") String assetId)
      throws IOException {
    requireNonemptyParameter(assetId);
    serveAssetFile(request, response, assetId);
  }

  private static final String ORIGINAL_FIGURE = "original";
  private static final ImmutableList<String> ORIGINAL_FIGURE_PATH = ImmutableList.of(ORIGINAL_FIGURE);

  /**
   * Serve the asset file for an identified figure thumbnail.
   */
  @RequestMapping("/{site}/article/figure/image")
  public void serveFigureImage(HttpServletRequest request,
                               HttpServletResponse response,
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
