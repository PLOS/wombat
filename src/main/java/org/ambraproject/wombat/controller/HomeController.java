package org.ambraproject.wombat.controller;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.service.remote.SoaService;
import org.ambraproject.wombat.service.remote.SolrSearchService;
import org.ambraproject.wombat.util.DoiSchemeStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.EnumSet;
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

    /**
     * @throws java.lang.IllegalArgumentException if name is not matched
     */
    private static Section forCaseInsensitiveName(String name) {
      return Section.valueOf(name.toUpperCase());
    }
  }

  private int parseNumberParameter(String param, int minValue) {
    if (param == null) {
      return minValue;
    }
    try {
      int value = Integer.parseInt(param);
      return (value < minValue) ? minValue : value;
    } catch (NumberFormatException e) {
      return minValue;
    }
  }

  @RequestMapping(value = "/{site}", method = RequestMethod.GET) // TODO Map to "/"
  public String serveHomepage(HttpServletRequest request, Model model, @SiteParam Site site,
                              @RequestParam(value = "section", required = false) String sectionParam,
                              @RequestParam(value = "page", required = false) String pageParam)
      throws IOException {
    if (!request.getServletPath().endsWith("/")) {
      return "redirect:" + site.getKey() + "/"; // TODO Support other site types
    }

    Map<String, Object> homepageConfig = site.getTheme().getConfigMap("homepage");
    int resultsPerPage = ((Number) homepageConfig.get("resultsPerPage")).intValue();
    List<String> supportedSections = (List<String>) homepageConfig.get("sections");
    model.addAttribute("supportedSections", supportedSections);

    Section section = null;
    if (!Strings.isNullOrEmpty(sectionParam)) {
      try {
        section = Section.forCaseInsensitiveName(sectionParam);
      } catch (IllegalArgumentException e) {
        // Fall through and use the default
      }
    }
    if (section == null || !supportedSections.contains(section.name().toLowerCase())) {
      // Use the default section specified in the theme
      String defaultSectionName = (String) homepageConfig.get("defaultSection");
      try {
        section = Section.forCaseInsensitiveName(defaultSectionName);
      } catch (IllegalArgumentException e) {
        String message = String.format("Invalid defaultSection value in homepage config: \"%s\". Expected one of: %s",
            defaultSectionName, EnumSet.allOf(Section.class));
        throw new RuntimeException(message, e);
      }
    }
    model.addAttribute("selectedSection", section.name().toLowerCase());

    int page = parseNumberParameter(pageParam, 1);
    int start = (page - 1) * resultsPerPage;
    model.addAttribute("resultsPerPage", resultsPerPage);

    Map<?, ?> articles;
    try {
      switch (section) {
        case RECENT:
          articles = solrSearchService.getHomePageArticles(site, start, resultsPerPage,
              SolrSearchService.SolrSortOrder.DATE_NEWEST_FIRST);
          break;

        case POPULAR:
          articles = solrSearchService.getHomePageArticles(site, start, resultsPerPage,
              SolrSearchService.SolrSortOrder.MOST_VIEWS_30_DAYS);
          break;

        case IN_THE_NEWS:
          articles = getInTheNewsArticles(site.getJournalKey());
          break;

        default:
          throw new IllegalStateException("Unexpected section value " + section);
      }
    } catch (IOException e) {
      log.error("Could not populate home page with articles from Solr", e);
      articles = null;
      // Render the rest of the page without the article list
      // The FreeMarker template should provide an error message if "articles" is missing from the model
    }

    if (articles != null) {
      model.addAttribute("articles", articles);
    }

    return site.getKey() + "/ftl/home/home";
  }

  private Map<String, Object> getInTheNewsArticles(String journalKey) throws IOException {
    String requestAddress = "journals/" + journalKey + "?inTheNewsArticles";
    List<Map<String, Object>> inTheNewsArticles = (List<Map<String, Object>>)
        soaService.requestObject(requestAddress, List.class);

    // From the presentation layer's perspective, all three of these article lists look the same.
    // However, two of them come from solr, and one from rhino.  Unfortunately solr uses
    // "id" as the name of the DOI attribute, while rhino uses "doi".  So this hack is
    // necessary.  (We also take the opportunity to strip off the DOI scheme.)
    for (Map<String, Object> article : inTheNewsArticles) {
      article = DoiSchemeStripper.strip(article);
      article.put("id", article.get("doi"));
    }
    Map<String, Object> results = Maps.newHashMapWithExpectedSize(1);
    results.put("docs", inTheNewsArticles);
    return results;
  }

}
