package org.ambraproject.wombat.controller;

import com.google.common.base.Optional;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.service.remote.AssetServiceResponse;
import org.ambraproject.wombat.service.remote.ContentRepoService;
import org.apache.http.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Forwards requests for files to the content repository.
 */
@Controller
public class IndirectFileController extends WombatController {

  @Autowired
  private ContentRepoService contentRepoService;

  @RequestMapping(value = {"indirect/{bucket}/{key}", "{site}/indirect/{bucket}/{key}"})
  public void serve(HttpServletResponse response,
                    HttpServletRequest request,
                    @SiteParam Site site,
                    @PathVariable("bucket") String bucket,
                    @PathVariable("key") String key)
      throws IOException {
    serve(response, request, bucket, key, Optional.<Integer>absent());
  }

  @RequestMapping(value = {"indirect/{bucket}/{key}/{version}", "{site}/indirect/{bucket}/{key}/{version}"})
  public void serve(HttpServletResponse response,
                    HttpServletRequest request,
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
    serve(response, request, bucket, key, Optional.of(versionInt));
  }

  private void serve(HttpServletResponse responseToClient, HttpServletRequest requestFromClient,
                     String bucket, String key, Optional<Integer> version)
      throws IOException {
    Header[] assetHeaders = copyAssetRequestHeaders(requestFromClient);
    try (AssetServiceResponse repoResponse = contentRepoService.request(bucket, key, version, assetHeaders)) {
      // TODO Serve 400-series errors if entity does not exist

      copyAssetServiceResponse(repoResponse, responseToClient);
    }
  }

}
