package org.ambraproject.wombat.controller;

import org.ambraproject.wombat.config.site.Siteless;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * Controller for user related actions
 */
@Controller
public class UserController extends WombatController {
  private static final Logger log = LoggerFactory.getLogger(UserController.class);

  @Autowired
  private ArticleApi articleApi;

  @RequestMapping(name = "userLogin", value = "/user/secure/login")
  public ModelAndView redirectToOriginalLink(HttpServletRequest request, @RequestParam("page") String page) {
    // page param should contain the url to the location we want to send the user to
    String contextPath = request.getContextPath();
    int index = page.indexOf(contextPath);
    if (index != -1) {
      page = page.substring(index + contextPath.length());
    }

    return new ModelAndView("redirect:" + page);
  }

  @Siteless
  @RequestMapping(name = "userLogout", value = "/user/logout")
  public ModelAndView redirectToSignOut(@RequestHeader(value = "Referer", required = false) String referrer) {
    if (referrer == null) {
      // We expect a typical user never to navigate to this page unless referred there by the logout service.
      // If we used 'required = true' on the referrer param, the user would be an unstyled 500 page when it is missing.
      // Here, we define that we instead want to indicate that no content is at the URL.
      throw new NotFoundException();
    }
    return new ModelAndView("redirect:" + referrer);
  }

}
