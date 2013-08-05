package org.ambraproject.wombat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
public class TemplatePageController {

  @RequestMapping("/{journal}/page/{pageName}")
  public String servePage(@PathVariable("journal") String journal,
                          @PathVariable("pageName") String pageName)
      throws IOException {
    return journal + "/ftl/page/" + pageName;
  }

}
