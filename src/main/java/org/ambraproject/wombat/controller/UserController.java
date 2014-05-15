package org.ambraproject.wombat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * Controller for user related actions
 */
@Controller
public class UserController extends WombatController {

  @RequestMapping(value={"/user/secure/login", "/{site}/user/secure/login"})
  public ModelAndView redirectToOriginalLink(HttpServletRequest request) {
    return new ModelAndView("redirect:" + request.getHeader("Referer"));
  }
}
