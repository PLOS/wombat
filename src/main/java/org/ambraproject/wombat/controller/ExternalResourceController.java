package org.ambraproject.wombat.controller;

import com.google.common.net.HttpHeaders;
import org.ambraproject.wombat.config.site.JournalSpecific;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.service.remote.ContentKey;
import org.ambraproject.wombat.service.remote.EditorialContentApi;
import org.ambraproject.wombat.util.CacheKey;
import org.ambraproject.wombat.util.HttpMessageUtil;
import org.ambraproject.wombat.util.ReproxyUtil;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * Forwards requests for files to the content repository.
 */
@Controller
public class ExternalResourceController extends WombatController {

  public static final String EXTERNAL_RESOURCE_NAMESPACE = "indirect";

  private static final Logger log = LoggerFactory.getLogger(ExternalResourceController.class);

  @Autowired
  private EditorialContentApi editorialContentApi;

  @JournalSpecific
  @RequestMapping(name = "repoObject", value = "/" + EXTERNAL_RESOURCE_NAMESPACE + "/{key}")
  public void serve(HttpServletResponse response,
                    HttpServletRequest request,
                    Site site,
                    @PathVariable("key") String key)
      throws IOException {
    serve(response, request, ContentKey.createForLatestVersion(key));
  }

  @JournalSpecific
  @RequestMapping(name = "versionedRepoObject", value = "/" + EXTERNAL_RESOURCE_NAMESPACE + "/{key}/{version}")
  public void serve(HttpServletResponse response,
                    HttpServletRequest request,
                    Site site,
                    @PathVariable("key") String key,
                    @PathVariable("version") String version)
      throws IOException {
    Integer versionInt;
    try {
      versionInt = Integer.valueOf(version);
    } catch (NumberFormatException e) {
      throw new NotFoundException("Not a valid version integer: " + version, e);
    }
    serve(response, request, ContentKey.createForVersion(key, versionInt));
  }

  @JournalSpecific
  @RequestMapping(name = "repoObjectUsingPublicUrl", value = "/s/file")
  public void serveWithPublicUrl(HttpServletResponse response,
                                 HttpServletRequest request,
                                 Site site,
                                 @RequestParam(value = "id", required = true) String key)
          throws IOException {
    serve(response, request, ContentKey.createForLatestVersion(key));
  }

  private static final int REPROXY_CACHE_FOR = 6 * 60 * 60; // 6 hours

  private void serve(HttpServletResponse responseToClient, HttpServletRequest requestFromClient,
                     ContentKey key)
      throws IOException {
    CacheKey cacheKey = key.asCacheKey("indirect");
    Map<String, Object> fileMetadata;

    try {
      fileMetadata = editorialContentApi.requestMetadata(cacheKey, key);
    } catch (EntityNotFoundException e) {
      String message = String.format("Not found in repo: %s", key.toString());
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
    try (CloseableHttpResponse repoResponse = editorialContentApi.request(key, assetHeaders)) {
      forwardAssetResponse(repoResponse, responseToClient, false);
    } catch (EntityNotFoundException e) {
      throw new NotFoundException("File not found in repo: " + key, e);
    }
  }

}
