package org.ambraproject.wombat.controller;

import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.service.remote.SoaService;
import org.ambraproject.wombat.util.HttpMessageUtil;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class FigureImageController extends WombatController {
  private static final Logger log = LoggerFactory.getLogger(FigureImageController.class);

  @Autowired
  private SoaService soaService;

  /**
   * Serve the asset file for an identified figure thumbnail.
   */
  @RequestMapping(value = {"/article/file", "/{site}/article/file"})
  public void serveFigureImage(HttpServletRequest request,
                               HttpServletResponse response,
                               @SiteParam Site site,
                               @RequestParam("id") String assetId,
                               @RequestParam("type") String fileType)
      throws IOException {
    requireNonemptyParameter(assetId);
    requireNonemptyParameter(fileType);
    try (CloseableHttpResponse responseFromService = soaService.requestAsset(assetId, fileType,
        HttpMessageUtil.getRequestHeaders(request, ASSET_REQUEST_HEADER_WHITELIST))) {
      HttpMessageUtil.copyResponseWithHeaders(responseFromService, response, ASSET_RESPONSE_HEADER_FILTER);
    } catch (EntityNotFoundException e) {
      throw new NotFoundException(e);
    }
  }

}
