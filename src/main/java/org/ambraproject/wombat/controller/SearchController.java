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

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.site.url.Link;
import org.ambraproject.wombat.feed.ArticleFeedView;
import org.ambraproject.wombat.feed.FeedMetadataField;
import org.ambraproject.wombat.feed.FeedType;
import org.ambraproject.wombat.model.SearchFilter;
import org.ambraproject.wombat.model.SearchFilterItem;
import org.ambraproject.wombat.model.TaxonomyGraph;
import org.ambraproject.wombat.service.AlertService;
import org.ambraproject.wombat.service.BrowseTaxonomyService;
import org.ambraproject.wombat.service.SearchFilterService;
import org.ambraproject.wombat.service.SolrArticleAdapter;
import org.ambraproject.wombat.service.remote.ApiAddress;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.ambraproject.wombat.service.remote.ArticleSearchQuery;
import org.ambraproject.wombat.service.remote.ServiceRequestException;
import org.ambraproject.wombat.service.remote.SolrSearchApi;
import org.ambraproject.wombat.util.UrlParamBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller class for user-initiated searches.
 */
@Controller
public class SearchController extends WombatController {
  private static final Logger log = LogManager.getLogger(SearchController.class);

  @Autowired
  private SiteSet siteSet;

  @Autowired
  private SolrSearchApi solrSearchApi;

  @Autowired
  private SearchFilterService searchFilterService;

  @Autowired
  private BrowseTaxonomyService browseTaxonomyService;

  @Autowired
  private ArticleFeedView articleFeedView;

  @Autowired
  private Gson gson;

  @Autowired
  private AlertService alertService;

  @Autowired
  private ArticleApi articleApi;

  private final String BROWSE_RESULTS_PER_PAGE = "13";
  private final String CANNOT_PARSE_QUERY_ERROR = "cannotParseQueryError";
  private final String UNKNOWN_QUERY_ERROR = "unknownQueryError";

  @VisibleForTesting
  protected Map<String, String> eIssnToJournalKey;

  /**
   * Initializes the eIssnToJournalKey map if necessary by calling rhino to get eISSNs for all journals.
   *
   * @param siteSet     set of all sites
   * @param currentSite site associated with the current request
   * @throws IOException
   */
  @VisibleForTesting
  protected synchronized void initializeEIssnToJournalKeyMap(SiteSet siteSet, Site currentSite) throws IOException {
    if (eIssnToJournalKey == null) {
      Map<String, String> mutable = new HashMap<>();
      for (Site site : siteSet.getSites()) {
        Map<String, String> rhinoResult = (Map<String, String>) articleApi.requestObject(
            ApiAddress.builder("journals").addToken(site.getJournalKey()).build(), Map.class);
        mutable.put(rhinoResult.get("eIssn"), site.getJournalKey());
      }
      eIssnToJournalKey = ImmutableMap.copyOf(mutable);
    }
  }


  /**
   * Adds a new property, link, to each search result passed in.  The value of this property is the correct URL to the
   * article on this environment.  Calling this method is necessary since article URLs need to be specific to the site
   * of the journal the article is published in, not the site in which the search results are being viewed.
   *
   * @param searchResults deserialized search results JSON
   * @param request       current request
   * @param site          site of the current request (for the search results)
   * @param siteSet       site set of the current request
   * @return searchResults decorated with the new property
   * @throws IOException
   */
  public SolrSearchApi.Result addArticleLinks(SolrSearchApi.Result results, HttpServletRequest request, Site site,
                                   SiteSet siteSet) throws IOException {
    initializeEIssnToJournalKeyMap(siteSet, site);
    SolrSearchApi.Result.Builder builder = results.toBuilder();
    List<Map<String, Object>> docs = results.getDocs().stream().map(doc -> {
        ImmutableMap.Builder<String, Object> newDoc = ImmutableMap.builder();
        String doi = (String) doc.get("id");
        String eIssn = (String) doc.get("eissn");
        String foreignJournalKey = eIssnToJournalKey.get(eIssn);
        String link = Link.toForeignSite(site, foreignJournalKey, siteSet).toPath("/article?id=" + doi).get(request);
        newDoc.putAll(doc);
        newDoc.put("link", link);
        newDoc.put("journalKey", foreignJournalKey);
        return newDoc.build();
      })
      .collect(Collectors.toList());
    return builder.setDocs(docs).build();
  }

  public static boolean isNullOrEmpty(Collection<?> collection) {
    return collection == null || collection.isEmpty();
  }

  private CommonParams modelCommonParams(HttpServletRequest request, Model model,
                                         @SiteParam Site site,
                                         @RequestParam MultiValueMap<String, String> params
                                         ) throws IOException {
    CommonParams commonParams = new CommonParams(siteSet);
    commonParams.parseParams(params);
    commonParams.addToModel(model, request);
    model.addAttribute("sortOrders", ArticleSearchQuery.SolrSortOrder.values());
    model.addAttribute("dateRanges", ArticleSearchQuery.SolrEnumeratedDateRange.values());
    return commonParams;
  }


  /**
   * Performs a simple search and serves the result as XML to be read by an RSS reader
   *
   * @param request HttpServletRequest
   * @param model   model that will be passed to the template
   * @param site    site the request originates from
   * @param params  search parameters identical to the {@code search} method
   * @return RSS view of articles returned by the search
   * @throws IOException
   */
  @RequestMapping(name = "searchFeed", value = "/search/feed/{feedType:atom|rss}",
      params = {"q"}, method = RequestMethod.GET)
  public ModelAndView getSearchRssFeedView(HttpServletRequest request, Model model, @SiteParam Site site,
                                           @PathVariable String feedType, @RequestParam MultiValueMap<String, String> params) throws IOException {
    CommonParams commonParams = modelCommonParams(request, model, site, params);

    String queryString = params.getFirst("q");
    ArticleSearchQuery query = commonParams.makeArticleSearchQueryBuilder()
        .setQuery(queryString)
        .setSimple(commonParams.isSimpleSearch(queryString))
        .setFields(ArticleSearchQuery.RSS_FIELDS)
        .build();

    SolrSearchApi.Result searchResults = solrSearchApi.search(query);

    String feedTitle = new UrlParamBuilder().addAll(params).build().toString();;
    ModelAndView mav = new ModelAndView();
    FeedMetadataField.SITE.putInto(mav, site);
    FeedMetadataField.FEED_INPUT.putInto(mav, searchResults.getDocs());
    FeedMetadataField.TITLE.putInto(mav, feedTitle);
    mav.setView(FeedType.getView(articleFeedView, feedType));
    return mav;
  }

  /**
   * Performs an advanced search and serves the result as XML to be read by an RSS reader
   *
   * @param request HttpServletRequest
   * @param model   model that will be passed to the template
   * @param site    site the request originates from
   * @param params  search parameters identical to the {@code search} method
   * @return RSS view of articles returned by the search
   * @throws IOException
   */
  @RequestMapping(name = "advancedSearchFeed", value = "/search/feed/{feedType:atom|rss}",
      params = {"unformattedQuery"}, method = RequestMethod.GET)
  public ModelAndView getAdvancedSearchRssFeedView(HttpServletRequest request, Model model, @SiteParam Site site,
                                                   @PathVariable String feedType, @RequestParam MultiValueMap<String, String> params) throws IOException {
    String queryString = params.getFirst("unformattedQuery");
    params.remove("unformattedQuery");
    params.add("q", queryString);
    return getSearchRssFeedView(request, model, site, feedType, params);
  }

  /**
   * Performs a "simple" or "advanced" search. The query parameter is read, and if advanced search
   * terms are found, an advanced search is performed. Otherwise, a simple search is performed. The
   * only difference between simple and advanced searches is the use of dismax in the ultimate
   * Solr query.
   *
   * @param request HttpServletRequest
   * @param model   model that will be passed to the template
   * @param site    site the request originates from
   * @param params  all URL parameters
   * @return String indicating template location
   * @throws IOException
   */

  @RequestMapping(name = "simpleSearch", value = "/search")
  public String search(HttpServletRequest request, Model model, @SiteParam Site site,
                       @RequestParam MultiValueMap<String, String> params) throws IOException {
    if (site.isMobile()) {
      /* Desktop site uses dynamic search, so do not populate the search now. */
      if (!performValidSearch(request, model, site, params)) {
        return advancedSearchAjax(model, site);
      }
    }
    return site.getKey() + "/ftl/search/searchResults";
  }

  /**
   * AJAX endpoint to perform a dynamic search, returning search results as JSON. Identical to the
   * {@code search} method above, the query parameter is read, and if advanced search
   * terms are found, an advanced search is performed. Otherwise, a simple search is performed.
   *
   * @param request HttpServletRequest
   * @param model   model that will be passed to the template
   * @param site    site the request originates from
   * @param params  all URL parameters
   * @return String indicating template location
   * @throws IOException
   */
  @RequestMapping(name = "dynamicSearch", value = "/dynamicSearch", params = {"q"})
  @ResponseBody
  public Object dynamicSearch(HttpServletRequest request, Model model, @SiteParam Site site,
                              @RequestParam MultiValueMap<String, String> params) throws IOException {
    performValidSearch(request, model, site, params);
    return gson.toJson(model);
  }

  private boolean performValidSearch(HttpServletRequest request, Model model, @SiteParam Site site,
                                     @RequestParam MultiValueMap<String, String> params
                                     ) throws IOException {
    CommonParams commonParams = modelCommonParams(request, model, site, params);

    String queryString = params.getFirst("q");
    ArticleSearchQuery query = commonParams.makeArticleSearchQueryBuilder()
      .setQuery(queryString)
      .setSimple(commonParams.isSimpleSearch(queryString))
      .build();
    SolrSearchApi.Result searchResults;
    try {
      searchResults = solrSearchApi.search(query);
    } catch (ServiceRequestException sre) {
      model.addAttribute(isInvalidSolrRequest(queryString, sre)
          ? CANNOT_PARSE_QUERY_ERROR : UNKNOWN_QUERY_ERROR, true);
      return false; //not a valid search - report errors
    }

    addFiltersToModel(request, model, site, commonParams, query, searchResults);
    model.addAttribute("searchResults", addArticleLinks(searchResults, request, site, siteSet));

    model.addAttribute("alertQuery", alertService.convertParamsToJson(params));
    return true; //valid search - proceed to return results
  }

  private void addFiltersToModel(HttpServletRequest request, Model model, @SiteParam Site site,
                                 CommonParams commonParams, ArticleSearchQuery queryObj,
                                 SolrSearchApi.Result searchResults) throws IOException {
    Set<SearchFilterItem> activeFilterItems;

    if (searchResults.getNumFound() == 0) {
      activeFilterItems = commonParams.setActiveFilterParams(model, request);
    } else {
      Map<String, SearchFilter> filters = searchFilterService.getSearchFilters(queryObj);
      filters.values().forEach(commonParams::setActiveAndInactiveFilterItems);

      activeFilterItems = new LinkedHashSet<>();
      filters.values().forEach(filter -> activeFilterItems.addAll(filter.getActiveFilterItems()));
      model.addAttribute("searchFilters", filters);
    }

    model.addAttribute("activeFilterItems", activeFilterItems);
  }

  private boolean isInvalidSolrRequest(String queryString, ServiceRequestException sre)
      throws IOException {
    if (sre.getResponseBody().contains("SyntaxError: Cannot parse")) {
      log.info("User attempted invalid search: " + queryString + "\n Exception: " + sre.getMessage());
      return true;
    } else {
      log.error("Unknown error returned from Solr: " + sre.getMessage());
      bugsnag.notify(sre);
      return false;
    }
  }

  /**
   * This is a catch for advanced searches originating from Old Ambra. It transforms the
   * "unformattedQuery" param into "q" which is used by Wombat's new search.
   * todo: remove this method and direct all advancedSearch requests to the simple search method
   */
  @RequestMapping(name = "advancedSearch", value = "/search", params = {"unformattedQuery", "!q"})
  public String advancedSearch(HttpServletRequest request, Model model, @SiteParam Site site,
                               @RequestParam MultiValueMap<String, String> params) throws IOException {
    String queryString = params.getFirst("unformattedQuery");
    params.remove("unformattedQuery");
    params.add("q", queryString);
    return search(request, model, site, params);
  }

  @RequestMapping(name = "newAdvancedSearch", value = "/search", params = {"!unformattedQuery", "!q"})
  public String advancedSearchAjax(Model model, @SiteParam Site site) throws IOException {
    model.addAttribute("isNewSearch", true);
    return site.getKey() + "/ftl/search/searchResults";
  }

  /**
   * Endpoint to render the subject area browser in mobile
   */
  @RequestMapping(name = "mobileSubjectAreaBrowser", value = "/subjectAreaBrowse")
  public String mobileSubjectAreaBrowser(@SiteParam Site site) {
    return site.getKey() + "/ftl/mobileSubjectAreaBrowser";
  }

  /**
   * Serves search result data. Used by the mobile taxonomy browser search results and
   * desktop subject area landing pages. This endpoint returns the articles for all subject areas.
   *
   * @param request HttpServletRequest
   * @param model   model that will be passed to the template
   * @param site    site the request originates from
   * @param params  all URL parameters
   * @return String indicating template location
   * @throws IOException
   */
  @RequestMapping(name = "browse", value = "/browse")
  public String browseAll(HttpServletRequest request, Model model, @SiteParam Site site,
                          @RequestParam MultiValueMap<String, String> params) throws IOException {
    subjectAreaSearch(request, model, site, params, "");
    return site.getKey() + "/ftl/browse/subjectArea/browseSubjectArea";
  }

  /**
   * Serves search result data. Used by the mobile taxonomy browser search results and
   * desktop subject area landing pages. This endpoint returns the articles for a single subject area.
   *
   * @param request HttpServletRequest
   * @param model   model that will be passed to the template
   * @param site    site the request originates from
   * @param subject the subject area to be searched
   * @param params  all URL parameters
   * @return String indicating template location
   * @throws IOException
   */
  @RequestMapping(name = "browseSubjectArea", value = "/browse/{subject}")
  public String browseSubjectArea(HttpServletRequest request, Model model, @SiteParam Site site,
                                  @PathVariable String subject, @RequestParam MultiValueMap<String, String> params)
      throws IOException {
    subjectAreaSearch(request, model, site, params, subject);
    return site.getKey() + "/ftl/browse/subjectArea/browseSubjectArea";
  }

  /**
   * Set defaults and performs search for subject area landing page
   *
   * @param request HTTP request for browsing subject areas
   * @param model   model that will be passed to the template
   * @param site    site the request originates from
   * @param params  HTTP request params
   * @param subject the subject area to be search; return all articles if no subject area is provided
   * @throws IOException
   */
  private void subjectAreaSearch(HttpServletRequest request, Model model, Site site,
                                 MultiValueMap<String, String> params, String subject) throws IOException {

    TaxonomyGraph taxonomyGraph = modelSubjectHierarchy(model, site, subject);

    String subjectName;
    if (Strings.isNullOrEmpty(subject)) {
      params.add("subject", "");
      subjectName = "All Subject Areas";
    } else {
      subject = subject.replace("_", " ");
      params.add("subject", subject);
      subjectName = taxonomyGraph.getName(subject);
    }
    model.addAttribute("subjectName", subjectName);

    // set defaults for subject area landing page
    if (isNullOrEmpty(params.get("resultsPerPage"))) {
      params.add("resultsPerPage", BROWSE_RESULTS_PER_PAGE);
    }

    if (isNullOrEmpty(params.get("sortOrder"))) {
      params.add("sortOrder", "DATE_NEWEST_FIRST");
    }

    if (isNullOrEmpty(params.get("filterJournals"))) {
      params.add("filterJournals", site.getJournalKey());
    }

    CommonParams commonParams = modelCommonParams(request, model, site, params);
    ArticleSearchQuery query = commonParams.makeArticleSearchQueryBuilder()
      .setSimple(false).build();

    SolrSearchApi.Result searchResults = solrSearchApi.search(query);

    model.addAttribute("articles", SolrArticleAdapter.unpackSolrQuery(searchResults));
    model.addAttribute("searchResults", addArticleLinks(searchResults, request, site, siteSet));
    model.addAttribute("page", commonParams.getSingleParam(params, "page", "1"));
    model.addAttribute("journalKey", site.getKey());
    model.addAttribute("isBrowse", true);

    String authId = request.getRemoteUser();
    boolean subscribed = false;
    if (authId != null) {
      String subjectParam = Strings.isNullOrEmpty(subject) ? "" : subjectName;
      subscribed = alertService.isUserSubscribed(authId, site.getJournalKey(), subjectParam);
    }
    model.addAttribute("subscribed", subscribed);
  }

  private TaxonomyGraph modelSubjectHierarchy(Model model, Site site, String subject) throws IOException {
    TaxonomyGraph fullTaxonomyView = browseTaxonomyService.parseCategories(site.getJournalKey());

    Collection<String> subjectParents;
    Collection<String> subjectChildren;
    if (subject != null && subject.length() > 0) {
      //Recreate the category name as stored in the DB
      subject = subject.replace("_", " ");

      TaxonomyGraph.CategoryView categoryView = fullTaxonomyView.getView(subject);
      if (categoryView == null) {
        throw new NotFoundException(String.format("category %s does not exist.", subject));
      } else {
        if (categoryView.getParents().isEmpty()) {
          subjectParents = new HashSet<>();
        } else {
          subjectParents = categoryView.getParents().keySet();
        }

        subjectChildren = categoryView.getChildren().keySet();
      }
    } else {
      subjectParents = new HashSet<>();
      subjectChildren = fullTaxonomyView.getRootCategoryNames();
    }

    model.addAttribute("subjectParents", subjectParents);
    model.addAttribute("subjectChildren", subjectChildren);

    return fullTaxonomyView;
  }
}
