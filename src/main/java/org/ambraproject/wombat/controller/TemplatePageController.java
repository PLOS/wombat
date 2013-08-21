package org.ambraproject.wombat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
public class TemplatePageController {

  @RequestMapping("/{site}/page/{pageName}")
  public String servePage(@PathVariable("site") String site,
                          @PathVariable("pageName") String pageName)
      throws IOException {
    return site + "/ftl/page/" + pageName;
  }

}
