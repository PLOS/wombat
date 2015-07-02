package org.ambraproject.wombat.controller;

import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.service.LockssService;
import org.ambraproject.wombat.service.remote.SolrSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Responsible for providing the article DOIs published in a given year and month
 */
@Controller
public class LockssController extends WombatController {

  @Autowired
  LockssService lockssService;

  @Autowired
  SolrSearchService solrSearchService;

  @RequestMapping(value={"{site}/lockss.txt"}, method = RequestMethod.GET)
  public String getLockssPermission() {
    return "";
  }
  @RequestMapping(value={"{site}/lockss-manifest/vol_{year}"}, method = RequestMethod.GET)
  public String getMonthsForYear(@SiteParam Site site, @PathVariable String year, Model model) {
    String[] months = lockssService.getMonthsForYear(year);
    model.addAttribute("year", year);
    model.addAttribute("months", months);
    return site + "/ftl/lockss/lockss";
  }

  @RequestMapping(value={"{site}/lockss-manifest/vol_{year}/{month}"}, method = RequestMethod.GET)
  public String getArticlesPerMonth(@SiteParam Site site, @PathVariable String year,
                                    @PathVariable String month) {

    return site + "/ftl/lockss/dois";
  }
}
