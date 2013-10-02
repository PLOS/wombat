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

package org.ambraproject.wombat.controller.plos;

import org.ambraproject.wombat.config.Site;
import org.ambraproject.wombat.controller.ControllerHook;
import org.ambraproject.wombat.service.SearchService;
import org.ambraproject.wombat.service.SoaService;
import org.ambraproject.wombat.service.SolrSearchService;
import org.springframework.ui.Model;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * ControllerHook that adds additional model data needed to render the PLOS ONE homepage.
 */
public class PlosOneHome implements ControllerHook {

  // TODO: is this too much of a hack?  Through some contortions we could get this
  // through RuntimeConfiguration instead, but it seems like a PLOS ONE-specific
  // class should "just know" what its site is.
  private static final Site SITE = new Site("PlosOne", "PLoSONE");

  private SoaService soaService;

  private SearchService searchService;

  /**
   * {@inheritDoc}
   */
  @Override
  public void populateCustomModelAttributes(Model model) throws IOException {
    List<?> inTheNewsArticles = soaService.requestObject("journals/PLoSONE?inTheNewsArticles", List.class);

    // From the presentation layer's perspective, all three of these article lists look the same.
    // However, two of them come from solr, and one from rhino.  Unfortunately solr uses
    // "id" as the name of the DOI attribute, while rhino uses "doi".  So this hack is
    // necessary...
    for (Object obj : inTheNewsArticles) {
      Map article = (Map) obj;
      article.put("id", article.get("doi"));
    }
    model.addAttribute("inTheNewsArticles", inTheNewsArticles);

    // TODO: paging

    Map<?, ?> recentArticlesSearch = searchService.simpleSearch(null, SITE, 1, 7,
        SolrSearchService.SolrSortOrder.DATE_NEWEST_FIRST, SolrSearchService.SolrDateRange.ALL_TIME);
    model.addAttribute("recentArticles", recentArticlesSearch.get("docs"));
    Map<?, ?> popularArticlesSearch = searchService.simpleSearch(null, SITE, 1, 7,
        SolrSearchService.SolrSortOrder.MOST_VIEWS_ALL_TIME, SolrSearchService.SolrDateRange.ALL_TIME);
    model.addAttribute("popularArticles", popularArticlesSearch.get("docs"));
  }

  public void setSoaService(SoaService soaService) {
    this.soaService = soaService;
  }

  public void setSearchService(SearchService searchService) {
    this.searchService = searchService;
  }
}
