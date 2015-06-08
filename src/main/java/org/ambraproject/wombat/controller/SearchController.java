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
import java.util.Map;

/**
 * Controller class for user-initiated searches.
 */
@Controller
public class SearchController extends WombatController {
  private static final Logger log = LoggerFactory.getLogger(SearchController.class);

  @Autowired
  private SearchService searchService;

  @RequestMapping(value = {"/search", "/{site}/search"})
  public String search(Model model, @SiteParam Site site,
                       @RequestParam(value = "q", required = false) String query,
                       @RequestParam(value = "subject", required = false) String subject,
                       @RequestParam(value = "author", required = false) String author,
                       @RequestParam(value = "page", required = false) Integer page,
                       @RequestParam(value = "sortOrder", required = false) String sortOrderParam,
                       @RequestParam(value = "dateRange", required = false) String dateRangeParam,
                       @RequestParam(value = "resultsPerPage", required = false, defaultValue = "15")
                       Integer resultsPerPage)
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
    SolrSearchService.SolrDateRange dateRange = SolrSearchService.SolrDateRange.ALL_TIME;
    if (!Strings.isNullOrEmpty(dateRangeParam)) {
      dateRange = SolrSearchService.SolrDateRange.valueOf(dateRangeParam);
    }

    model.addAttribute("sortOrders", SolrSearchService.SolrSortOrder.values());
    model.addAttribute("dateRanges", SolrSearchService.SolrDateRange.values());

    // TODO: bind sticky form params using Spring MVC support for Freemarker.  I think we have to add
    // some more dependencies to do this.  See
    // http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/view.html#view-velocity
    model.addAttribute("selectedSortOrder", sortOrder);
    model.addAttribute("selectedDateRange", dateRange);
    model.addAttribute("selectedResultsPerPage", resultsPerPage);

    Map<?, ?> searchResults;
    if (!Strings.isNullOrEmpty(subject)) {
      searchResults = searchService.subjectSearch(subject, site, start, resultsPerPage, sortOrder, dateRange);
    } else if (!Strings.isNullOrEmpty(author)) {
      searchResults = searchService.authorSearch(author, site, start, resultsPerPage, sortOrder, dateRange);
    } else {
      searchResults = searchService.simpleSearch(query, site, start, resultsPerPage, sortOrder, dateRange);
    }
    model.addAttribute("searchResults", searchResults);
    return site.getKey() + "/ftl/search/searchResults";
  }
}
