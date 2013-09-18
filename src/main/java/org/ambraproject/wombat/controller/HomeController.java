package org.ambraproject.wombat.controller;

import org.ambraproject.wombat.config.Site;
import org.ambraproject.wombat.config.SiteSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Locale;

/**
 * Handles requests for a site home page.
 */
@Controller
public class HomeController {

  private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

  @Autowired
  private SiteSet siteSet;

  /**
   * Simply selects the home view to render by returning its name.
   */
  @RequestMapping(value = "/{site}/", method = RequestMethod.GET)
  public String home(Locale locale, Model model, @PathVariable("site") String siteParam) throws Exception {
    logger.info("Welcome home! The client locale is {}.", locale);

    Site site = siteSet.getSite(siteParam);
    return site.getKey() + "/ftl/home";
  }

}
