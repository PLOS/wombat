package org.ambraproject.wombat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Locale;

@Controller
public class TemplatePageController {

  @RequestMapping("/{journal}/**")
  public String servePage(HttpServletRequest request, Locale locale, Model model,
                          @PathVariable("journal") String journal)
      throws IOException {
    // Kludge to get "static/**"
    String servletPath = request.getServletPath();
    String pagePath = servletPath.substring(1);

    return pagePath;
  }

}
