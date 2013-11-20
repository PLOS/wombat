package org.ambraproject.wombat.controller;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Closer;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.service.SoaService;
import org.ambraproject.wombat.util.DeserializedJsonUtil;
import org.apache.commons.io.IOUtils;
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
public class FigureImageController {

  @Autowired
  private SoaService soaService;

  /**
   * Forward a stream for an asset file from the SOA to the response.
   *
   * @param response
   * @param assetId
   * @param contentType
   * @throws IOException
   */
  private void serveAssetFile(HttpServletResponse response, String assetId, String contentType) throws IOException {
    response.setContentType(contentType);

    Closer closer = Closer.create();
    try {
      InputStream assetStream = soaService.requestStream("assetfiles/" + assetId);
      if (assetStream == null) {
        throw new EntityNotFoundException(assetId);
      }
      closer.register(assetStream);
      OutputStream responseStream = closer.register(response.getOutputStream());
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
    Map<String, Object> assetMetadata = soaService.requestObject("assetfiles/" + assetId + "?metadata", Map.class);
    String contentType = (String) assetMetadata.get("contentType");
    serveAssetFile(response, assetId, contentType);
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
    Map<String, ?> assetMetadata = soaService.requestObject("assets/" + figureId + "?figure", Map.class);

    List<String> pathToFigureObject = ORIGINAL_FIGURE.equals(figureSize)
        ? ORIGINAL_FIGURE_PATH : ImmutableList.of("thumbnails", figureSize);
    Map<String, ?> figureObject = (Map<String, ?>) DeserializedJsonUtil.readField(assetMetadata, pathToFigureObject);
    if (figureObject == null) {
      throw new IllegalArgumentException("Not a valid size: " + figureSize); // TODO: Respond with 404?
    }
    String assetFileId = (String) figureObject.get("file");
    Map<String, ?> assetFileMeta = (Map<String, ?>) figureObject.get("metadata");

    String contentType = (String) assetFileMeta.get("contentType");
    serveAssetFile(response, assetFileId, contentType);
  }

}
