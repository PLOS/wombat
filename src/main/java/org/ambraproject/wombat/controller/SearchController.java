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
import org.ambraproject.wombat.service.SearchService;
import org.ambraproject.wombat.service.SolrSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

/**
 * Controller class for user-initiated searches.
 */
@Controller
public class SearchController {

  @Autowired
  private SearchService searchService;

  @RequestMapping("/{journal}/search")
  public String search(Model model, @PathVariable("journal") String journal, @RequestParam("q") String query,
      @RequestParam(value = "sortOrder", required = false) String sortOrderParam,
      @RequestParam(value = "dateRange", required = false) String dateRangeParam) throws IOException {

    // TODO: paging.  Initialize these from params.
    int start = 1;
    int rows = 25;

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
    model.addAttribute("currentQuery", query);

    // TODO: consider using an enum for possible journal values, and validating here.
    model.addAttribute("searchResults", searchService.simpleSearch(query, journal, start, rows, sortOrder, dateRange));
    return journal + "/ftl/search/searchResults";
  }
}
