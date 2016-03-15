/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2013 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.wombat.controller;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.feed.ArticleFeedView;
import org.ambraproject.wombat.feed.FeedMetadataField;
import org.ambraproject.wombat.feed.FeedType;
import org.ambraproject.wombat.model.JournalFilterType;
import org.ambraproject.wombat.model.SearchFilter;
import org.ambraproject.wombat.model.SearchFilterItem;
import org.ambraproject.wombat.model.SingletonSearchFilterType;
import org.ambraproject.wombat.model.TaxonomyGraph;
import org.ambraproject.wombat.service.BrowseTaxonomyService;
import org.ambraproject.wombat.service.SolrArticleAdapter;
import org.ambraproject.wombat.service.remote.ArticleSearchQuery;
import org.ambraproject.wombat.service.remote.SearchFilterService;
import org.ambraproject.wombat.service.remote.ServiceRequestException;
import org.ambraproject.wombat.service.remote.SolrSearchService;
import org.ambraproject.wombat.service.remote.SolrSearchServiceImpl;
import org.ambraproject.wombat.util.ListUtil;
import org.ambraproject.wombat.util.UrlParamBuilder;
import org.apache.commons.lang.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Controller class for user-initiated searches.
 */
@Controller
public class SearchController extends WombatController {
  private static final Logger log = LoggerFactory.getLogger(SearchController.class);

  @Autowired
  private SiteSet siteSet;

  @Autowired
  private SolrSearchService solrSearchService;

  @Autowired
  private SearchFilterService searchFilterService;

  @Autowired
  private BrowseTaxonomyService browseTaxonomyService;

  @Autowired
  private ArticleFeedView articleFeedView;

  private final String BROWSE_RESULTS_PER_PAGE = "13";

  /**
   * Class that encapsulates the parameters that are shared across many different search types. For example, a subject
   * search and an advanced search will have many parameters in common, such as sort order, date range, page, results
   * per page, etc.  This class eliminates the need to have long lists of @RequestParam parameters duplicated across
   * many controller methods.
   * <p>
   * This class also contains logic having to do with which parameters take precedence over others, defaults when
   * parameters are absent, and the like.
   */
  @VisibleForTesting
  static final class CommonParams {

    private enum AdvancedSearchTerms {
      EVERYTHING("everything:"),
      TITLE("title:"),
      AUTHOR("author:"),
      BODY("body:"),
      ABSTRACT("abstract:"),
      SUBJECT("subject:"),
      PUBLICATION_DATE("publication_date:"),
      ACCEPTED_DATE("accepted_date:"),
      ID("id:"),
      ARTICLE_TYPE("article_type:"),
      AUTHOR_AFFILIATE("author_affiliate:"),
      COMPETING_INTEREST("competing_interest:"),
      CONCLUSIONS("conclusions:"),
      EDITOR("editor:"),
      ELOCATION_ID("elocation_id:"),
      FIGURE_TABLE_CAPTION("figure_table_caption:"),
      FINANCIAL_DISCLOSURE("financial_disclosure:"),
      INTRODUCTION("introduction:"),
      ISSUE("issue:"),
      MATERIALS_AND_METHODS("materials_and_methods:"),
      RECEIVED_DATE("received_date:"),
      REFERENCE("reference:"),
      RESULTS_AND_DISCUSSION("results_and_discussion:"),
      SUPPORTING_INFORMATION("supporting_information:"),
      TRIAL_REGISTRATION("trial_registration:"),
      VOLUME("volume:");

      private final String text;

      private AdvancedSearchTerms(final String text) {
        this.text = text;
      }

      @Override
      public String toString() {
        return text;
      }
    }

    /**
     * The number of the first desired result (zero-based) that will be passed to solr. Calculated from the page and
     * resultsPerPage URL parameters.
     */
    int start;

    SolrSearchServiceImpl.SolrSortOrder sortOrder;

    SolrSearchService.SearchCriterion dateRange;

    List<String> articleTypes;

    List<String> journalKeys;

    @VisibleForTesting
    Set<String> filterJournalNames;

    @VisibleForTesting
    List<String> subjectList;

    List<String> authors;

    List<String> sections;

    /**
     * Indicates whether any filter parameters are being applied to the search (journal, subject area, etc).
     */
    @VisibleForTesting
    boolean isFiltered;

    private SiteSet siteSet;

    private Site site;

    private int resultsPerPage;

    private String startDate;

    private String endDate;

    private final String DEFAULT_START_DATE = "2003-01-01";

    // doesn't include journal and date filter param names
    static final Set<String> FILTER_PARAMETER_NAMES = Stream.of(SingletonSearchFilterType.values()).map
        (SingletonSearchFilterType::getParameterName).collect(Collectors.toSet());

    /**
     * Constructor.
     *
     * @param siteSet siteSet associated with the request
     * @param site    site of the request
     */
    CommonParams(SiteSet siteSet, Site site) {
      this.siteSet = siteSet;
      this.site = site;
    }

    /**
     * Extracts parameters from the raw parameter map, and performs some logic related to what parameters take
     * precedence and default values when ones aren't present.
     *
     * @param params
     * @throws IOException
     */
    void parseParams(Map<String, List<String>> params) throws IOException {
      String pageParam = getSingleParam(params, "page", null);
      resultsPerPage = Integer.parseInt(getSingleParam(params, "resultsPerPage", "15"));
      if (pageParam != null) {
        int page = Integer.parseInt(pageParam);
        start = (page - 1) * resultsPerPage;
      }
      sortOrder = SolrSearchServiceImpl.SolrSortOrder.RELEVANCE;
      String sortOrderParam = getSingleParam(params, "sortOrder", null);
      if (!Strings.isNullOrEmpty(sortOrderParam)) {
        sortOrder = SolrSearchServiceImpl.SolrSortOrder.valueOf(sortOrderParam);
      }
      dateRange = parseDateRange(getSingleParam(params, "dateRange", null),
          getSingleParam(params, "filterStartDate", null), getSingleParam(params, "filterEndDate", null));
      journalKeys = ListUtil.isNullOrEmpty(params.get("filterJournals"))
          ? new ArrayList<String>() : params.get("filterJournals");

      filterJournalNames = new HashSet<>();
      for (String journalKey : journalKeys) {
        filterJournalNames.add(siteSet.getJournalNameFromKey(journalKey));
      }
      startDate = getSingleParam(params, "filterStartDate", null);
      endDate = getSingleParam(params, "filterEndDate", null);

      if (startDate == null && endDate != null) {
        startDate = DEFAULT_START_DATE;
      } else if (startDate != null && endDate == null) {
        endDate = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
      }

      subjectList = parseSubjects(getSingleParam(params, "subject", null), params.get("filterSubjects"));
      articleTypes = params.get("filterArticleTypes");
      articleTypes = articleTypes == null ? new ArrayList<String>() : articleTypes;
      authors = ListUtil.isNullOrEmpty(params.get("filterAuthors"))
          ? new ArrayList<String>() : params.get("filterAuthors");
      sections = ListUtil.isNullOrEmpty(params.get("filterSections"))
          ? new ArrayList<String>() : params.get("filterSections");

      isFiltered = !filterJournalNames.isEmpty() || !subjectList.isEmpty() || !articleTypes.isEmpty()
          || dateRange != SolrSearchServiceImpl.SolrEnumeratedDateRange.ALL_TIME || !authors.isEmpty()
          || startDate != null || endDate != null || !sections.isEmpty();
    }

    /**
     * Adds parameters (and derived values) back to the model needed for results page rendering. This only adds model
     * attributes that are shared amongst different types of searches; it is the caller's responsibility to add the
     * search results and any other data needed.
     *
     * @param model   model that will be passed to the template
     * @param request HttpServletRequest
     */
    void addToModel(Model model, HttpServletRequest request) {
      model.addAttribute("resultsPerPage", resultsPerPage);
      model.addAttribute("filterJournalNames", filterJournalNames);

      // TODO: split or share model assignments between mobile and desktop.
      model.addAttribute("filterJournals", journalKeys);
      model.addAttribute("filterStartDate", startDate);
      model.addAttribute("filterEndDate", endDate);
      model.addAttribute("filterSubjects", subjectList);
      model.addAttribute("filterArticleTypes", articleTypes);
      model.addAttribute("filterAuthors", authors);
      model.addAttribute("filterSections", sections);

      // TODO: bind sticky form params using Spring MVC support for Freemarker.  I think we have to add
      // some more dependencies to do this.  See
      // http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/view.html#view-velocity
      model.addAttribute("selectedSortOrder", sortOrder);
      model.addAttribute("selectedDateRange", dateRange);
      model.addAttribute("selectedResultsPerPage", resultsPerPage);
      model.addAttribute("isFiltered", isFiltered);

      // We pass in the request parameters here, because they are needed by paging.ftl.
      // The normal way to get request parameters from a freemarker template is to use the
      // RequestParameters variable, but due to a bug in freemarker, this does not handle
      // multi-valued parameters correctly.  See http://sourceforge.net/p/freemarker/bugs/324/
      Map<String, String[]> parameterMap = request.getParameterMap();
      model.addAttribute("parameterMap", parameterMap);

      Map<String, String[]> clearDateFilterParams = new HashMap<>();
      clearDateFilterParams.putAll(parameterMap);
      clearDateFilterParams.remove("filterStartDate");
      clearDateFilterParams.remove("filterEndDate");
      model.addAttribute("dateClearParams", clearDateFilterParams);

      Map<String, String[]> clearAllFilterParams = new HashMap<>();
      clearAllFilterParams.putAll(clearDateFilterParams);
      clearAllFilterParams.remove("filterJournals");
      clearAllFilterParams.remove("filterSubjects");
      clearAllFilterParams.remove("filterAuthors");
      clearAllFilterParams.remove("filterSections");
      clearAllFilterParams.remove("filterArticleTypes");
      model.addAttribute("clearAllFilterParams", clearAllFilterParams);

    }

    private String getSingleParam(Map<String, List<String>> params, String key, String defaultValue) {
      List<String> values = params.get(key);
      return values == null || values.isEmpty() ? defaultValue
          : values.get(0) == null || values.get(0).isEmpty() ? defaultValue : values.get(0);
    }

    /**
     * Determines which publication dates to filter by in the search. If no dates are input, a default date range of All
     * Time will be used. Mobile search only provides the enumerated dateRangeParam field, while desktop search provides
     * explicit fields for start and end dates. The parameters are mutually exclusive.
     *
     * @param dateRangeParam mobile date range enumeration value
     * @param startDate      desktop start date value
     * @param endDate        desktop end date value
     * @return A generic @SearchCriterion object used by Solr
     */
    private SolrSearchService.SearchCriterion parseDateRange(String dateRangeParam, String startDate, String endDate) {
      SolrSearchService.SearchCriterion dateRange = SolrSearchServiceImpl.SolrEnumeratedDateRange.ALL_TIME;
      if (!Strings.isNullOrEmpty(dateRangeParam)) {
        dateRange = SolrSearchServiceImpl.SolrEnumeratedDateRange.valueOf(dateRangeParam);
      } else if (!Strings.isNullOrEmpty(startDate) && !Strings.isNullOrEmpty(endDate)) {
        dateRange = new SolrSearchServiceImpl.SolrExplicitDateRange("explicit date range", startDate,
            endDate);
      }
      return dateRange;
    }

    /**
     * subject is a mobile-only parameter, while subjects is a desktop-only parameter
     *
     * @param subject  mobile subject area value
     * @param subjects desktop list of subject area values
     * @return singleton list of subject if subjects is null or empty, else return subjects
     */
    private List<String> parseSubjects(String subject, List<String> subjects) {
      if (Strings.isNullOrEmpty(subject) && subjects != null && subjects.size() > 0) {
        return subjects;
      } else {
        return subject != null ? Collections.singletonList(subject) : new ArrayList<String>();
      }
    }

    private ArticleSearchQuery.Builder fill(ArticleSearchQuery.Builder builder) {
      return builder
          .setJournalKeys(journalKeys)
          .setArticleTypes(articleTypes)
          .setSubjects(subjectList)
          .setAuthors(authors)
          .setSections(sections)
          .setStart(start)
          .setRows(resultsPerPage)
          .setSortOrder(sortOrder)
          .setDateRange(dateRange)
          .setStartDate(startDate)
          .setEndDate(endDate);
    }

    private static final ImmutableMap<String, Function<CommonParams, List<String>>> FILTER_KEYS_TO_FIELDS =
        ImmutableMap.<String, Function<CommonParams, List<String>>>builder()
            .put(JournalFilterType.JOURNAL_FILTER_MAP_KEY, params -> params.journalKeys)
            .put(SingletonSearchFilterType.ARTICLE_TYPE.getFilterMapKey(), params -> params.articleTypes)
            .put(SingletonSearchFilterType.SUBJECT_AREA.getFilterMapKey(), params -> params.subjectList)
            .put(SingletonSearchFilterType.AUTHOR.getFilterMapKey(), params -> params.authors)
            .put(SingletonSearchFilterType.SECTION.getFilterMapKey(), params -> params.sections)
            .build();

    /**
     * Examine incoming URL parameters to see which filter items are active. CommonParams contains
     * journalKeys, articleTypes, subjectList, authors, and sections parsed from request params.
     * Check each string in these lists against their applicable filters.
     *
     * @param filter the search filter to examine
     */
    public void setActiveAndInactiveFilterItems(SearchFilter filter) {
      String filterMapKey = filter.getFilterTypeMapKey();
      Function<CommonParams, List<String>> getter = FILTER_KEYS_TO_FIELDS.get(filterMapKey);
      if (getter == null) {
        throw new RuntimeException("Search Filter not configured with sane map key: " + filterMapKey);
      }
      filter.setActiveAndInactiveFilterItems(getter.apply(this));
    }

    /**
     *  Creates an instance of {SearchFilterItem} for active filters using url parameters
     *
     * @param activeFilterItems set of active filter items
     * @param parameterMap request's query parameter
     * @param filterName name of the filter
     * @param filterValues values of the filter
     */
    private void buildActiveFilterItems(Set<SearchFilterItem> activeFilterItems, Map<String,
        String[]> parameterMap, String filterName, String[] filterValues) {

      for (String filterValue : filterValues) {
        List<String> filterValueList = new ArrayList<>(Arrays.asList(filterValues));
        Map<String, List<String>> queryParamMap = new HashMap<>();
        // covert Map<String, String[]> to Map<String, List<String> for code re-usability
        queryParamMap.putAll(parameterMap.entrySet().stream().collect(Collectors.toMap(entry -> entry
            .getKey(), entry -> new ArrayList<>(Arrays.asList(entry.getValue())))));
        queryParamMap.remove(filterName);
        // include the rest of filter values for that specific filter
        if (filterValueList.size() > 1) {
          filterValueList.remove(filterValue);
          queryParamMap.put(filterName, filterValueList);
        }
        String displayName;
        if (filterName.equals("filterJournals")) {
          displayName = siteSet.getJournalNameFromKey(filterValue);
        } else {
          displayName = filterValue;
        }
        SearchFilterItem filterItem = new SearchFilterItem(displayName, 0,
            filterName, filterValue, queryParamMap);
        activeFilterItems.add(filterItem);
      }
    }
    /**
     * Examine the incoming URL when there is no search result and set the active filters
     *
     * @return set of active filters
     */
    public Set<SearchFilterItem> setActiveFilterParams(Model model, HttpServletRequest request) {
      Map<String, String[]> parameterMap = request.getParameterMap();
      model.addAttribute("parameterMap", parameterMap);


      // exclude non-filter query parameters
      Map<String, String[]> filtersOnlyMap = parameterMap.entrySet().stream()
          .filter(entry -> FILTER_PARAMETER_NAMES.contains(entry.getKey())
              || ("filterJournals").equals(entry.getKey()))
          .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));

      Set<SearchFilterItem> activeFilterItems = new LinkedHashSet<>();
      filtersOnlyMap.forEach((filterName, filterValues) -> buildActiveFilterItems(activeFilterItems,
          parameterMap, filterName, filterValues));
      return activeFilterItems;
    }

    /**
     * @param query the incoming query string
     * @return True if the query string does not contain any advanced search terms,
     * listed in {@link AdvancedSearchTerms}
     */
    private boolean isSimpleSearch(String query) {
      return Arrays.stream(AdvancedSearchTerms.values()).noneMatch(e -> query.contains(e.text));
    }
  }

  /**
   * Examine the current @code{ArticleSearchQuery} object and build a single URL parameter
   * string to append to the current search URL.
   *
   * @param q the search query to rebuild search URL parameters from
   * @return ImmutableListMultimap that contains the URL parameter list
   */
  private static ImmutableListMultimap<String, String> rebuildUrlParameters(ArticleSearchQuery q) {
    Preconditions.checkArgument(!q.isForRawResults());
    Preconditions.checkArgument(!q.getFacet().isPresent());

    ImmutableListMultimap.Builder<String, String> builder = ImmutableListMultimap.builder();
    builder.put(q.isSimple() ? "q" : "unformattedQuery", q.getQuery().or(""));

    int rows = q.getRows();
    builder.put("resultsPerPage", Integer.toString(rows));
    if (rows > 0) {
      int page = q.getStart() / rows + 1;
      builder.put("page", Integer.toString(page));
    }

    builder.putAll("filterJournals", q.getJournalKeys());
    builder.putAll("filterSubjects", q.getSubjects());
    builder.putAll("filterAuthors", q.getAuthors());
    builder.putAll("filterSections", q.getSections());
    builder.putAll("filterArticleTypes", q.getArticleTypes());
    builder.putAll("filterStartDate", q.getStartDate() == null ? "" : q.getStartDate());
    builder.putAll("filterEndDate", q.getEndDate() == null ? "" : q.getEndDate());

    // TODO: Support dateRange. Note this is different from startDate and endDate
    // TODO: Support sortOrder

    for (Map.Entry<String, String> entry : q.getRawParameters().entrySet()) {
      builder.put(entry);
    }

    return builder.build();
  }

  private CommonParams modelCommonParams(HttpServletRequest request, Model model,
      @SiteParam Site site, @RequestParam MultiValueMap<String, String> params) throws IOException {
    CommonParams commonParams = new CommonParams(siteSet, site);
    commonParams.parseParams(params);
    commonParams.addToModel(model, request);
    model.addAttribute("sortOrders", SolrSearchServiceImpl.SolrSortOrder.values());
    model.addAttribute("dateRanges", SolrSearchServiceImpl.SolrEnumeratedDateRange.values());
    return commonParams;
  }


  /**
   * Performs a simple search and serves the result as XML to be read by an RSS reader
   *
   * @param request HttpServletRequest
   * @param model   model that will be passed to the template
   * @param site    site the request originates from
   * @param params  search parameters identical to the {@code search} method
   * @return RSS view of articles returned by the search
   * @throws IOException
   */
  @RequestMapping(name = "searchFeed", value = "/search/feed/{feedType:atom|rss}",
      params = {"q"}, method = RequestMethod.GET)
  public ModelAndView getSearchRssFeedView(HttpServletRequest request, Model model, @SiteParam Site site,
      @PathVariable String feedType, @RequestParam MultiValueMap<String, String> params) throws IOException {
    CommonParams commonParams = modelCommonParams(request, model, site, params);

    String queryString = params.getFirst("q");
    ArticleSearchQuery.Builder query = ArticleSearchQuery.builder()
        .setQuery(queryString)
        .setSimple(commonParams.isSimpleSearch(queryString))
        .setIsRssSearch(true);
    commonParams.fill(query);
    ArticleSearchQuery queryObj = query.build();

    Map<String, ?> searchResults = solrSearchService.search(queryObj);

    String feedTitle = representQueryParametersAsString(params);
    return getFeedModelAndView(site, feedType, feedTitle, searchResults);
  }

  /**
   * Performs an advanced search and serves the result as XML to be read by an RSS reader
   *
   * @param request HttpServletRequest
   * @param model   model that will be passed to the template
   * @param site    site the request originates from
   * @param params  search parameters identical to the {@code search} method
   * @return RSS view of articles returned by the search
   * @throws IOException
   */
  @RequestMapping(name = "advancedSearchFeed", value = "/search/feed/{feedType:atom|rss}",
      params = {"unformattedQuery"}, method = RequestMethod.GET)
  public ModelAndView getAdvancedSearchRssFeedView(HttpServletRequest request, Model model, @SiteParam Site site,
      @PathVariable String feedType, @RequestParam MultiValueMap<String, String> params) throws IOException {
    String queryString = params.getFirst("unformattedQuery");
    params.remove("unformattedQuery");
    params.add("q", queryString);
    return getSearchRssFeedView(request, model, site, feedType, params);
  }

  private static String representQueryParametersAsString(MultiValueMap<String, String> params) {
    UrlParamBuilder builder = UrlParamBuilder.params();
    for (Map.Entry<String, List<String>> entry : params.entrySet()) {
      String key = entry.getKey();
      for (String value : entry.getValue()) {
        builder.add(key, value);
      }
    }
    return builder.toString();
  }

  private ModelAndView getFeedModelAndView(Site site, String feedType, String title, Map<String, ?> searchResults) {
    ModelAndView mav = new ModelAndView();
    FeedMetadataField.SITE.putInto(mav, site);
    FeedMetadataField.FEED_INPUT.putInto(mav, searchResults.get("docs"));
    FeedMetadataField.TITLE.putInto(mav, title);
    mav.setView(FeedType.getView(articleFeedView, feedType));
    return mav;
  }

  // Unless the "!volume" part is included in the params in the next few methods, you will
  // get an "ambiguous handler method" exception from spring.  I think this is because all
  // of these methods (including volumeSearch) use a MultiValueMap for @RequestParam, instead
  // of individually listing the params.

  /**
   * Performs a "simple" or "advanced" search. The query parameter is read, and if advanced search
   * terms are found, an advanced search is performed. Otherwise, a simple search is performed. The
   * only difference between simple and advanced searches is the use of dismax in the ultimate
   * Solr query.
   *
   * @param request HttpServletRequest
   * @param model   model that will be passed to the template
   * @param site    site the request originates from
   * @param params  all URL parameters
   * @return String indicating template location
   * @throws IOException
   */
  @RequestMapping(name = "simpleSearch", value = "/search", params = {"q"})
  public String search(HttpServletRequest request, Model model, @SiteParam Site site,
      @RequestParam MultiValueMap<String, String> params) throws IOException {
    CommonParams commonParams = modelCommonParams(request, model, site, params);

    String queryString = params.getFirst("q");
    ArticleSearchQuery.Builder query = ArticleSearchQuery.builder()
        .setQuery(queryString)
        .setSimple(commonParams.isSimpleSearch(queryString));
    commonParams.fill(query);
    ArticleSearchQuery queryObj = query.build();
    Map<?, ?> searchResults;
    try {
      searchResults = solrSearchService.search(queryObj);
    } catch (ServiceRequestException sre) {
      return handleFailedSolrRequest(model, site, queryString, sre);
    }

    model.addAttribute("searchResults", solrSearchService.addArticleLinks(searchResults, request, site, siteSet));

    Set<SearchFilterItem> activeFilterItems;

    if ((Double) searchResults.get("numFound") == 0.0) {
       activeFilterItems = commonParams.setActiveFilterParams(model, request);
    } else {
      Map<String, SearchFilter> filters = searchFilterService.getSearchFilters(queryObj, rebuildUrlParameters(queryObj));
      filters.values().forEach(commonParams::setActiveAndInactiveFilterItems);

      activeFilterItems = new LinkedHashSet<>();
      filters.values().forEach(filter -> activeFilterItems.addAll(filter.getActiveFilterItems()));
      model.addAttribute("searchFilters", filters);
    }

    model.addAttribute("activeFilterItems", activeFilterItems);

    return site.getKey() + "/ftl/search/searchResults";
  }

  private String handleFailedSolrRequest(Model model, Site site, String queryString,
      ServiceRequestException sre) throws IOException {
    if (sre.getResponseBody().contains("SyntaxError: Cannot parse")) {
      log.info("User attempted invalid search: " + queryString + "\n Exception: " + sre.getMessage());
       model.addAttribute("cannotParseQueryError", true);
    } else {
      log.error("Unknown error returned from Solr: " + sre.getMessage());
      model.addAttribute("unknownQueryError", true);
    }
    return newAdvancedSearch(model, site);
  }


  /**
   * This is a catch for advanced searches originating from Old Ambra. It transforms the
   * "unformattedQuery" param into "q" which is used by Wombat's new search.
   * todo: remove this method once Old Ambra advanced search is destroyed
   */
  @RequestMapping(name = "advancedSearch", value = "/search", params = {"unformattedQuery", "!q"})
  public String advancedSearch(HttpServletRequest request, Model model, @SiteParam Site site,
      @RequestParam MultiValueMap<String, String> params) throws IOException {
    String queryString = params.getFirst("unformattedQuery");
    params.remove("unformattedQuery");
    params.add("q", queryString);
    return search(request, model, site, params);
  }

  @RequestMapping(name = "newAdvancedSearch", value = "/search", params = {"!unformattedQuery", "!q"})
  public String newAdvancedSearch(Model model, @SiteParam Site site) throws IOException {
    model.addAttribute("isNewSearch", true);
    model.addAttribute("otherQuery", "");
    model.addAttribute("activeFilterItems", new HashSet<>());
    return site.getKey() + "/ftl/search/searchResults";
  }

  /**
   * Endpoint to render the subject area browser in mobile
   *
   */
  @RequestMapping(name = "mobileSubjectAreaBrowser", value = "/subjectAreaBrowse")
  public String mobileSubjectAreaBrowser(@SiteParam Site site) {
    return site.getKey() + "/ftl/mobileSubjectAreaBrowser";
  }

  /**
   * Serves search result data. Used by the mobile taxonomy browser search results and
   * desktop subject area landing pages. This endpoint returns the articles for all subject areas.
   *
   * @param request HttpServletRequest
   * @param model   model that will be passed to the template
   * @param site    site the request originates from
   * @param params  all URL parameters
   * @return String indicating template location
   * @throws IOException
   */
  @RequestMapping(name = "browse", value = "/browse")
  public String browseAll(HttpServletRequest request, Model model, @SiteParam Site site,
      @RequestParam MultiValueMap<String, String> params) throws IOException {
    subjectAreaSearch(request, model, site, params, "");
    return site.getKey() + "/ftl/browseSubjectArea";
  }

  /**
   * Serves search result data. Used by the mobile taxonomy browser search results and
   * desktop subject area landing pages. This endpoint returns the articles for a single subject area.
   *
   * @param request HttpServletRequest
   * @param model   model that will be passed to the template
   * @param site    site the request originates from
   * @param subject the subject area to be searched
   * @param params  all URL parameters
   * @return String indicating template location
   * @throws IOException
   */
  @RequestMapping(name = "browseSubjectArea", value = "/browse/{subject}")
  public String browseSubjectArea(HttpServletRequest request, Model model, @SiteParam Site site,
      @PathVariable String subject, @RequestParam MultiValueMap<String, String> params)
      throws IOException {
    subjectAreaSearch(request, model, site, params, subject);
    return site.getKey() + "/ftl/browseSubjectArea";
  }

  /**
   * Set defaults and performs search for subject area landing page
   *
   * @param request HTTP request for browsing subject areas
   * @param model model that will be passed to the template
   * @param site site the request originates from
   * @param params HTTP request params
   * @param subject the subject area to be search; return all articles if no subject area is provided
   * @throws IOException
   */
  private void subjectAreaSearch(HttpServletRequest request, Model model, Site site,
      MultiValueMap<String, String> params, String subject) throws IOException {

    modelSubjectHierarchy(model, site, subject);

    String subjectName;
    if (Strings.isNullOrEmpty(subject)) {
      params.add("subject", "");
      subjectName = "All Subject Areas";
    } else {
      subject = subject.replace("_", " ");
      params.add("subject", subject);
      subjectName = WordUtils.capitalize(subject);
    }
    model.addAttribute("subjectName", subjectName);

    // set defaults for subject area landing page
    if (ListUtil.isNullOrEmpty(params.get("resultsPerPage"))) {
      params.add("resultsPerPage", BROWSE_RESULTS_PER_PAGE);
    }

    if (ListUtil.isNullOrEmpty(params.get("sortOrder"))) {
      params.add("sortOrder", "DATE_NEWEST_FIRST");
    }

    if (ListUtil.isNullOrEmpty(params.get("filterJournals"))) {
      params.add("filterJournals", site.getJournalKey());
    }

    CommonParams commonParams = modelCommonParams(request, model, site, params);
    ArticleSearchQuery.Builder query = ArticleSearchQuery.builder()
        .setQuery("")
        .setSimple(false);
    commonParams.fill(query);

    ArticleSearchQuery queryObj = query.build();
    Map<String, ?> searchResults = solrSearchService.search(queryObj);

    model.addAttribute("articles", SolrArticleAdapter.unpackSolrQuery(searchResults));
    model.addAttribute("searchResults", searchResults);
    model.addAttribute("page", commonParams.getSingleParam(params, "page", "1"));
    model.addAttribute("journalKey", site.getKey());
  }

  private void modelSubjectHierarchy(Model model, Site site, String subject) throws IOException {
    TaxonomyGraph fullTaxonomyView = browseTaxonomyService.parseCategories(site.getJournalKey());

    Set<String> subjectParents;
    Set<String> subjectChildren;
    if (subject != null && subject.length() > 0) {
      //Recreate the category name as stored in the DB
      subject = subject.replace("_", " ");

      TaxonomyGraph.CategoryView categoryView = fullTaxonomyView.getView(subject);
      if (categoryView == null) {
        throw new NotFoundException(String.format("category %s does not exist.", subject));
      } else {
        if (categoryView.getParents().isEmpty()) {
          subjectParents = new HashSet<>();
        } else {
          subjectParents = categoryView.getParents().keySet();
        }

        subjectChildren = categoryView.getChildren().keySet();
      }
    } else {
      subjectParents = new HashSet<>();
      subjectChildren = fullTaxonomyView.getRootCategoryNames();
    }

    model.addAttribute("subjectParents", subjectParents);
    model.addAttribute("subjectChildren", subjectChildren);
  }
}
