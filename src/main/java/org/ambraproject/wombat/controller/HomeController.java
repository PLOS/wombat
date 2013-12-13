package org.ambraproject.wombat.controller;

import com.google.common.base.Strings;
import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.ambraproject.wombat.config.Site;
import org.ambraproject.wombat.config.SiteSet;
import org.ambraproject.wombat.service.SearchService;
import org.ambraproject.wombat.service.SolrSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

/**
 * Handles requests for a site home page.
 */
@Controller
public class HomeController extends WombatController {
  private static final Logger log = LoggerFactory.getLogger(HomeController.class);

  @Autowired
  private RuntimeConfiguration runtimeConfiguration;

  @Autowired
  private SolrSearchService solrSearchService;

  /**
   * Simply selects the home view to render by returning its name.
   */
  @RequestMapping(value = "/{site}/", method = RequestMethod.GET)
  public String home(HttpServletRequest request, Locale locale, Model model, @PathVariable("site") String siteParam)
      throws Exception {
    Site site = siteSet.getSite(siteParam);

    populateWithArticleList(request, model, site, solrSearchService, SolrSearchService.SolrSortOrder.DATE_NEWEST_FIRST);

    // Certain sites (such as PLOS ONE) are highly customized vs. the "normal" wombat themes,
    // and not only require their own views, but also custom data to be passed into that view.
    // Here we check to see if this is the case for this site.
    ControllerHook hook = runtimeConfiguration.getHomePageHook(siteParam);
    if (hook != null) {
      hook.populateCustomModelAttributes(request, model);
    }
    return site.getKey() + "/ftl/home";
  }

  private static final int RESULTS_PER_PAGE = 7; // TODO: Convert to configuration hook in theme?

  /**
   * Populate a model object with a feed of articles from Solr.
   * <p/>
   * This logic generally should be private to this class; it is public only for reuse by {@link ControllerHook}s.
   *
   * @param request       the request for a home page
   * @param model         the response's model
   * @param site          the site of the home page
   * @param searchService the service through which to access Solr
   * @param order         the order in which to list the articles on the home page
   */
  public static void populateWithArticleList(HttpServletRequest request, Model model, Site site,
                                             SearchService searchService,
                                             SolrSearchService.SolrSortOrder order) {
    int start = 1;
    String page = request.getParameter("page");
    if (!Strings.isNullOrEmpty(page)) {
      start = (Integer.parseInt(page) - 1) * RESULTS_PER_PAGE + 1;
    }
    model.addAttribute("resultsPerPage", RESULTS_PER_PAGE);

    try {
      Map<?, ?> articles = searchService.simpleSearch(null, site, start, RESULTS_PER_PAGE,
          order, SolrSearchService.SolrDateRange.ALL_TIME);
      model.addAttribute("articles", articles);
    } catch (IOException e) {
      log.error("Could not populate home page with articles from Solr", e);
      // Render the rest of the page without the article list
      // The FreeMarker template should provide an error message if "articles" is missing from the model
    }
  }

}
