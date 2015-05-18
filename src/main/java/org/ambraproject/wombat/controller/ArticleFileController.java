package org.ambraproject.wombat.controller;

import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.service.remote.SoaRequest;
import org.ambraproject.wombat.util.HttpMessageUtil;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class ArticleFileController extends ArticleSpaceController {

  /**
   * Serve an asset file from a particular revision of an article.
   * <p>
   * It may be desirable to make {@code revision} optional, but that would require help from the service API to
   * disambiguate it to the asset's parent article's default revision. For now, just require it.
   */
  @RequestMapping(value = {"/article/file", "/{site}/article/file"})
  public void serveFile(HttpServletRequest requestFromClient,
                        HttpServletResponse responseToClient,
                        @SiteParam Site site,
                        @RequestParam(value = ID_PARAM, required = true) String assetId,
                        @RequestParam(value = REVISION_PARAM, required = true) String revision,
                        @RequestParam(value = "type", required = true) String fileType)
      throws IOException {
    SoaRequest requestToService = SoaRequest.request("assetfiles")
        .addParameter("id", assetId).addParameter("r", revision).addParameter("file", fileType)
        .build();
    try (CloseableHttpResponse responseFromService = soaService.requestAsset(requestToService,
        HttpMessageUtil.getRequestHeaders(requestFromClient, ASSET_REQUEST_HEADER_WHITELIST))) {
      HttpMessageUtil.copyResponseWithHeaders(responseFromService, responseToClient, ASSET_RESPONSE_HEADER_FILTER);
    } catch (EntityNotFoundException e) {
      throw new NotFoundException(e);
    }
  }

}
