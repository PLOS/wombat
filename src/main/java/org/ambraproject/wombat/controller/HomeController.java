package org.ambraproject.wombat.controller;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
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

  /**
   * Enumerates the allowed values for the section parameter for this page.
   */
  private static enum SectionType {
    RECENT {
      @Override
      public List<Object> getArticles(HomeController context, SectionSpec section, Site site, int start) throws IOException {
        Map<?, ?> result = context.solrSearchService.getHomePageArticles(site, start, section.resultCount,
            SolrSearchService.SolrSortOrder.DATE_NEWEST_FIRST);
        return (List<Object>) result.get("docs");
      }
    },
    POPULAR {
      @Override
      public List<Object> getArticles(HomeController context, SectionSpec section, Site site, int start) throws IOException {
        Map<?, ?> result = context.solrSearchService.getHomePageArticles(site, start, section.resultCount,
            SolrSearchService.SolrSortOrder.MOST_VIEWS_30_DAYS);
        return (List<Object>) result.get("docs");
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

  private static class SectionSpec {
    private final SectionType type;
    private final int resultCount;
    private final List<Number> shuffleSeq;

    private SectionSpec(Map<String, Object> configuration) {
      type = SectionType.forCaseInsensitiveName((String) configuration.get("name"));
      resultCount = ((Number) configuration.get("resultCount")).intValue();
      shuffleSeq = (List<Number>) configuration.get("shuffle"); // may be null

      Preconditions.checkArgument(resultCount > 0);
    }

    public String getName() {
      return type.name().toLowerCase();
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

  private static List<SectionSpec> parseSectionSpecs(List<Map<String, Object>> sectionSpecs) {
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
      List<Object> articles = section.type.getArticles(this, section, site, start);
      sectionsForModel.put(section.getName(), articles);
    }

    model.addAttribute("sections", sectionsForModel);
    return site.getKey() + "/ftl/home/home";
  }

  private static List<?> getInTheNewsArticles(SoaService soaService, String journalKey) throws IOException {
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
    return inTheNewsArticles;
  }

}
