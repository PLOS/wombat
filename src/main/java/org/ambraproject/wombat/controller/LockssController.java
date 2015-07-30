package org.ambraproject.wombat.controller;

import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.service.ArticleArchiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.text.ParseException;

/**
 * Responsible for providing the article DOIs published in a given year and month
 */
@Controller
public class LockssController extends WombatController {

  @Autowired
  ArticleArchiveService articleArchiveServiceImpl;

  @RequestMapping(value={"{site}/lockss.txt"}, method = RequestMethod.GET)
  public String getLockssPermission(@SiteParam Site site) {
    return site + "/ftl/lockss/permission";
  }

  @RequestMapping(value="{site}/lockss-manifest", method = RequestMethod.GET)
  public String getYearsForJournal(@SiteParam Site site, Model model) throws IOException, ParseException {
    int[] yearRange = articleArchiveServiceImpl.getYearForJournal(site);
    model.addAttribute("yearRange", yearRange);
    return site + "/ftl/lockss/years";
  }

  @RequestMapping(value={"{site}/lockss-manifest/vol_{year}"}, method = RequestMethod.GET)
  public String getMonthsForYear(@SiteParam Site site, @PathVariable String year, Model model) {
    String[] months = articleArchiveServiceImpl.getMonthsForYear(year);
    model.addAttribute("year", year);
    model.addAttribute("months", months);
    return site + "/ftl/lockss/months";
  }

  @RequestMapping(value={"{site}/lockss-manifest/vol_{year}/{month}"}, method = RequestMethod.GET)
  public String getArticlesPerMonth(@SiteParam Site site, @PathVariable String year,
                                    @PathVariable String month, Model model) throws IOException {
    String[] dois = articleArchiveServiceImpl.getArticleDoisPerMonth(site, year, month);
    model.addAttribute("month", month);
    model.addAttribute("year", year);
    model.addAttribute("dois", dois);

    return site + "/ftl/lockss/dois";
  }
}
