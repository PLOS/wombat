package org.ambraproject.wombat.controller;

import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.service.LockssService;
import org.ambraproject.wombat.service.LockssServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;

/**
 * Responsible for providing the article DOIs published in a given year and month
 */
@Controller
public class LockssController extends WombatController {

  @Autowired
  LockssService lockssServiceImpl;

  @RequestMapping(value={"{site}/lockss.txt"}, method = RequestMethod.GET)
  public String getLockssPermission() {
    return "";
  }

  @RequestMapping(value={"{site}/lockss-manifest/vol_{year}"}, method = RequestMethod.GET)
  public String getMonthsForYear(@SiteParam Site site, @PathVariable String year, Model model) {
    String[] months = lockssServiceImpl.getMonthsForYear(year);
    model.addAttribute("year", year);
    model.addAttribute("months", months);
    return site + "/ftl/lockss/lockss";
  }

  @RequestMapping(value={"{site}/lockss-manifest/vol_{year}/{month}"}, method = RequestMethod.GET)
  public String getArticlesPerMonth(@SiteParam Site site, @PathVariable String year,
                                    @PathVariable String month, Model model) throws IOException {
    String[] dois = lockssServiceImpl.getArticleDoisPerMonth(site, year, month);
    model.addAttribute("month", month);
    model.addAttribute("year", year);
    model.addAttribute("dois", dois);

    return site + "/ftl/lockss/dois";
  }
}
