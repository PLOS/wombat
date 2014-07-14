package org.ambraproject.wombat.controller;

import com.google.common.base.Optional;
import com.google.common.primitives.Ints;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.service.EntityNotFoundException;
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

  @RequestMapping(value = {"indirect/{key}", "{site}/indirect/{key}"})
  public void serve(HttpServletResponse response,
                    HttpServletRequest request,
                    @SiteParam Site site,
                    @PathVariable("key") String key)
      throws IOException {
    serve(response, request, key, Optional.<String>absent());
  }

  @RequestMapping(value = {"indirect/{key}/{version}", "{site}/indirect/{key}/{version}"})
  public void serve(HttpServletResponse response,
                    HttpServletRequest request,
                    @SiteParam Site site,
                    @PathVariable("key") String key,
                    @PathVariable("version") String version)
      throws IOException {
    if (Ints.tryParse(version) == null){
      throw new NotFoundException("Not a valid version integer: " + version);
    }

    serve(response, request, key, Optional.of(version));
  }

  private void serve(HttpServletResponse responseToClient, HttpServletRequest requestFromClient,
                     String key, Optional<String> version)
      throws IOException {
    Header[] assetHeaders = copyAssetRequestHeaders(requestFromClient);
    try (AssetServiceResponse repoResponse = contentRepoService.request(key, version, assetHeaders)) {
      copyAssetServiceResponse(repoResponse, responseToClient);
    } catch (EntityNotFoundException e) {
      String message = String.format("Not found in repo: [key: %s, version: %s]",
          key, version.orNull());
      throw new NotFoundException(message, e);
    }
  }

}
