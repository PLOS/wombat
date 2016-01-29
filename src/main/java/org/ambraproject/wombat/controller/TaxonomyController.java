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
import org.ambraproject.wombat.model.CategoryView;
import org.ambraproject.wombat.model.SubjectCount;
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
import java.util.Collection;
import java.util.Collections;
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
  public List<SubjectCount> read(@SiteParam Site site, HttpServletRequest request)
      throws IOException {
    Map<String, Object> taxonomyBrowserConfig = site.getTheme().getConfigMap("taxonomyBrowser");
    boolean hasTaxonomyBrowser = (boolean) taxonomyBrowserConfig.get("hasTaxonomyBrowser");
    if (!hasTaxonomyBrowser) {
      throw new NotFoundException();
    }

    CategoryView categoryView;
    try {
      categoryView = browseTaxonomyService.parseCategories(site.getJournalKey());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    //parent will be null only for the ROOT taxonomy
    String parent = getFullPathVariable(request, true, TAXONOMY_NAMESPACE);
    if (!Strings.isNullOrEmpty(parent)) {
      parent = URLDecoder.decode(parent, "UTF-8");
    }

    if (parent == null) {
      parent = "";
    } else {
      String[] levels = parent.split("/");
      for (String level : levels) {
        categoryView = categoryView.getChild(level);
      }
      if (parent.charAt(0) != '/') {
        parent = '/' + parent;
      }
    }

    Collection<SubjectCount> articleCounts;
    try {
      articleCounts = browseTaxonomyService.getCounts(categoryView, site.getJournalKey());
    } catch (Exception e) {
      throw new IOException(e);
    }

    Map<String, SortedSet<String>> tree = getShortTree(categoryView);
    List<SubjectCount> results = new ArrayList<>(tree.size());
    for (Map.Entry<String, SortedSet<String>> entry : tree.entrySet()) {
      String subjectName = parent + '/' + entry.getKey();
      long childCount = entry.getValue().size();
      long articleCount = articleCounts.stream()
          .filter(count -> count.subject.equalsIgnoreCase(entry.getKey()))
          .findAny().orElse(null).articleCount;
      results.add(new SubjectCount(subjectName, articleCount, childCount));
    }

    if(categoryView.getName().equals("ROOT")) {
      Long rootArticleCount = articleCounts.stream()
          .filter(count -> count.subject.equals("ROOT"))
          .findAny().orElse(null).articleCount;
      results.add(new SubjectCount("ROOT", rootArticleCount, (long) results.size()));
    }

    Collections.sort(results);
    return results;
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

    for(String key : categoryView.getChildren().keySet()) {
      ConcurrentSkipListSet sortedSet = new ConcurrentSkipListSet();
      sortedSet.addAll(categoryView.getChild(key).getChildren().keySet());
      results.put(key, sortedSet);
    }

    return results;
  }
}
