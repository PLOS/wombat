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
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.service.remote.ArticleSearchQuery;
import org.ambraproject.wombat.service.remote.SoaService;
import org.ambraproject.wombat.service.remote.SolrSearchService;
import org.ambraproject.wombat.service.remote.SolrSearchServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

/**
 * Controller for the browse page.
 */
@Controller
public class BrowseController extends WombatController {

  @Autowired
  private SoaService soaService;

  @Autowired
  private SolrSearchService solrSearchService;

  @Autowired
  private SiteSet siteSet;

  private static final int RESULT_PER_PAGE = 13;

  @RequestMapping(name = "browse", value = "/browse")
  public String browse(Model model, @SiteParam Site site) {
    model.addAttribute("journalKey", site.getKey());
    return site.getKey() + "/ftl/browse";
  }

  @RequestMapping(name = "browseSubjectArea", value = "/browse/{subject}")
  public String browsSubjectArea(HttpServletRequest request, Model model, @SiteParam Site site,
                                 @PathVariable String subject) throws
      IOException {
    enforceDevFeature("browse");
    // TODO: check the site, this controller should return 404 for non PLOS One journals
    if (!Strings.isNullOrEmpty(subject)) {
      subject = subject.replace("_", " ");
      ArticleSearchQuery query = ArticleSearchQuery.builder()
          .setQuery("")
          .setSubjects(Collections.singletonList(subject))
          .setJournalKeys(Collections.singletonList(site.getJournalKey()))
          .setArticleTypes(new ArrayList<>())
          .setRows(RESULT_PER_PAGE)
          .setSortOrder(SolrSearchServiceImpl.SolrSortOrder.RELEVANCE)
          .setDateRange(SolrSearchServiceImpl.SolrEnumeratedDateRange.ALL_TIME)
          .setIsPartialSearch(false)
          .setForRawResults(false)
          .build();
      Map<?, ?> searchResults = solrSearchService.search(query);

      model.addAttribute("searchResults", solrSearchService.addArticleLinks(searchResults, request, site,
          siteSet));
    }

    model.addAttribute("journalKey", site.getKey());
    return site.getKey() + "/ftl/browseSubjectArea";
  }

  @RequestMapping(name = "browseVolumes", value = "/browse/volume")
  public String browseVolume(Model model, @SiteParam Site site) throws IOException {
    enforceDevFeature("browse");
    String journalMetaUrl = "journals/" + site.getJournalKey();
    Map<String, Object> journalMetadata = soaService.requestObject(journalMetaUrl, Map.class);
    model.addAttribute("journal", journalMetadata);
    return site.getKey() + "/ftl/article/browseVolumes";
  }

  @RequestMapping(name = "browseIssues", value = "/browse/issue")
  public String browseIssue(Model model, @SiteParam Site site,
                            @RequestParam("id") String issueId) throws IOException {
    //TODO: implement this method. Stubbed out here to provide a linkable endpoint for the browseVolumes template
    enforceDevFeature("browse");
    return site.getKey() + "/ftl/article/browseIssues";
  }
}
