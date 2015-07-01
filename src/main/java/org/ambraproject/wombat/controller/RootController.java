package org.ambraproject.wombat.controller;

import com.google.common.base.Optional;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteMapping;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.service.remote.EditorialContentService;
import org.ambraproject.wombat.util.CacheParams;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;

/**
 * Handles requests to the application root.
 */
@Controller
public class RootController extends WombatController {

  @Autowired
  private ServletContext servletContext;

  @Autowired
  private EditorialContentService editorialContentService;

  private String getResourceAsBase64(String path) throws IOException {
    byte[] bytes;
    try (InputStream stream = servletContext.getResourceAsStream(path)) {
      bytes = IOUtils.toByteArray(stream);
    }
    return Base64.encodeBase64String(bytes);
  }

  /**
   * Show a page in response to the application root.
   * <p/>
   * This is here only for development/debugging: if you browse to the application root while you're setting up, this
   * page is more useful than an error message. But all end-user-facing pages should belong to one of the sites in
   * {@code siteSet}.
   */
  @SiteMapping(excluded={"DesktopPlosCollections","MobilePlosCollections"}) // temporary (see tempCollectionsHome below)
  @RequestMapping(value = "/", method = RequestMethod.GET)
  public String home(Model model) throws Exception {
    model.addAttribute("siteKeys", siteSet.getSiteKeys());

    /*
     * Must supply this image data embedded (if at all), because there's no URL at which we could put it
     * without colliding with the site namespace.
     */
    model.addAttribute("imageCode", getResourceAsBase64("/WEB-INF/themes/root/app/wombat.jpg"));

    return "//approot";
  }

  /**
   * Home controller for PLOS Collections (which uses site content with a reserved slug called "homepage")
   * NOTE: This is a temporary solution until DPRO-1238 is completed this sprint
   * TODO: specify SiteContentController and "/" namespace for DesktopPlosCollections in wombat.yaml config
   */
  @SiteMapping(value={"DesktopPlosCollections","MobilePlosCollections"})
  @RequestMapping(value = "/", method = RequestMethod.GET)
  public String tempCollectionsHome(Model model, @SiteParam Site site) throws Exception {
    String repoKey = "desktop.collections.s.homepage";

    String cacheKey = "siteContent_meta:" + repoKey;

    try {
      // Check for validity of the content repo key prior to rendering page. Return a 404 if no object found.
      editorialContentService.requestMetadata(CacheParams.create(cacheKey), repoKey, Optional.<Integer>absent());
    } catch (EntityNotFoundException e) {
      throw new NotFoundException(e);
    }
    model.addAttribute("siteContentRepoKey", repoKey);
    return site + "/ftl/siteContent/container";
  }

}
