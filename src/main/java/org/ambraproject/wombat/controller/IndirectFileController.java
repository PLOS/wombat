package org.ambraproject.wombat.controller;

import com.google.common.net.HttpHeaders;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.service.remote.EditorialContentService;
import org.ambraproject.wombat.util.CacheParams;
import org.ambraproject.wombat.util.HttpMessageUtil;
import org.ambraproject.wombat.util.ReproxyUtil;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Forwards requests for files to the content repository.
 */
@Controller
public class IndirectFileController extends WombatController {

  @Autowired
  private EditorialContentService editorialContentService;

  @RequestMapping(name = "repoObject", value = "/indirect/{key}")
  public void serve(HttpServletResponse response,
                    HttpServletRequest request,
                    @SiteParam Site site,
                    @PathVariable("key") String key)
      throws IOException {
    serve(response, request, key, Optional.empty());
  }

  @RequestMapping(name = "versionedRepoObject", value = "/indirect/{key}/{version}")
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

  @RequestMapping(name = "repoObjectUsingPublicUrl", value = "/s/file")
  public void serveWithPublicUrl(HttpServletResponse response,
                                 HttpServletRequest request,
                                 @SiteParam Site site,
                                 @RequestParam(value = "id", required = true) String key)
      throws IOException {
    serve(response, request, key, Optional.empty());
  }

  private static final int REPROXY_CACHE_FOR = 6 * 60 * 60; // 6 hours

  private void serve(HttpServletResponse responseToClient, HttpServletRequest requestFromClient,
                     String key, Optional<Integer> version)
      throws IOException {
    String cacheKey = "indirect:" + CacheParams.createKeyHash(key, String.valueOf(version.orElse(null)));
    Map<String, Object> fileMetadata;

    try {
      fileMetadata = editorialContentService.requestMetadata(CacheParams.create(cacheKey), key, version);
    } catch (EntityNotFoundException e) {
      String message = String.format("Not found in repo: [key: %s, version: %s]",
          key, version.orElse(null));
      throw new NotFoundException(message, e);
    }

    String contentType = (String) fileMetadata.get("contentType");
    if (contentType != null) {
      responseToClient.setHeader(HttpHeaders.CONTENT_TYPE, contentType);
    }

    String downloadName = (String) fileMetadata.get("downloadName");
    if (downloadName != null) {
      responseToClient.setHeader(HttpHeaders.CONTENT_DISPOSITION, "filename=" + downloadName);
    }

    List<String> reproxyUrls = (List<String>) fileMetadata.get("reproxyURL");
    if (ReproxyUtil.applyReproxy(requestFromClient, responseToClient, reproxyUrls, REPROXY_CACHE_FOR)) {
      return;
    }

    Collection<Header> assetHeaders = HttpMessageUtil.getRequestHeaders(requestFromClient, ASSET_REQUEST_HEADER_WHITELIST);
    try (CloseableHttpResponse repoResponse = editorialContentService.request(key, version, assetHeaders)) {
      HttpMessageUtil.copyResponseWithHeaders(repoResponse, responseToClient, WombatController::filterAssetResponseHeaders);
    } catch (EntityNotFoundException e) {
      String message = String.format("Not found in repo: [key: %s, version: %s]",
          key, version.orElse(null));
      throw new NotFoundException(message, e);
    }
  }

}
