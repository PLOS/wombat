/*
 * Copyright (c) 2017 Public Library of Science
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package org.ambraproject.wombat.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.model.JournalFilterType;
import org.ambraproject.wombat.model.SearchFilter;
import org.ambraproject.wombat.model.SearchFilterItem;
import org.ambraproject.wombat.model.SingletonSearchFilterType;
import org.ambraproject.wombat.service.UnmatchedSiteException;
import org.ambraproject.wombat.service.remote.ArticleSearchQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ui.Model;

/**
 * Class that encapsulates the parameters that are shared across many different
 * search types. For example, a subject search and an advanced search will have
 * many parameters in common, such as sort order, date range, page, results per
 * page, etc.  This class eliminates the need to have long lists of
 * @RequestParam parameters duplicated across many controller methods. <p> This
 * class also contains logic having to do with which parameters take precedence
 * over others, defaults when parameters are absent, and the like.
 */

public class CommonParams {
  private static final Logger log = LogManager.getLogger(CommonParams.class);

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
    DATA_AVAILABILITY("data_availability:"),
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

  ArticleSearchQuery.SolrSortOrder sortOrder;

  ArticleSearchQuery.SearchCriterion dateRange;

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

  private LocalDate startDate;

  private LocalDate endDate;

  private static final LocalDate DEFAULT_START_DATE = LocalDate.parse("2003-01-01");

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
    String pageParam = getSingleParam(params, "page", null);resultsPerPage=Integer.parseInt(getSingleParam(params, "resultsPerPage", "15"));
    if (pageParam != null) {
      int page = Integer.parseInt(pageParam);
      start = (page - 1) * resultsPerPage;
    }
    sortOrder = ArticleSearchQuery.SolrSortOrder.RELEVANCE;
    String sortOrderParam = getSingleParam(params, "sortOrder", null);
    if (!Strings.isNullOrEmpty(sortOrderParam)) {
      sortOrder = ArticleSearchQuery.SolrSortOrder.valueOf(sortOrderParam);
    }
    dateRange = parseDateRange(getSingleParam(params, "dateRange", null),
        getDateParam(params, "filterStartDate"), getDateParam(params, "filterEndDate"));
    List<String> allJournalKeys = SearchController.isNullOrEmpty(params.get("filterJournals"))
        ? new ArrayList<>() : params.get("filterJournals");

    filterJournalNames = new HashSet<>();
    // will have only valid journal keys
    journalKeys = new ArrayList<>();
    for (String journalKey : allJournalKeys) {
      try {
        String journalName = siteSet.getJournalNameFromKey(journalKey);
        journalKeys.add(journalKey);
        filterJournalNames.add(journalName);
      } catch (UnmatchedSiteException umse) {
        log.info("Search on an invalid journal key: %s".format(journalKey));
      }
    }
    startDate = getDateParam(params, "filterStartDate");
    endDate = getDateParam(params, "filterEndDate");

    if (startDate == null && endDate != null) {
      startDate = DEFAULT_START_DATE;
    } else if (startDate != null && endDate == null) {
      endDate = LocalDate.now();
    }

    subjectList = parseSubjects(getSingleParam(params, "subject", null), params.get("filterSubjects"));
    articleTypes = params.get("filterArticleTypes");
    articleTypes = articleTypes == null ? new ArrayList<String>() : articleTypes;
    authors = SearchController.isNullOrEmpty(params.get("filterAuthors"))
        ? new ArrayList<String>() : params.get("filterAuthors");
    sections = SearchController.isNullOrEmpty(params.get("filterSections"))
        ? new ArrayList<String>() : params.get("filterSections");

    isFiltered = !filterJournalNames.isEmpty() || !subjectList.isEmpty() || !articleTypes.isEmpty()
        || dateRange != ArticleSearchQuery.SolrEnumeratedDateRange.ALL_TIME || !authors.isEmpty()
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
    model.addAttribute("filterStartDate", startDate == null ? null : startDate.toString());
    model.addAttribute("filterEndDate", endDate == null ? null : endDate.toString());
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

  public String getSingleParam(Map<String, List<String>> params, String key, String defaultValue) {
    List<String> values = params.get(key);
    return values == null || values.isEmpty() ? defaultValue
        : values.get(0) == null || values.get(0).isEmpty() ? defaultValue : values.get(0);
  }

  private LocalDate getDateParam(Map<String, List<String>> params, String key) {
    String dateString = getSingleParam(params, key, null);
    if (Strings.isNullOrEmpty(dateString)) return null;
    try {
      return LocalDate.parse(dateString);
    } catch (DateTimeParseException e) {
      log.info("Invalid date for {}: {}", key, dateString);
      return null;
    }
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
  private ArticleSearchQuery.SearchCriterion parseDateRange(String dateRangeParam, LocalDate startDate, LocalDate endDate) {
    ArticleSearchQuery.SearchCriterion dateRange = ArticleSearchQuery.SolrEnumeratedDateRange.ALL_TIME;
    if (!Strings.isNullOrEmpty(dateRangeParam)) {
      dateRange = ArticleSearchQuery.SolrEnumeratedDateRange.valueOf(dateRangeParam);
    } else if (startDate != null && endDate != null) {
      dateRange = new ArticleSearchQuery.SolrExplicitDateRange("explicit date range",
          startDate.toString(), endDate.toString());
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
      return subject != null ? ImmutableList.of(subject) : new ArrayList<String>();
    }
  }

  ArticleSearchQuery.Builder makeArticleSearchQueryBuilder() {
    return ArticleSearchQuery.builder()
        .setJournalKeys(journalKeys)
        .setArticleTypes(articleTypes)
        .setSubjects(subjectList)
        .setAuthors(authors)
        .setSections(sections)
        .setStart(start)
        .setRows(resultsPerPage)
        .setSortOrder(sortOrder)
        .setDateRange(dateRange)
        .setStartDate(startDate == null ? null : startDate.toString())
        .setEndDate(endDate == null ? null : endDate.toString());
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
   * Creates an instance of {SearchFilterItem} for active filters using url parameters
   *
   * @param activeFilterItems set of active filter items
   * @param parameterMap      request's query parameter
   * @param filterName        name of the filter
   * @param filterValues      values of the filter
   */
  private void buildActiveFilterItems(Set<SearchFilterItem> activeFilterItems, Map<String,
      String[]> parameterMap, String filterName, String[] filterValues) {

    for (String filterValue : filterValues) {
      List<String> filterValueList = new ArrayList<>(Arrays.asList(filterValues));
      Map<String, List<String>> queryParamMap = new HashMap<>();
      // covert Map<String, String[]> to Map<String, List<String> for code re-usability
      queryParamMap.putAll(parameterMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
          (Map.Entry<String, String[]> entry) -> new ArrayList<>(Arrays.asList(entry.getValue())))));
      queryParamMap.remove(filterName);
      // include the rest of filter values for that specific filter
      if (filterValueList.size() > 1) {
        filterValueList.remove(filterValue);
        queryParamMap.put(filterName, filterValueList);
      }
      String displayName;
      try {
        if (filterName.equals("filterJournals")) {
          displayName = siteSet.getJournalNameFromKey(filterValue);
        } else {
          displayName = filterValue;
        }
        SearchFilterItem filterItem = SearchFilterItem.builder()
          .setDisplayName(displayName)
          .setNumberOfHits(0)
          .setFilterParamName(filterName)
          .setFilterValue(filterValue)
          .setFilteredResultsParameters(queryParamMap)
          .build();
        activeFilterItems.add(filterItem);
      } catch (UnmatchedSiteException umse) {
        log.info("Search on an invalid journal filter: %s".format(filterValue));
      }
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
  public boolean isSimpleSearch(String query) {
    return Arrays.stream(AdvancedSearchTerms.values()).noneMatch(e -> query.contains(e.text));
  }
}
