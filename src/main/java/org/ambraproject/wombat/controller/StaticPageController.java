package org.ambraproject.wombat.controller;

import org.ambraproject.wombat.config.site.Site;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.io.InputStream;

/**
 * Serves a static page directly from a site's theme. "Static" in this context means that no data is provided to the
 * model. The page is still generated from the FreeMarker engine and may call programmatic directives.
 */
@Controller
public class StaticPageController extends WombatController {

  @RequestMapping(name = "staticPage", value = "/static/{pageName}")
  public String renderStaticPage(Site site, @PathVariable String pageName) {
    if (!doesStaticPageExist(site, pageName)) {
      throw new NotFoundException();
    }
    return site + "/ftl/static/" + pageName;
  }

  private static boolean doesStaticPageExist(Site site, String pageName) {
    String resourcePath = String.format("/ftl/static/%s.ftl", pageName);
    try (InputStream stream = site.getTheme().getStaticResource(resourcePath)) {
      return stream != null;
      // TODO: Performance?
      // Maybe Theme wants a method that only checks the existence of a resource without opening a stream to it.
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
