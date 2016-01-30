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
import org.ambraproject.wombat.model.TaxonomyCountTable;
import org.ambraproject.wombat.model.TaxonomyGraph;
import org.ambraproject.wombat.model.TaxonomyGraph.CategoryView;
import org.ambraproject.wombat.service.BrowseTaxonomyService;
import org.ambraproject.wombat.service.remote.SoaService;
import org.ambraproject.wombat.util.HttpMessageUtil;
import org.ambraproject.wombat.util.UriUtil;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Controller that handles JSON requests from the taxonomy browser.
 */
@Controller
public class TaxonomyController extends WombatController {

  private static final String TAXONOMY_NAMESPACE = "/taxonomy/";
  private static final String TAXONOMY_TEMPLATE = TAXONOMY_NAMESPACE + "**";

  @Autowired
  private SoaService soaService;

  @Autowired
  private BrowseTaxonomyService browseTaxonomyService;

  @RequestMapping(name = "taxonomy", value = "" + TAXONOMY_TEMPLATE, method = RequestMethod.GET)
  @ResponseBody
  //todo: use query parameters to send category data instead of parsing the path to get the parent term
  public List<SubjectData> read(@SiteParam Site site, HttpServletRequest request)
      throws IOException {
    Map<String, Object> taxonomyBrowserConfig = site.getTheme().getConfigMap("taxonomyBrowser");
    boolean hasTaxonomyBrowser = (boolean) taxonomyBrowserConfig.get("hasTaxonomyBrowser");
    if (!hasTaxonomyBrowser) {
      throw new NotFoundException();
    }

    TaxonomyGraph taxonomyGraph = browseTaxonomyService.parseCategories(site.getJournalKey());

    //parent will be null only for the ROOT taxonomy
    String parent = getFullPathVariable(request, true, TAXONOMY_NAMESPACE);
    if (!Strings.isNullOrEmpty(parent)) {
      parent = URLDecoder.decode(parent, "UTF-8");
    }

    List<String> terms = TaxonomyGraph.parseTerms(parent);
    String parentLeafNodeName = terms.get(terms.size() - 1);
    CategoryView categoryView = taxonomyGraph.getView(parentLeafNodeName);

    TaxonomyCountTable articleCounts = browseTaxonomyService.getCounts(taxonomyGraph, site.getJournalKey());

    Map<String, SortedSet<String>> tree = getShortTree(categoryView);
    List<SubjectData> results = new ArrayList<>(tree.size());
    for (Map.Entry<String, SortedSet<String>> entry : tree.entrySet()) {
      String subjectName = parent + '/' + entry.getKey();
      long childCount = entry.getValue().size();
      long articleCount = articleCounts.getCount(subjectName);
      results.add(new SubjectData(subjectName, articleCount, childCount));
    }

    if(categoryView.getName().equals("ROOT")) {
      Long rootArticleCount = articleCounts.getCount("ROOT");
      results.add(new SubjectData("ROOT", rootArticleCount, (long) results.size()));
    }

    Collections.sort(results, Comparator.comparing(SubjectData::getSubject));
    return results;
  }

  private static class SubjectData {
    private final String subject;
    private final long articleCount;
    private final long childCount;

    private SubjectData(String subject, long articleCount, long childCount) {
      this.subject = subject;
      this.articleCount = articleCount;
      this.childCount = childCount;
    }

    public String getSubject() {
      return subject;
    }

    public long getArticleCount() {
      return articleCount;
    }

    public long getChildCount() {
      return childCount;
    }
  }

  @RequestMapping(name = "taxonomyCategoryFlag", value = "" + TAXONOMY_NAMESPACE + "flag/{action:add|remove}", method = RequestMethod.POST)
  @ResponseBody
  public void setFlag(HttpServletRequest request, HttpServletResponse responseToClient,
                      @RequestParam(value = "categoryTerm", required = true) String categoryTerm,
                      @RequestParam(value = "articleDoi", required = true) String articleDoi)
      throws IOException {
    // pass through any article category flagging ajax traffic to/from rhino
    URI forwardedUrl = UriUtil.concatenate(soaService.getServerUrl(), UriUtil.stripUrlPrefix(request.getRequestURI(), TAXONOMY_NAMESPACE));
    HttpUriRequest req = HttpMessageUtil.buildRequest(forwardedUrl, "POST",
            HttpMessageUtil.getRequestParameters(request), new BasicNameValuePair("authId", request.getRemoteUser()));
    soaService.forwardResponse(req, responseToClient);
  }

  /**
   * For the top elements: return keys and the immediate children
   *
   * @param categoryView
   *
   * @return a map of keys and the immediate children
   */
  @SuppressWarnings("unchecked")
  public static Map<String, SortedSet<String>> getShortTree(CategoryView categoryView) {

    Map<String, SortedSet<String>> results = new ConcurrentSkipListMap<>();

    for(CategoryView child : categoryView.getChildren().values()) {
      ConcurrentSkipListSet sortedSet = new ConcurrentSkipListSet();
      sortedSet.addAll(categoryView.getChildren().keySet());
      results.put(child.getName(), sortedSet);
    }

    return results;
  }
}
