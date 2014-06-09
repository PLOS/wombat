package org.ambraproject.wombat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * Controller for user related actions
 */
@Controller
public class UserController extends WombatController {

  @RequestMapping(value={"/user/secure/login", "/{site}/user/secure/login"})
  public ModelAndView redirectToOriginalLink(HttpServletRequest request, @RequestParam("page") String page) {
    // page param should contain the url to the location we want to send the user to
    String contextPath = request.getContextPath();
    int index = page.indexOf(contextPath);
    if (index != -1 ) {
      page = page.substring(index + contextPath.length());
    }

    return new ModelAndView("redirect:" + page);
  }

  @RequestMapping(value={"/user/logout", "/{site}/user/logout" })
  public ModelAndView redirectToSignOut(HttpServletRequest request) {
    return new ModelAndView("redirect:" + request.getHeader("Referer"));
  }
}
