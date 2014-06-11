package org.ambraproject.wombat.controller;

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

  @RequestMapping(value = {"indirect/{bucket}/{key}/{version}", "{site}/indirect/{bucket}/{key}/{version}"})
  public void serve(HttpServletResponse responseToClient,
                    @SiteParam Site site,
                    @PathVariable("bucket") String bucket,
                    @PathVariable("key") String key,
                    @PathVariable("version") String version)
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
