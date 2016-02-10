package org.ambraproject.wombat.controller;

import com.google.common.collect.ImmutableList;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.rss.WombatFeed;
import org.ambraproject.wombat.rss.WombatRssViewer;
import org.ambraproject.wombat.service.remote.ArticleSearchQuery;
import org.ambraproject.wombat.service.remote.SolrSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.Map;

@Controller
public class FeedController {

  @Autowired
  private WombatFeed myFeed;

  @Autowired
  private WombatRssViewer wombatRssViewer;

  @Autowired
  private SolrSearchService solrSearchService;

  @RequestMapping(name = "feed", value = "/feed.*", method = RequestMethod.GET)
  public ModelAndView getContent(@SiteParam Site site) {
    ModelAndView mav = new ModelAndView();

    // Temporarily hard-coded results. TODO: Wire to actual Solr service.
    Map<String, ?> dummyResults;
    try {
      dummyResults = solrSearchService.search(ArticleSearchQuery.builder()
          .setQuery("gene")
          .setJournalKeys(ImmutableList.of("PLoSONE"))
          .setStart(0).setRows(15)
          .setSimple(true).build());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    mav.addObject("site", site);
    mav.addObject("solrResults", dummyResults.get("docs"));

    mav.setView(wombatRssViewer);
    return mav;
  }
}