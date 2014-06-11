package org.ambraproject.wombat.controller;

import com.google.common.base.Optional;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.service.remote.ContentRepoService;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Forwards requests for files to the content repository.
 */
@Controller
public class IndirectFileController {

  @Autowired
  private ContentRepoService contentRepoService;

  @RequestMapping(value = {"indirect/{bucket}/{key}", "{site}/indirect/{bucket}/{key}"})
  public void serve(HttpServletResponse response,
                    @SiteParam Site site,
                    @PathVariable("bucket") String bucket,
                    @PathVariable("key") String key)
      throws IOException {
    serve(response, bucket, key, Optional.<Integer>absent());
  }

  @RequestMapping(value = {"indirect/{bucket}/{key}/{version}", "{site}/indirect/{bucket}/{key}/{version}"})
  public void serve(HttpServletResponse response,
                    @SiteParam Site site,
                    @PathVariable("bucket") String bucket,
                    @PathVariable("key") String key,
                    @PathVariable("version") String version)
      throws IOException {
    Integer versionInt;
    try {
      versionInt = Integer.valueOf(version);
    } catch (NumberFormatException e) {
      throw new NotFoundException("Not a valid version integer: " + version, e);
    }
    serve(response, bucket, key, Optional.of(versionInt));
  }

  private void serve(HttpServletResponse responseToClient,
                     String bucket, String key, Optional<Integer> version)
      throws IOException {
    try (ContentRepoService.ContentRepoResponse repoResponse = contentRepoService.request(bucket, key, version)) {
      // TODO Support reproxy headers
      // TODO Serve 400-series errors if entity does not exist
      // TODO Unify with FigureImageController.serveAssetFile()

      for (Header headerFromService : repoResponse.getAllHeaders()) {
        String name = headerFromService.getName();
        responseToClient.setHeader(name, headerFromService.getValue());
      }

      try (InputStream assetStream = repoResponse.getStream()) {
        try (OutputStream responseStream = responseToClient.getOutputStream()) {
          IOUtils.copy(assetStream, responseStream);
        }
      }
    }
  }

}
