package org.ambraproject.wombat.controller;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.feed.ArticleFeedView;
import org.ambraproject.wombat.feed.CommentFeedView;
import org.ambraproject.wombat.feed.FeedMetadataField;
import org.ambraproject.wombat.feed.FeedType;
import org.ambraproject.wombat.service.ApiAddress;
import org.ambraproject.wombat.service.CommentService;
import org.ambraproject.wombat.service.RecentArticleService;
import org.ambraproject.wombat.service.SolrArticleAdapter;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.ambraproject.wombat.service.remote.ArticleSearchQuery;
import org.ambraproject.wombat.service.remote.SolrSearchApi;
import org.ambraproject.wombat.service.remote.SolrSearchApiImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles requests for a site home page.
 */
@Controller
public class HomeController extends WombatController {
  private static final Logger log = LoggerFactory.getLogger(HomeController.class);

  @Autowired
  private SolrSearchApi solrSearchApi;
  @Autowired
  private ArticleApi articleApi;
  @Autowired
  private RecentArticleService recentArticleService;
  @Autowired
  private ArticleFeedView articleFeedView;
  @Autowired
  private CommentFeedView commentFeedView;
  @Autowired
  private CommentService commentService;

  /**
   * Enumerates the allowed values for the section parameter for this page.
   */
  private static enum SectionType {
    RECENT {
      @Override
      public List<SolrArticleAdapter> getArticles(HomeController context, SectionSpec section, Site site, int start) throws IOException {
        return getArticlesFromSolr(context, section, site, start, SolrSearchApiImpl.SolrSortOrder.DATE_NEWEST_FIRST);
      }
    },
    POPULAR {
      @Override
      public List<SolrArticleAdapter> getArticles(HomeController context, SectionSpec section, Site site, int start) throws IOException {
        return getArticlesFromSolr(context, section, site, start, SolrSearchApiImpl.SolrSortOrder.MOST_VIEWS_30_DAYS);
      }
    },
    CURATED {
      @Override
      public List<SolrArticleAdapter> getArticles(HomeController context, SectionSpec section, Site site, int start) throws IOException {
        String journalKey = site.getJournalKey();
        Map<String, Object> curatedList = context.articleApi.requestObject(
            ApiAddress.builder("lists").addToken(section.curatedListType)
                .addToken("journals").addToken(journalKey)
                .addToken("keys").addToken(section.curatedListName)
                .build(),
            Map.class);
        List<Map<String,Object>> articles = (List<Map<String, Object>>) curatedList.get("articles");

        List<String> dois = articles.stream()
            .map(article -> (String) article.get("doi"))
            .collect(Collectors.toList());

        Map<String, Object> results = (Map<String, Object>) context.solrSearchApi.lookupArticlesByDois(dois);
        List<SolrArticleAdapter> unpacked = SolrArticleAdapter.unpackSolrQuery(results);
        return Ordering.explicit(dois).onResultOf(SolrArticleAdapter::getDoi).sortedCopy(unpacked);
      }
    };

    private static List<SolrArticleAdapter> getArticlesFromSolr(HomeController context, SectionSpec section, Site site, int start,
                                                                SolrSearchApiImpl.SolrSortOrder order)
        throws IOException {
      ArticleSearchQuery.Builder query = ArticleSearchQuery.builder()
          .setStart(start)
          .setRows(section.resultCount)
          .setSortOrder(order)
          .setJournalKeys(ImmutableList.of(site.getJournalKey()))
          .setDateRange(SolrSearchApiImpl.SolrEnumeratedDateRange.ALL_TIME);
      Map<String, Object> result = (Map<String, Object>) context.solrSearchApi.search(query.build());
      return SolrArticleAdapter.unpackSolrQuery(result);
    }

    /**
     * @throws java.lang.IllegalArgumentException if name is not matched
     */
    private static SectionType forCaseInsensitiveName(String name) {
      return SectionType.valueOf(name.toUpperCase());
    }

    public abstract List<SolrArticleAdapter> getArticles(HomeController context, SectionSpec section, Site site, int start) throws IOException;
  }

  private class SectionSpec {
    private final SectionType type;
    private final int resultCount;
    private final Double since; // nullable
    private final boolean shuffle;
    private final List<String> articleTypes;
    private final List<String> articleTypesToExclude;
    private final String curatedListName;
    private final String curatedListType;
    private final Integer cacheTtl; // nullable

    private SectionSpec(Map<String, Object> configuration) {
      type = SectionType.forCaseInsensitiveName((String) configuration.get("name"));
      resultCount = ((Number) configuration.get("resultCount")).intValue();
      Preconditions.checkArgument(resultCount > 0);

      Number shuffleThreshold = (Number) configuration.get("since");
      this.since = (shuffleThreshold == null) ? null : shuffleThreshold.doubleValue();

      Boolean shuffle = (Boolean) configuration.get("shuffle");
      this.shuffle = (shuffle != null) && shuffle;

      this.articleTypes = (List<String>) configuration.get("articleTypes");
      this.articleTypesToExclude = (List<String>) configuration.get("articleTypesToExclude");

      this.curatedListName = (String) configuration.get("curatedListName");
      Preconditions.checkArgument((curatedListName != null) == (type == SectionType.CURATED));

      this.curatedListType = (String) configuration.get("curatedListType");
      Preconditions.checkArgument((curatedListType != null) == (type == SectionType.CURATED));

      Number cacheTtl = (Number) configuration.get("cacheTtl");
      this.cacheTtl = (cacheTtl == null) ? null : cacheTtl.intValue();
    }

    public String getName() {
      return (type == SectionType.CURATED) ? curatedListName : type.name().toLowerCase();
    }

    public List<SolrArticleAdapter> getArticles(Site site, int start) throws IOException {
      if (since != null) {
        if (type == SectionType.RECENT) {
          return recentArticleService.getRecentArticles(site, resultCount, since, shuffle,
              articleTypes, articleTypesToExclude, Optional.fromNullable(cacheTtl));
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
      if (sectionSpec.getName().equalsIgnoreCase(sectionParam)) {
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

  @RequestMapping(name = "homePage", value = "", method = RequestMethod.GET)
  public String serveHomepage(HttpServletRequest request, Model model, @SiteParam Site site,
                              @RequestParam(value = "section", required = false) String sectionParam,
                              @RequestParam(value = "page", required = false) String pageParam)
      throws IOException {
    if (!request.getServletPath().endsWith("/")) {
      return "redirect:" + request.getServletPath() + "/";
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
        List<SolrArticleAdapter> articles = section.getArticles(site, start);
        sectionsForModel.put(section.getName(), articles);
      } catch (IOException e) {
        log.error("Could not populate home page section: " + section.getName(), e);
        // Render the rest of the page without the article list
        // The FreeMarker template should provide an error message if there is a null value in sectionsForModel
      }
    }

    if ((Boolean) homepageConfig.get("showsIssue")) {
      try {
        populateCurrentIssue(model, site);
      } catch (IOException e) {
        log.error("Could not retrieve current issue for: " + site.getJournalKey(), e);
      }
    }

    model.addAttribute("sections", sectionsForModel);
    model.addAttribute("parameterMap", request.getParameterMap()); // needed for paging
    return site.getKey() + "/ftl/home/home";
  }

  private void populateCurrentIssue(Model model, Site site) throws IOException {
    ApiAddress journalAddress = ApiAddress.builder("journals").addToken(site.getJournalKey()).build();
    Map<String, Object> journal = articleApi.requestObject(journalAddress, Map.class);
    Map<String, Object> currentIssue = (Map<String, Object>) journal.get("currentIssue");
    if (currentIssue == null) {
      throw new RuntimeException("Current issue is not set for " + site.getJournalKey());
    }
    model.addAttribute("currentIssue", currentIssue);
  }

  /**
   * Serves recent journal articles as XML to be read by an RSS reader
   *
   * @param site    site the request originates from
   * @return RSS view of recent articles for the specified site
   * @throws IOException
   */
  @RequestMapping(name ="homepageFeed", value="/feed/{feedType:atom|rss}", method = RequestMethod.GET)
  public ModelAndView getRssFeedView(@SiteParam Site site, @PathVariable String feedType)
      throws IOException {

    ArticleSearchQuery.Builder query = ArticleSearchQuery.builder()
        .setStart(0)
        .setRows(getFeedLength(site))
        .setSortOrder(SolrSearchApiImpl.SolrSortOrder.DATE_NEWEST_FIRST)
        .setJournalKeys(ImmutableList.of(site.getJournalKey()))
        .setDateRange(SolrSearchApiImpl.SolrEnumeratedDateRange.ALL_TIME)
        .setIsRssSearch(true);
    Map<String, ?> recentArticles = solrSearchApi.search(query.build());

    ModelAndView mav = new ModelAndView();
    FeedMetadataField.SITE.putInto(mav, site);
    FeedMetadataField.FEED_INPUT.putInto(mav, recentArticles.get("docs"));
    mav.setView(FeedType.getView(articleFeedView, feedType));
    return mav;
  }

  @RequestMapping(name = "commentFeed", value = "/feed/comments/{feedType:atom|rss}", method = RequestMethod.GET)
  public ModelAndView getCommentFeed(@SiteParam Site site, @PathVariable String feedType)
      throws IOException {
    List<Map<String, Object>> comments = commentService.getRecentJournalComments(site.getJournalKey(), getFeedLength(site));

    ModelAndView mav = new ModelAndView();
    FeedMetadataField.SITE.putInto(mav, site);
    FeedMetadataField.TITLE.putInto(mav, "Comments");
    FeedMetadataField.FEED_INPUT.putInto(mav, comments);
    mav.setView(FeedType.getView(commentFeedView, feedType));
    return mav;
  }

}
