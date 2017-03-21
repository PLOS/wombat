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

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.model.TaxonomyCountTable;
import org.ambraproject.wombat.model.TaxonomyGraph;
import org.ambraproject.wombat.model.TaxonomyGraph.CategoryView;
import org.ambraproject.wombat.service.remote.ApiAddress;
import org.ambraproject.wombat.service.BrowseTaxonomyService;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.ambraproject.wombat.service.remote.UserApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
  private ArticleApi articleApi;
  @Autowired
  private UserApi userApi;
  @Autowired
  private BrowseTaxonomyService browseTaxonomyService;

  @RequestMapping(name = "taxonomy", value = TAXONOMY_TEMPLATE, method = RequestMethod.GET)
  @ResponseBody
  public List<SubjectData> read(@SiteParam Site site,
      @RequestParam MultiValueMap<String, String> params)
      throws IOException {
    Map<String, Object> taxonomyBrowserConfig = site.getTheme().getConfigMap("taxonomyBrowser");
    boolean hasTaxonomyBrowser = (boolean) taxonomyBrowserConfig.get("hasTaxonomyBrowser");
    if (!hasTaxonomyBrowser) {
      throw new NotFoundException();
    }

    TaxonomyGraph taxonomyGraph = browseTaxonomyService.parseCategories(site.getJournalKey());

    //parent will be null only for the ROOT taxonomy
    String parent;
    if (params.isEmpty()) {
      parent = null;
    } else {
      List<String> categoryParams = params.get("c");
      //todo: After cleaning up redirects and solving the 502 proxy error, this replace should be removed
      categoryParams.replaceAll(s -> s.replace("_", " "));
      parent = Joiner.on("/").join(categoryParams);
    }

    TaxonomyCountTable articleCounts = browseTaxonomyService.getCounts(taxonomyGraph, site.getJournalKey());

    final Collection<CategoryView> children;
    if (parent != null) {
      List<String> terms = TaxonomyGraph.parseTerms(parent);
      String parentLeafNodeName = terms.get(terms.size() - 1);
      CategoryView categoryView = taxonomyGraph.getView(parentLeafNodeName);
      children = categoryView.getChildren().values();
    } else {
      children = taxonomyGraph.getRootCategoryViews();
    }
    Map<String, SortedSet<String>> tree = getShortTree(children);

    List<SubjectData> results = new ArrayList<>(tree.size());
    for (Map.Entry<String, SortedSet<String>> entry : tree.entrySet()) {
      String key = entry.getKey();
      String subjectName = Strings.nullToEmpty(parent) + '/' + key;
      long childCount = entry.getValue().size();
      long articleCount = articleCounts.getCount(key);
      results.add(new SubjectData(subjectName, articleCount, childCount));
    }

    if (parent == null) {
      long rootArticleCount = articleCounts.getCount("ROOT");
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
                      @PathVariable(value = "action") String action,
                      @RequestParam(value = "categoryTerm", required = true) String categoryTerm,
                      @RequestParam(value = "articleDoi", required = true) String articleDoi)
      throws IOException {
    ApiAddress.Builder address = ApiAddress.builder("articles").embedDoi(articleDoi).addToken("categories")
        .addParameter("flag", action)
        .addParameter("categoryTerm", categoryTerm);

    String authId = request.getRemoteUser();
    if (authId != null) {
      String userId = userApi.getUserIdFromAuthId(authId);
      address.addParameter("userId", userId);
    }

    articleApi.postObject(address.build(), null);
  }

  /**
   * For the top elements: return keys and the immediate children
   *
   * @param children
   *
   * @return a map of keys and the immediate children
   */
  @SuppressWarnings("unchecked")
  public static Map<String, SortedSet<String>> getShortTree(Collection<CategoryView> children) {

    Map<String, SortedSet<String>> results = new ConcurrentSkipListMap<>();

    for(CategoryView child : children) {
      ConcurrentSkipListSet sortedSet = new ConcurrentSkipListSet();
      sortedSet.addAll(child.getChildren().keySet());
      results.put(child.getName(), sortedSet);
    }

    return results;
  }
}
