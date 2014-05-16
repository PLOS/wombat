package org.ambraproject.wombat.controller;

import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.service.remote.SoaService;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
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
  private SoaService soaService;

  @RequestMapping(value = {"indirect/{bucket}/{key}/{version}", "{site}/indirect/{bucket}/{key}/{version}"})
  public void serve(HttpServletResponse responseToClient,
                    @SiteParam Site site,
                    @PathVariable("bucket") String bucket,
                    @PathVariable("key") String key,
                    @PathVariable("version") String version)
      throws IOException {
    try (CloseableHttpResponse responseFromRepo = soaService.requestFromContentRepo(bucket, key, version)) {
      // TODO Support reproxy headers; unify with FigureImageController.serveAssetFile()

      for (Header headerFromService : responseFromRepo.getAllHeaders()) {
        String name = headerFromService.getName();
        responseToClient.setHeader(name, headerFromService.getValue());
      }

      try (InputStream assetStream = responseFromRepo.getEntity().getContent()) {
        try (OutputStream responseStream = responseToClient.getOutputStream()) {
          IOUtils.copy(assetStream, responseStream);
        }
      }
    }
  }

}
