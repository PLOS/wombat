package org.ambraproject.wombat.controller;

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
public class ThumbnailController {

  @Autowired
  private SoaService soaService;

  /**
   * Serve the asset file for a figure thumbnail as the response body. Forward a stream from the SOA.
   * <p/>
   * Differs from {@link org.ambraproject.wombat.controller.FigurePageController#serveAsset} in that this method keys
   * off the asset ID and uses it to look up the figure metadata, where the other one gets an asset <em>file</em> ID.
   * They should probably be unified, however.
   * <p/>
   * TODO: Unify with {@link org.ambraproject.wombat.controller.FigurePageController#serveAsset}
   */
  @RequestMapping("/{site}/article/thumbnail")
  public void serveThumbnail(HttpServletResponse response,
                             @PathVariable("site") String site,
                             @RequestParam("id") String figureId)
      throws IOException {
    Map<String, ?> assetMetadata = soaService.requestObject("assets/" + figureId + "?figure", Map.class);

    String thumbnailSize = "medium"; // TODO Generalize to other sizes
    String thumbnailAssetFile = (String) DeserializedJsonUtil.readField(assetMetadata, "thumbnails", thumbnailSize, "file");
    Map<String, ?> thumbnailMeta = (Map<String, ?>) DeserializedJsonUtil.readField(assetMetadata, "thumbnails", thumbnailSize, "metadata");

    String contentType = (String) thumbnailMeta.get("contentType");
    response.setContentType(contentType);

    Closer closer = Closer.create();
    try {
      InputStream assetStream = soaService.requestStream("assetfiles/" + thumbnailAssetFile);
      if (assetStream == null) {
        throw new EntityNotFoundException(thumbnailAssetFile);
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

}
