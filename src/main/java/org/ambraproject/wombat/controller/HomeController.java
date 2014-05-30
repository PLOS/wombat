package org.ambraproject.wombat.controller;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.service.RecentArticleService;
import org.ambraproject.wombat.service.remote.SoaService;
import org.ambraproject.wombat.service.remote.SolrSearchService;
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
import java.util.ArrayList;
import java.util.Collection;
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

  @Autowired
  private RecentArticleService recentArticleService;

  /**
   * Extract {@code docs} element; rename {@code "id"} to {@code "doi"} to match the service API.
   */
  private static List<Object> sanitizeSolrResults(Map<?, ?> solrResults) {
    List<Object> articles = (List<Object>) solrResults.get("docs");
    for (Object articleObj : articles) {
      Map<String, Object> article = (Map<String, Object>) articleObj;
      article.put("doi", article.remove("id"));
    }
    return articles;
  }

  /**
   * Enumerates the allowed values for the section parameter for this page.
   */
  private static enum SectionType {
    RECENT {
      @Override
      public List<Object> getArticles(HomeController context, SectionSpec section, Site site, int start) throws IOException {
        Map<?, ?> result = context.solrSearchService.getHomePageArticles(site, start, section.resultCount,
            SolrSearchService.SolrSortOrder.DATE_NEWEST_FIRST);
        return sanitizeSolrResults(result);
      }
    },
    POPULAR {
      @Override
      public List<Object> getArticles(HomeController context, SectionSpec section, Site site, int start) throws IOException {
        Map<?, ?> result = context.solrSearchService.getHomePageArticles(site, start, section.resultCount,
            SolrSearchService.SolrSortOrder.MOST_VIEWS_30_DAYS);
        return sanitizeSolrResults(result);
      }
    },
    IN_THE_NEWS {
      @Override
      public List<Object> getArticles(HomeController context, SectionSpec section, Site site, int start) throws IOException {
        return (List<Object>) getInTheNewsArticles(context.soaService, site.getJournalKey());
      }
    };

    /**
     * @throws java.lang.IllegalArgumentException if name is not matched
     */
    private static SectionType forCaseInsensitiveName(String name) {
      return SectionType.valueOf(name.toUpperCase());
    }

    public abstract List<Object> getArticles(HomeController context, SectionSpec section, Site site, int start) throws IOException;
  }

  private class SectionSpec {
    private final SectionType type;
    private final int resultCount;
    private final Double shuffle; // nullable

    private SectionSpec(Map<String, Object> configuration) {
      type = SectionType.forCaseInsensitiveName((String) configuration.get("name"));
      resultCount = ((Number) configuration.get("resultCount")).intValue();
      Preconditions.checkArgument(resultCount > 0);

      Number shuffleThreshold = (Number) configuration.get("shuffle");
      this.shuffle = (shuffleThreshold == null) ? null : shuffleThreshold.doubleValue();
    }

    public String getName() {
      return type.name().toLowerCase();
    }

    public List<Object> getArticles(Site site, int start) throws IOException {
      if (shuffle != null) {
        if (type == SectionType.RECENT) {
          return recentArticleService.getRecentArticles(site, resultCount, shuffle);
        } else {
          throw new IllegalArgumentException("Shuffling is supported only on RECENT section"); // No plans to support
        }
      } else {
        return type.getArticles(HomeController.this, this, site, start);
      }
    }
  }

  private static int parseNumberParameter(String param, int minValue) {
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

  private List<SectionSpec> parseSectionSpecs(List<Map<String, Object>> sectionSpecs) {
    List<SectionSpec> sections = new ArrayList<>(sectionSpecs.size());
    for (Map<String, Object> sectionSpec : sectionSpecs) {
      sections.add(new SectionSpec(sectionSpec));
    }
    return sections;
  }

  private static List<String> getSupportedSectionNames(List<SectionSpec> supportedSections) {
    List<String> supportedSectionNames = new ArrayList<>(supportedSections.size());
    for (SectionSpec sectionSpec : supportedSections) {
      supportedSectionNames.add(sectionSpec.getName());
    }
    return supportedSectionNames;
  }

  private static SectionSpec findQueriedSection(Collection<SectionSpec> sectionSpecs,
                                                String sectionParam, String defaultSection) {
    if (sectionParam == null) {
      sectionParam = defaultSection;
    }
    for (SectionSpec sectionSpec : sectionSpecs) {
      if (sectionSpec.type.name().equalsIgnoreCase(sectionParam)) {
        return sectionSpec;
      }
    }

    if (sectionParam.equalsIgnoreCase(defaultSection)) {
      throw new IllegalArgumentException("Default not found in specs");
    } else {
      // Iterate again and return the default
      return findQueriedSection(sectionSpecs, defaultSection, defaultSection);
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

    List<SectionSpec> sectionSpecs = parseSectionSpecs((List<Map<String, Object>>) homepageConfig.get("sections"));
    model.addAttribute("supportedSections", getSupportedSectionNames(sectionSpecs));
    String defaultSection = (String) homepageConfig.get("defaultSelection");

    Collection<SectionSpec> sectionsToRender;
    int start;
    if (defaultSection == null) {
      sectionsToRender = sectionSpecs; // Use all sections
      start = 0;
    } else {
      SectionSpec selectedSection = findQueriedSection(sectionSpecs, sectionParam, defaultSection);
      model.addAttribute("selectedSection", selectedSection.getName());
      sectionsToRender = ImmutableList.of(selectedSection);

      model.addAttribute("resultsPerPage", selectedSection.resultCount);
      int pageSelection = parseNumberParameter(pageParam, 1);
      start = (pageSelection - 1) * selectedSection.resultCount;
    }


    Map<String, Object> sectionsForModel = Maps.newHashMapWithExpectedSize(sectionsToRender.size());
    for (SectionSpec section : sectionsToRender) {
      try {
        List<Object> articles = section.getArticles(site, start);
        sectionsForModel.put(section.getName(), articles);
      } catch (IOException e) {
        log.error("Could not populate home page section: " + section.getName(), e);
        // Render the rest of the page without the article list
        // The FreeMarker template should provide an error message if there is a null value in sectionsForModel
      }
    }

    if ((Boolean) homepageConfig.get("showsIssue")) {
      String issueAddress = "journals/" + site.getJournalKey() + "?currentIssue";
      Map<String, Object> currentIssue = soaService.requestObject(issueAddress, Map.class);
      model.addAttribute("currentIssue", currentIssue);
      Map<String, Object> issueImageMetadata = soaService.requestObject("articles/" + currentIssue.get("imageUri"), Map.class);
      model.addAttribute("issueImage", issueImageMetadata);
    }

    model.addAttribute("sections", sectionsForModel);
    return site.getKey() + "/ftl/home/home";
  }

  private static List<?> getInTheNewsArticles(SoaService soaService, String journalKey) throws IOException {
    String requestAddress = "journals/" + journalKey + "?inTheNewsArticles";
    List<Map<String, Object>> inTheNewsArticles = (List<Map<String, Object>>)
        soaService.requestObject(requestAddress, List.class);

    return inTheNewsArticles;
  }

}
