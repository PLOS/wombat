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
import org.ambraproject.wombat.service.remote.SearchService;
import org.ambraproject.wombat.service.remote.SolrSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Controller class for user-initiated searches.
 */
@Controller
public class SearchController extends WombatController {
  private static final Logger log = LoggerFactory.getLogger(SearchController.class);

  @Autowired
  private SearchService searchService;

  /**
   * Performs a search.
   *
   * @param model information to be sent to the template
   * @param site the current site
   * @param query "simple search" query.  This word or phrase will be searched against all
   *     solr fields.
   * @param unformattedQuery "advanced search" query.  This may be a boolean combination of any
   *     predicates that query any of the solr fields.  This is the boolean expression that can
   *     be built on the advanced search page.  If present, query will be ignored.
   * @param subject if present, a subject search will be performed on this subject, and query and
   *     unformattedQuery will be ignored.
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
   * @return path to the search results freemarker template
   * @throws IOException
   */
  @RequestMapping(value = {"/search", "/{site}/search"})
  public String search(Model model, @SiteParam Site site,
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
                       @RequestParam(value = "filterJournals", required = false) List<String> journals)
          throws IOException {

    // If no filterJournals param is present, default to the current site.
    journals = journals == null || journals.isEmpty() ? Collections.singletonList(site.getJournalKey()) : journals;
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

    //Mobile search only provides the enumerated dateRangeParam field, while desktop search provides
    //explicit fields for start and end dates. The parameters are mutually exclusive.
    SearchService.SearchCriterion dateRange = SolrSearchService.SolrEnumeratedDateRange.ALL_TIME;
    SolrSearchService.SolrEnumeratedDateRange enumeratedDateRange
        = SolrSearchService.SolrEnumeratedDateRange.ALL_TIME;
    if (!Strings.isNullOrEmpty(dateRangeParam)) {
      dateRange = SolrSearchService.SolrEnumeratedDateRange.valueOf(dateRangeParam);
    } else if (!Strings.isNullOrEmpty(startDate) && !Strings.isNullOrEmpty(endDate)) {
      dateRange = new SolrSearchService.SolrExplicitDateRange("explicit date range", startDate,
          endDate);
    }

    //TODO: split or share model assignments between mobile and desktop
    model.addAttribute("sortOrders", SolrSearchService.SolrSortOrder.values());
    model.addAttribute("dateRanges", SolrSearchService.SolrEnumeratedDateRange.values());

    // TODO: bind sticky form params using Spring MVC support for Freemarker.  I think we have to add
    // some more dependencies to do this.  See
    // http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/view.html#view-velocity
    model.addAttribute("selectedSortOrder", sortOrder);
    model.addAttribute("selectedDateRange", enumeratedDateRange);
    model.addAttribute("selectedResultsPerPage", resultsPerPage);

    Map<?, ?> searchResults;
    if (!Strings.isNullOrEmpty(unformattedQuery)) {
      searchResults = searchService.advancedSearch(unformattedQuery, journals, start, resultsPerPage, sortOrder);
    } else if (!Strings.isNullOrEmpty(subject)) {
      searchResults = searchService.subjectSearch(subject, journals, start, resultsPerPage, sortOrder, dateRange);
    } else if (!Strings.isNullOrEmpty(author)) {
      searchResults = searchService.authorSearch(author, journals, start, resultsPerPage,
          sortOrder, dateRange);
    } else {
      searchResults = searchService.simpleSearch(query, journals, start, resultsPerPage,
          sortOrder, dateRange);
    }
    model.addAttribute("searchResults", searchResults);
    return site.getKey() + "/ftl/search/searchResults";
  }
}
