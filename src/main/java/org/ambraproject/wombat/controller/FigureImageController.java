package org.ambraproject.wombat.controller;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
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
  private static final ImmutableSet<String> FIGURE_SIZES = ImmutableSet.of(ORIGINAL_FIGURE,
      "small", "inline", "medium", "large");

  /**
   * Serve the asset file for an identified figure thumbnail.
   */
  @RequestMapping("/{site}/article/figure/image")
  public void serveFigureImage(HttpServletResponse response,
                               @PathVariable("site") String site,
                               @RequestParam("id") String figureId,
                               @RequestParam("size") String figureSize)
      throws IOException {
    Preconditions.checkArgument(FIGURE_SIZES.contains(figureSize)); // TODO: Respond with 404?

    Map<String, ?> assetMetadata = soaService.requestObject("assets/" + figureId + "?figure", Map.class);

    Map<String, ?> assetFileMeta;
    String assetFileId;
    if (ORIGINAL_FIGURE.equals(figureSize)) {
      assetFileId = (String) DeserializedJsonUtil.readField(assetMetadata, ORIGINAL_FIGURE, "file");
      assetFileMeta = (Map<String, ?>) DeserializedJsonUtil.readField(assetMetadata, ORIGINAL_FIGURE, "metadata");
    } else {
      assetFileId = (String) DeserializedJsonUtil.readField(assetMetadata, "thumbnails", figureSize, "file");
      assetFileMeta = (Map<String, ?>) DeserializedJsonUtil.readField(assetMetadata, "thumbnails", figureSize, "metadata");
    }

    String contentType = (String) assetFileMeta.get("contentType");
    serveAssetFile(response, assetFileId, contentType);
  }

}
