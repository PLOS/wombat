package org.ambraproject.wombat.controller;

import org.ambraproject.wombat.config.site.MappingSiteScope;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteScope;
import org.ambraproject.wombat.config.theme.Theme;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.io.InputStream;

@Controller
public class TemplatePageController extends WombatController {

  @MappingSiteScope(SiteScope.JOURNAL_SPECIFIC)
  @RequestMapping(name = "templatePage", value = "/page/{pageName}")
  public String servePage(Site site,
                          @PathVariable("pageName") String pageName)
      throws IOException {
    // Validate that the page exists. (In order to display a nice 404 page,
    // we must do this before returning a template path, rather than in an @ExceptionHandler.)
    Theme theme = site.getTheme();
    try (InputStream page = theme.getStaticResource("ftl/page/" + pageName + ".ftl")) {
      if (page == null) {
        throw new NotFoundException();
      }
    }

    return site + "/ftl/page/" + pageName;
  }

}
