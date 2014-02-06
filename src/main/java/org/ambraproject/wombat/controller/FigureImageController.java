package org.ambraproject.wombat.controller;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Closer;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.service.SoaService;
import org.ambraproject.wombat.util.DeserializedJsonUtil;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

@Controller
public class FigureImageController extends WombatController {

  @Autowired
  private SoaService soaService;

  /**
   * Forward a response for an asset file from the SOA to the response.
   *
   * @param responseToClient
   * @param assetId
   * @throws IOException
   */
  private void serveAssetFile(HttpServletResponse responseToClient, String assetId) throws IOException {
    HttpResponse responseFromService = soaService.requestAsset(assetId);

    /*
     * Repeat all headers from the service to the client. This propagates (at minimum) the "content-type" and
     * "content-disposition" headers, and headers that control reproxying.
     */
    for (Header headerFromService : responseFromService.getAllHeaders()) {
      responseToClient.setHeader(headerFromService.getName(), headerFromService.getValue());
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
  public void serveAsset(HttpServletResponse response,
                         @PathVariable("site") String site,
                         @RequestParam("id") String assetId)
      throws IOException {
    requireNonemptyParameter(assetId);
    serveAssetFile(response, assetId);
  }

  private static final String ORIGINAL_FIGURE = "original";
  private static final ImmutableList<String> ORIGINAL_FIGURE_PATH = ImmutableList.of(ORIGINAL_FIGURE);

  /**
   * Serve the asset file for an identified figure thumbnail.
   */
  @RequestMapping("/{site}/article/figure/image")
  public void serveFigureImage(HttpServletResponse response,
                               @RequestParam("id") String figureId,
                               @RequestParam("size") String figureSize)
      throws IOException {
    requireNonemptyParameter(figureId);
    Map<String, ?> assetMetadata = soaService.requestObject("assets/" + figureId + "?figure", Map.class);

    List<String> pathToFigureObject = ORIGINAL_FIGURE.equals(figureSize)
        ? ORIGINAL_FIGURE_PATH : ImmutableList.of("thumbnails", figureSize);
    Map<String, ?> figureObject = (Map<String, ?>) DeserializedJsonUtil.readField(assetMetadata, pathToFigureObject);
    if (figureObject == null) {
      throw new NotFoundException("Not a valid size: " + figureSize);
    }
    String assetFileId = (String) figureObject.get("file");

    serveAssetFile(response, assetFileId);
  }

}
