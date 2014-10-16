package org.ambraproject.wombat.controller;

import com.google.common.base.Optional;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.service.remote.ContentRepoService;
import org.ambraproject.wombat.util.HttpMessageUtil;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

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
    serve(response, request, key, Optional.<Integer>absent());
  }

  @RequestMapping(value = {"indirect/{key}/{version}", "{site}/indirect/{key}/{version}"})
  public void serve(HttpServletResponse response,
                    HttpServletRequest request,
                    @SiteParam Site site,
                    @PathVariable("key") String key,
                    @PathVariable("version") String version)
      throws IOException {
    Integer versionInt;
    try {
      versionInt = Integer.valueOf(version);
    } catch (NumberFormatException e) {
      throw new NotFoundException("Not a valid version integer: " + version, e);
    }
    serve(response, request, key, Optional.of(versionInt));
  }

  private void serve(HttpServletResponse responseToClient, HttpServletRequest requestFromClient,
                     String key, Optional<Integer> version)
      throws IOException {
    Collection<Header> assetHeaders = HttpMessageUtil.getRequestHeaders(requestFromClient, ASSET_REQUEST_HEADER_WHITELIST);
    try (CloseableHttpResponse repoResponse = contentRepoService.request(key, version, assetHeaders)) {
      HttpMessageUtil.copyResponseWithHeaders(repoResponse, responseToClient, ASSET_RESPONSE_HEADER_WHITELIST);
    } catch (EntityNotFoundException e) {
      String message = String.format("Not found in repo: [key: %s, version: %s]",
          key, version.orNull());
      throw new NotFoundException(message, e);
    }
  }

}
