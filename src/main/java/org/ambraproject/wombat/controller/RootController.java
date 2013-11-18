package org.ambraproject.wombat.controller;

import org.ambraproject.wombat.config.SiteSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Handles requests to the application root.
 */
@Controller
public class RootController {

  @Autowired
  private SiteSet siteSet;

  /**
   * Show a page in response to the application root.
   * <p/>
   * This is here only for development/debugging: if you browse to the application root while you're setting up, this
   * page is more useful than an error message. But all end-user-facing pages should belong to one of the sites in
   * {@code siteSet}.
   */
  @RequestMapping(value = "/", method = RequestMethod.GET)
  public String home(Model model) throws Exception {
    model.addAttribute("siteKeys", siteSet.getSiteKeys());
    return "//approot";
  }

}
