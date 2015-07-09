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

import com.google.common.base.Strings;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.service.remote.SearchService;
import org.ambraproject.wombat.service.remote.SolrSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Controller class for user-initiated searches.
 */
@Controller
public class SearchController extends WombatController {
  private static final Logger log = LoggerFactory.getLogger(SearchController.class);

  @Autowired
  private SiteSet siteSet;

  @Autowired
  private SearchService searchService;

  /**
   * Performs a search.
   *
   * @param request the incoming request
   * @param model information to be sent to the template
   * @param site the current site
   * @param query "simple search" query.  This word or phrase will be searched against all
   *     solr fields.
   * @param unformattedQuery "advanced search" query.  This may be a boolean combination of any
   *     predicates that query any of the solr fields.  This is the boolean expression that can
   *     be built on the advanced search page.  If present, query will be ignored.
   * @param subject if present, a subject search will be performed on this subject, and query and
   *     unformattedQuery will be ignored. Mobile only.
   * @param author if present, an author search will be performed on this author, and query and
   *     unformattedQuery will be ignored.
   * @param page results page we are requesting (1-based).  If absent, the first page of results
   *     will be returned.
   * @param sortOrderParam specifies the sort order of results.  If absent, the sort order will
   *     default to relevance.
   * @param dateRangeParam specifies the publication date range for the results.  If absent, results
   *     from all time will be returned.
   * @param resultsPerPage maximum number of results to return (for the given results page).
   * @param journals list of journal keys in which to search.  If absent, the search will only
   *     be over the current site.
   * @param subjects if present, a subject search will be performed on this subject, and query and
   *     unformattedQuery will be ignored. Only the first subject provided is used.
   * @return path to the search results freemarker template
   * @throws IOException
   */
  @RequestMapping(value = {"/search", "/{site}/search"})
  public String search(HttpServletRequest request, Model model, @SiteParam Site site,
                       @RequestParam(value = "q", required = false) String query,
                       @RequestParam(value = "unformattedQuery", required = false) String unformattedQuery,
                       @RequestParam(value = "subject", required = false) String subject,
                       @RequestParam(value = "author", required = false) String author,
                       @RequestParam(value = "page", required = false) Integer page,
                       @RequestParam(value = "sortOrder", required = false) String sortOrderParam,
                       @RequestParam(value = "dateRange", required = false) String dateRangeParam,
                       @RequestParam(value = "filterStartDate", required = false) String startDate,
                       @RequestParam(value = "filterEndDate", required = false) String endDate,
                       @RequestParam(value = "resultsPerPage", required = false, defaultValue = "15")
                       Integer resultsPerPage,
                       @RequestParam(value = "filterJournals", required = false) List<String> journals,
                       @RequestParam(value = "filterSubjects", required = false) List<String> subjects,
                       @RequestParam(value = "filterArticleTypes", required = false) List<String> articleTypes)
          throws IOException {

    if (query == null) {
      log.warn("Received search request in {} with null query param (possible apache rewrite issue)", site);
      // May be due to apache rewrite config issue which needs attention. Meanwhile, set query to
      // empty string which will direct the user to a search error page instead of a hard NPE trace
      query="";
    }
    int start = 0;
    if (page != null) {
      start = (page - 1) * resultsPerPage;
    }
    model.addAttribute("resultsPerPage", resultsPerPage);

    SolrSearchService.SolrSortOrder sortOrder = SolrSearchService.SolrSortOrder.RELEVANCE;
    if (!Strings.isNullOrEmpty(sortOrderParam)) {
      sortOrder = SolrSearchService.SolrSortOrder.valueOf(sortOrderParam);
    }

    SearchService.SearchCriterion dateRange = parseDateRange(dateRangeParam, startDate, endDate);

    journals = parseJournals(site, journals, unformattedQuery);
    //Journal name is identical between mobile and desktop sites, so the first site matched is sufficient
    Set<String> filterJournalNames = new HashSet<>();
    for (String journalKey : journals) {
      filterJournalNames.add(siteSet.getSites(journalKey).get(0).getJournalName());
    }
    model.addAttribute("filterJournalNames", filterJournalNames);

    //TODO: split or share model assignments between mobile and desktop
    model.addAttribute("filterJournals", journals);
    model.addAttribute("filterStartDate",startDate);
    model.addAttribute("filterEndDate", endDate);

    subject = parseSubjects(subject, subjects);
    List<String> subjectList = subject == null ? new ArrayList<String>() : Collections.singletonList(subject);
    model.addAttribute("filterSubjects", subjectList);

    model.addAttribute("sortOrders", SolrSearchService.SolrSortOrder.values());
    model.addAttribute("dateRanges", SolrSearchService.SolrEnumeratedDateRange.values());

    articleTypes = articleTypes == null ? new ArrayList<String>() : articleTypes;
    model.addAttribute("filterArticleTypes", articleTypes);

    // TODO: bind sticky form params using Spring MVC support for Freemarker.  I think we have to add
    // some more dependencies to do this.  See
    // http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/view.html#view-velocity
    model.addAttribute("selectedSortOrder", sortOrder);
    model.addAttribute("selectedDateRange", dateRange);
    model.addAttribute("selectedResultsPerPage", resultsPerPage);

    Boolean isFiltered = !filterJournalNames.isEmpty() || dateRange != SolrSearchService.SolrEnumeratedDateRange.ALL_TIME
        || !subjectList.isEmpty() || !articleTypes.isEmpty();
    model.addAttribute("isFiltered", isFiltered);

    Map<?, ?> searchResults;
    if (!Strings.isNullOrEmpty(unformattedQuery)) {
      searchResults = searchService.advancedSearch(unformattedQuery, journals, articleTypes, start,
          resultsPerPage, sortOrder);
    } else if (!Strings.isNullOrEmpty(subject)) {
      searchResults = searchService.subjectSearch(subject, journals, articleTypes, start,
          resultsPerPage, sortOrder, dateRange);
    } else if (!Strings.isNullOrEmpty(author)) {
      searchResults = searchService.authorSearch(author, journals, articleTypes, start,
          resultsPerPage, sortOrder, dateRange);
    } else {
      searchResults = searchService.simpleSearch(query, journals, articleTypes, start,
          resultsPerPage, sortOrder, dateRange);
    }
    model.addAttribute("searchResults", searchResults);

    // We pass in the request parameters here, because they are needed by paging.ftl.
    // The normal way to get request parameters from a freemarker template is to use the
    // RequestParameters variable, but due to a bug in freemarker, this does not handle
    // multi-valued parameters correctly.  See http://sourceforge.net/p/freemarker/bugs/324/
    model.addAttribute("parameterMap", request.getParameterMap());
    return site.getKey() + "/ftl/search/searchResults";
  }

  /**
   * Determines which publication dates to filter by in the search.
   * If no dates are input, a default date range of All Time will be used.
   * Mobile search only provides the enumerated dateRangeParam field, while desktop search provides
   * explicit fields for start and end dates. The parameters are mutually exclusive.
   * @param dateRangeParam mobile date range enumeration value
   * @param startDate desktop start date value
   * @param endDate desktop end date value
   * @return A generic @SearchCriterion object used by Solr
   */
  private SearchService.SearchCriterion parseDateRange(String dateRangeParam, String startDate, String endDate) {
    SearchService.SearchCriterion dateRange = SolrSearchService.SolrEnumeratedDateRange.ALL_TIME;
    if (!Strings.isNullOrEmpty(dateRangeParam)) {
      dateRange = SolrSearchService.SolrEnumeratedDateRange.valueOf(dateRangeParam);
    } else if (!Strings.isNullOrEmpty(startDate) && !Strings.isNullOrEmpty(endDate)) {
      dateRange = new SolrSearchService.SolrExplicitDateRange("explicit date range", startDate,
          endDate);
    }
    return dateRange;
  }

  /**
   * Determines what journal keys to use in the search.
   *
   * @param site the site the request is associated with
   * @param journalParams journal keys passed as URL parameters, if any
   * @param unformattedQuery the value of the unformattedQuery param (used in advanced search)
   * @return if unformattedQuery is non-empty, and journalParams is empty, all journal keys will be
   *     returned.  Otherwise, if journalParams is non-empty, those will be returned; otherwise
   *     the current site's journal key will be returned.
   */
  private List<String> parseJournals(Site site, List<String> journalParams, String unformattedQuery) {

    // If we are in advanced search mode (unformattedQuery populated), and no journals are specified,
    // we default to all journals.
    if (!Strings.isNullOrEmpty(unformattedQuery) && (journalParams == null || journalParams.isEmpty())) {
      return new ArrayList(siteSet.getJournalKeys());
    } else {

      // If no filterJournals param is present, default to the current site.
      return journalParams == null || journalParams.isEmpty()
          ? Collections.singletonList(site.getJournalKey())
          : journalParams;
    }
  }

  //TODO: allow filtering by multiple subjects
  /**
   * subject is a mobile-only parameter, while subjects is a desktop-only parameter
   * @param subject mobile subject area value
   * @param subjects desktop list of subject area values
   * @return String subject if subjects is null or empty else return subjects[0]
   */
  private String parseSubjects(String subject, List<String> subjects) {
    if (Strings.isNullOrEmpty(subject) && subjects != null && subjects.size() > 0
        && !Strings.isNullOrEmpty(subjects.get(0))) {
      subject = subjects.get(0);
    }
    return subject;
  }
}
