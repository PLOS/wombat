package org.ambraproject.wombat.controller;

import com.google.common.base.Strings;
import org.ambraproject.wombat.config.Site;
import org.ambraproject.wombat.service.remote.SearchService;
import org.ambraproject.wombat.service.remote.SoaService;
import org.ambraproject.wombat.service.remote.SolrSearchService;
import org.ambraproject.wombat.service.UnmatchedSiteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles requests for a site home page.
 */
@Controller
public class HomeController extends WombatController {
  private static final Logger log = LoggerFactory.getLogger(HomeController.class);

  @Autowired
  private SolrSearchService solrSearchService;

  @Autowired
  private SoaService soaService;

  /**
   * Enumerates the allowed values for the section parameter for this page.
   */
  private static enum Section {
    RECENT,
    POPULAR,
    IN_THE_NEWS;
  }

  @RequestMapping(value = "/{site}", method = RequestMethod.GET)
  public String serveHomepage(HttpServletRequest request, Model model, @PathVariable("site") String siteParam,
                              @RequestParam(value = "section", required = false) String sectionParam)
      throws IOException {
    if (!request.getServletPath().endsWith("/")) {
      return "redirect:" + siteParam + "/";
    }

    Site site;
    try {
      site = siteSet.getSite(siteParam);
    } catch (UnmatchedSiteException e) {
      throw new NotFoundException(e);
    }

    Map<String, Object> homepageConfig = site.getTheme().getConfigMap("homepage");
    int resultsPerPage = ((Number) homepageConfig.get("resultsPerPage")).intValue();
    List<String> supportedSections = (List<String>) homepageConfig.get("sections");
    model.addAttribute("supportedSections", supportedSections);

    Section section = null;
    if (!Strings.isNullOrEmpty(sectionParam)) {
      try {
        section = Section.valueOf(sectionParam.toUpperCase());
      } catch (IllegalArgumentException e) {
        // Fall through and use the default
      }
    }
    if (section == null || !supportedSections.contains(section.name().toLowerCase())) {
      section = Section.RECENT; // default value
    }
    model.addAttribute("selectedSection", section.name().toLowerCase());

    switch (section) {
      case RECENT:
        HomeController.populateWithArticleList(request, model, site, resultsPerPage, solrSearchService,
            SolrSearchService.SolrSortOrder.DATE_NEWEST_FIRST);
        break;

      case POPULAR:
        HomeController.populateWithArticleList(request, model, site, resultsPerPage, solrSearchService,
            SolrSearchService.SolrSortOrder.MOST_VIEWS_30_DAYS);
        break;

      case IN_THE_NEWS:
        model.addAttribute("articles", getInTheNewsArticles(site.getJournalKey()));
        break;

      default:
        throw new IllegalStateException("Unexpected section value " + section);
    }

    return site.getKey() + "/ftl/home/home";
  }

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
                                             int resultsPerPage,
                                             SearchService searchService,
                                             SolrSearchService.SolrSortOrder order) {
    int start = 0;
    String page = request.getParameter("page");
    if (!Strings.isNullOrEmpty(page)) {
      start = (Integer.parseInt(page) - 1) * resultsPerPage;
    }
    model.addAttribute("resultsPerPage", resultsPerPage);

    try {
      Map<?, ?> articles = searchService.getHomePageArticles(site, start, resultsPerPage, order);
      model.addAttribute("articles", articles);
    } catch (IOException e) {
      log.error("Could not populate home page with articles from Solr", e);
      // Render the rest of the page without the article list
      // The FreeMarker template should provide an error message if "articles" is missing from the model
    }
  }

  private Map getInTheNewsArticles(String journalKey) throws IOException {
    String requestAddress = "journals/" + journalKey + "?inTheNewsArticles";
    List<Map<String, Object>> inTheNewsArticles = soaService.requestObject(requestAddress, List.class);

    // From the presentation layer's perspective, all three of these article lists look the same.
    // However, two of them come from solr, and one from rhino.  Unfortunately solr uses
    // "id" as the name of the DOI attribute, while rhino uses "doi".  So this hack is
    // necessary...
    for (Map<String, Object> article : inTheNewsArticles) {
      article.put("id", article.get("doi"));
    }
    Map<String, Object> results = new HashMap<>();
    results.put("docs", inTheNewsArticles);
    return results;
  }

}
