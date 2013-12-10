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

import com.google.common.base.Strings;
import org.ambraproject.wombat.config.Site;
import org.ambraproject.wombat.controller.ControllerHook;
import org.ambraproject.wombat.controller.HomeController;
import org.ambraproject.wombat.service.SearchService;
import org.ambraproject.wombat.service.SoaService;
import org.ambraproject.wombat.service.SolrSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ControllerHook that adds additional model data needed to render the PLOS ONE homepage.
 */
public class PlosOneHome implements ControllerHook {
  private static final Logger log = LoggerFactory.getLogger(PlosOneHome.class);

  /**
   * Enumerates the allowed values for the section parameter for this page.
   */
  private static enum Section {
    RECENT,
    POPULAR,
    IN_THE_NEWS;
  }

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
  public void populateCustomModelAttributes(HttpServletRequest request, Model model) throws IOException {
    Section section = Section.RECENT;
    String sectionParam = request.getParameter("section");
    if (!Strings.isNullOrEmpty(sectionParam)) {

      // TODO: better validation/error handling
      section = Section.valueOf(sectionParam.toUpperCase());
    }
    model.addAttribute("selectedSection", section.name().toLowerCase());

    switch (section) {
      case RECENT:
        HomeController.populateWithArticleList(request, model, SITE, searchService,
            SolrSearchService.SolrSortOrder.DATE_NEWEST_FIRST);
        break;

      case POPULAR:
        HomeController.populateWithArticleList(request, model, SITE, searchService,
            SolrSearchService.SolrSortOrder.MOST_VIEWS_ALL_TIME);
        break;

      case IN_THE_NEWS:
        model.addAttribute("articles", getInTheNewsArticles());
        break;

      default:
        throw new IllegalStateException("Unexpected section value " + section);
    }
  }

  private Map getInTheNewsArticles() throws IOException {
    List<?> inTheNewsArticles = soaService.requestObject("journals/PLoSONE?inTheNewsArticles", List.class);

    // From the presentation layer's perspective, all three of these article lists look the same.
    // However, two of them come from solr, and one from rhino.  Unfortunately solr uses
    // "id" as the name of the DOI attribute, while rhino uses "doi".  So this hack is
    // necessary...
    for (Object obj : inTheNewsArticles) {
      Map article = (Map) obj;
      article.put("id", article.get("doi"));
    }
    Map results = new HashMap();
    results.put("docs", inTheNewsArticles);
    return results;
  }

  public void setSoaService(SoaService soaService) {
    this.soaService = soaService;
  }

  public void setSearchService(SearchService searchService) {
    this.searchService = searchService;
  }
}
