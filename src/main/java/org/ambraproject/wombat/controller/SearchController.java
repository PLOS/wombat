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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.service.remote.SearchService;
import org.ambraproject.wombat.service.remote.SolrSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Controller class for user-initiated searches.
 */
@Controller
public class SearchController extends WombatController {
  private static final Logger log = LoggerFactory.getLogger(SearchController.class);

  @Autowired
  private SiteSet siteSet;

  @Autowired
  private SearchService searchService;

  /**
   * Class that encapsulates the parameters that are shared across many different search types.
   * For example, a subject search and an advanced search will have many parameters in common,
   * such as sort order, date range, page, results per page, etc.  This class eliminates the
   * need to have long lists of @RequestParam parameters duplicated across many controller
   * methods.
   * <p/>
   * This class also contains logic having to do with which parameters take precedence
   * over others, defaults when parameters are absent, and the like.
   */
  @VisibleForTesting
  static final class CommonParams {

    /**
     * The number of the first desired result (zero-based) that will be passed to solr.
     * Calculated from the page and resultsPerPage URL parameters.
     */
    int start;

    SolrSearchService.SolrSortOrder sortOrder;

    SearchService.SearchCriterion dateRange;

    List<String> articleTypes;

    List<String> journalKeys;

    @VisibleForTesting
    Set<String> filterJournalNames;

    @VisibleForTesting
    List<String> subjectList;

    /**
     * Indicates whether any filter parameters are being applied to the search (journal, subject area, etc).
     */
    @VisibleForTesting
    boolean isFiltered;

    private SiteSet siteSet;

    private Site site;

    private int resultsPerPage;

    private String startDate;

    private String endDate;

    /**
     * Constructor.
     *
     * @param siteSet siteSet associated with the request
     * @param site site of the request
     */
    CommonParams(SiteSet siteSet, Site site) {
      this.siteSet = siteSet;
      this.site = site;
    }

    /**
     * Extracts parameters from the raw parameter map, and performs some logic related to what
     * parameters take precedence and default values when ones aren't present.
     *
     * @param params
     * @throws IOException
     */
    void parseParams(Map<String, List<String>> params) throws IOException {
      String pageParam = getSingleParam(params, "page", null);
      resultsPerPage = Integer.parseInt(getSingleParam(params, "resultsPerPage", "15"));
      if (pageParam != null) {
        int page = Integer.parseInt(pageParam);
        start = (page - 1) * resultsPerPage;
      }
      sortOrder = SolrSearchService.SolrSortOrder.RELEVANCE;
      String sortOrderParam = getSingleParam(params, "sortOrder", null);
      if (!Strings.isNullOrEmpty(sortOrderParam)) {
        sortOrder = SolrSearchService.SolrSortOrder.valueOf(sortOrderParam);
      }
      dateRange = parseDateRange(getSingleParam(params, "dateRange", null),
          getSingleParam(params, "filterStartDate", null), getSingleParam(params, "filterEndDate", null));
      journalKeys = parseJournals(site, params.get("filterJournals"), getSingleParam(params, "unformattedQuery", null));

      //Journal name is identical between mobile and desktop sites, so the first site matched is sufficient
      filterJournalNames = new HashSet<>();
      for (String journalKey : journalKeys) {
        filterJournalNames.add(siteSet.getSites(journalKey).get(0).getJournalName());
      }
      startDate = getSingleParam(params, "filterStartDate", null);
      endDate = getSingleParam(params, "filterEndDate", null);
      subjectList = parseSubjects(getSingleParam(params, "subject", null), params.get("filterSubjects"));
      articleTypes = params.get("filterArticleTypes");
      articleTypes = articleTypes == null ? new ArrayList<String>() : articleTypes;
      isFiltered = !filterJournalNames.isEmpty() || !subjectList.isEmpty() || !articleTypes.isEmpty()
          || dateRange != SolrSearchService.SolrEnumeratedDateRange.ALL_TIME;
    }

    /**
     * Adds parameters (and derived values) back to the model needed for results page rendering.
     * This only adds model attributes that are shared amongst different types of searches; it is
     * the caller's responsibility to add the search results and any other data needed.
     *
     * @param model model that will be passed to the template
     * @param request HttpServletRequest
     */
    void addToModel(Model model, HttpServletRequest request) {
      model.addAttribute("resultsPerPage", resultsPerPage);
      model.addAttribute("filterJournalNames", filterJournalNames);

      // TODO: split or share model assignments between mobile and desktop.
      model.addAttribute("filterJournals", journalKeys);
      model.addAttribute("filterStartDate", startDate);
      model.addAttribute("filterEndDate", endDate);
      model.addAttribute("filterSubjects", subjectList);
      model.addAttribute("filterArticleTypes", articleTypes);

      // TODO: bind sticky form params using Spring MVC support for Freemarker.  I think we have to add
      // some more dependencies to do this.  See
      // http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/view.html#view-velocity
      model.addAttribute("selectedSortOrder", sortOrder);
      model.addAttribute("selectedDateRange", dateRange);
      model.addAttribute("selectedResultsPerPage", resultsPerPage);
      model.addAttribute("isFiltered", isFiltered);

      // We pass in the request parameters here, because they are needed by paging.ftl.
      // The normal way to get request parameters from a freemarker template is to use the
      // RequestParameters variable, but due to a bug in freemarker, this does not handle
      // multi-valued parameters correctly.  See http://sourceforge.net/p/freemarker/bugs/324/
      model.addAttribute("parameterMap", request.getParameterMap());
    }

    private String getSingleParam(Map<String, List<String>> params, String key, String defaultValue) {
      List<String> values = params.get(key);
      return values == null || values.isEmpty() ? defaultValue : values.get(0);
    }

    /**
     * Determines which publication dates to filter by in the search.
     * If no dates are input, a default date range of All Time will be used.
     * Mobile search only provides the enumerated dateRangeParam field, while desktop search provides
     * explicit fields for start and end dates. The parameters are mutually exclusive.
     * @param dateRangeParam mobile date range enumeration value
     * @param startDate desktop start date value
     * @param endDate desktop end date value
     * @return A generic @SearchCriterion object used by Solr
     */
    private SearchService.SearchCriterion parseDateRange(String dateRangeParam, String startDate, String endDate) {
      SearchService.SearchCriterion dateRange = SolrSearchService.SolrEnumeratedDateRange.ALL_TIME;
      if (!Strings.isNullOrEmpty(dateRangeParam)) {
        dateRange = SolrSearchService.SolrEnumeratedDateRange.valueOf(dateRangeParam);
      } else if (!Strings.isNullOrEmpty(startDate) && !Strings.isNullOrEmpty(endDate)) {
        dateRange = new SolrSearchService.SolrExplicitDateRange("explicit date range", startDate,
            endDate);
      }
      return dateRange;
    }

    /**
     * Determines what journal keys to use in the search.
     *
     * @param site the site the request is associated with
     * @param journalParams journal keys passed as URL parameters, if any
     * @param unformattedQuery the value of the unformattedQuery param (used in advanced search)
     * @return if unformattedQuery is non-empty, and journalParams is empty, all journal keys will be
     *     returned.  Otherwise, if journalParams is non-empty, those will be returned; otherwise
     *     the current site's journal key will be returned.
     */
    private List<String> parseJournals(Site site, List<String> journalParams, String unformattedQuery) {

      // If we are in advanced search mode (unformattedQuery populated), and no journals are specified,
      // we default to all journals.
      if (!Strings.isNullOrEmpty(unformattedQuery) && (journalParams == null || journalParams.isEmpty())) {
        return new ArrayList(siteSet.getJournalKeys());
      } else {

        // If no filterJournals param is present, default to the current site.
        return journalParams == null || journalParams.isEmpty()
            ? Collections.singletonList(site.getJournalKey())
            : journalParams;
      }
    }

    /**
     * subject is a mobile-only parameter, while subjects is a desktop-only parameter
     * @param subject mobile subject area value
     * @param subjects desktop list of subject area values
     * @return singleton list of subject if subjects is null or empty, else return subjects
     */
    private List<String> parseSubjects(String subject, List<String> subjects) {
      if (Strings.isNullOrEmpty(subject) && subjects != null && subjects.size() > 0) {
        return subjects;
      } else {
        return subject != null ? Collections.singletonList(subject) : new ArrayList<String>();
      }
    }
  }

  // Unless the "!volume" part is included in the params in the next few methods, you will
  // get an "ambiguous handler method" exception from spring.  I think this is because all
  // of these methods (including volumeSearch) use a MultiValueMap for @RequestParam, instead
  // of individually listing the params.

  /**
   * Performs a "simple" search, where the q parameter's value is a single search term.
   *
   * @param request HttpServletRequest
   * @param model model that will be passed to the template
   * @param site site the request originates from
   * @param params all URL parameters
   * @return String indicating template location
   * @throws IOException
   */
  @RequestMapping(name = "simpleSearch", value = "/{site}/search", params = {"q", "!volume"})
  public String simpleSearch(HttpServletRequest request, Model model, @SiteParam Site site,
      @RequestParam MultiValueMap<String, String> params) throws IOException {
    CommonParams commonParams = new CommonParams(siteSet, site);
    commonParams.parseParams(params);
    commonParams.addToModel(model, request);
    addOptionsToModel(model);
    Map<?, ?> searchResults = searchService.simpleSearch(params.getFirst("q"), commonParams.journalKeys,
        commonParams.articleTypes, commonParams.start, commonParams.resultsPerPage, commonParams.sortOrder,
        commonParams.dateRange);
    model.addAttribute("searchResults", searchService.addArticleLinks(searchResults, request, site, siteSet, true));
    return site.getKey() + "/ftl/search/searchResults";
  }

  /**
   * Performs an "advanced" search, where the unformattedQuery parameter may have a boolean combination
   * of search terms.  The form that generates this URL is still served by ambra.
   *
   * @param request HttpServletRequest
   * @param model model that will be passed to the template
   * @param site site the request originates from
   * @param params all URL parameters
   * @return String indicating template location
   * @throws IOException
   */
  @RequestMapping(name = "advancedSearch", value = "/{site}/search", params = {"unformattedQuery", "!volume"})
  public String advancedSearch(HttpServletRequest request, Model model, @SiteParam Site site,
      @RequestParam MultiValueMap<String, String> params) throws IOException {
    CommonParams commonParams = new CommonParams(siteSet, site);
    commonParams.parseParams(params);
    commonParams.addToModel(model, request);
    addOptionsToModel(model);
    Map<?, ?> searchResults = searchService.advancedSearch(params.getFirst("unformattedQuery"),
        commonParams.journalKeys, commonParams.articleTypes, commonParams.subjectList, commonParams.start,
        commonParams.resultsPerPage, commonParams.sortOrder);
    model.addAttribute("searchResults", searchService.addArticleLinks(searchResults, request, site, siteSet, true));
    return site.getKey() + "/ftl/search/searchResults";
  }

  /**
   * Performs a subject search, where the subject param is expected to be a term from our taxonomy.
   *
   * @param request HttpServletRequest
   * @param model model that will be passed to the template
   * @param site site the request originates from
   * @param params all URL parameters
   * @return String indicating template location
   * @throws IOException
   */
  @RequestMapping(name = "subjectSearch", value = "/{site}/search", params = {"subject", "!filterSubjects", "!volume",
      "!unformattedQuery"})
  public String subjectSearch(HttpServletRequest request, Model model, @SiteParam Site site,
      @RequestParam MultiValueMap<String, String> params) throws IOException {
    return doSubjectsSearch(request, model, site, params);
  }

  /**
   * Performs a subject search, where the subject param is expected to be a term from our taxonomy.
   * Multiple subjects are allowed in the filterSubjects params.
   *
   * @param request HttpServletRequest
   * @param model model that will be passed to the template
   * @param site site the request originates from
   * @param params all URL parameters
   * @return String indicating template location
   * @throws IOException
   */
  @RequestMapping(name = "subjectsSearch", value = "/{site}/search", params = {"filterSubjects", "!subject", "!volume",
      "!unformattedQuery"})
  public String subjectsSearch(HttpServletRequest request, Model model, @SiteParam Site site,
      @RequestParam MultiValueMap<String, String> params) throws IOException {
    return doSubjectsSearch(request, model, site, params);
  }

  private String doSubjectsSearch(HttpServletRequest request, Model model, Site site,
      MultiValueMap<String, String> params) throws IOException {
    CommonParams commonParams = new CommonParams(siteSet, site);
    commonParams.parseParams(params);
    commonParams.addToModel(model, request);
    addOptionsToModel(model);
    Map<?, ?> searchResults = searchService.subjectSearch(commonParams.subjectList, commonParams.journalKeys,
        commonParams.start, commonParams.resultsPerPage, commonParams.sortOrder, commonParams.dateRange);
    model.addAttribute("searchResults", searchService.addArticleLinks(searchResults, request, site, siteSet, true));
    return site.getKey() + "/ftl/search/searchResults";
  }

  /**
   * Performs an author search, where the value of the author param is searched against authors' names.
   *
   * @param request HttpServletRequest
   * @param model model that will be passed to the template
   * @param site site the request originates from
   * @param params all URL parameters
   * @return String indicating template location
   * @throws IOException
   */
  @RequestMapping(name = "authorSearch", value = "/{site}/search", params = {"author", "!volume"})
  public String authorSearch(HttpServletRequest request, Model model, @SiteParam Site site,
      @RequestParam MultiValueMap<String, String> params) throws IOException {
    CommonParams commonParams = new CommonParams(siteSet, site);
    commonParams.parseParams(params);
    commonParams.addToModel(model, request);
    addOptionsToModel(model);
    Map<?, ?> searchResults = searchService.authorSearch(params.getFirst("author"), commonParams.journalKeys,
        commonParams.start, commonParams.resultsPerPage, commonParams.sortOrder, commonParams.dateRange);
    model.addAttribute("searchResults", searchService.addArticleLinks(searchResults, request, site, siteSet, true));
    return site.getKey() + "/ftl/search/searchResults";
  }

  // Requests coming from the advanced search form with URLs beginning with "/search/quick/" will always
  // have the parameters id, eLocationId, and volume, although only one will be populated.  The expressions
  // like "id!=" in the following request mappings cause spring to map to controller methods only if
  // the corresponding parameters are present, and do not have a value of the empty string.

  /**
   * Searches for an article having the given doi (the value of the id parameter).  If the DOI
   * exists for any journal, this method will redirect to the article.  Otherwise, an empty
   * search results page will be rendered.
   *
   * @param request HttpServletRequest
   * @param model model that will be passed to the template
   * @param site site the request originates from
   * @param doi identifies the article
   * @return String indicating template location
   * @throws IOException
   */
  @RequestMapping(name = "doiSearch", value = "/{site}/search", params = {"id!="})
  public String doiSearch(HttpServletRequest request, Model model, @SiteParam Site site,
      @RequestParam(value = "id", required = true) String doi) throws IOException {
    Map<?, ?> searchResults = searchService.lookupArticleByDoi(doi);
    return renderSingleResult(searchResults, "doi:" + doi, request, model, site);
  }

  /**
   * Searches for an article having the given eLocationId from the given journal.  Note that eLocationIds
   * are only unique within journals, so both parameters are necessary.  If the article is found, this
   * method will redirect to it; otherwise an empty search results page will be rendered.
   *
   * @param request HttpServletRequest
   * @param model model that will be passed to the template
   * @param site site the request originates from
   * @param eLocationId identifies the article in a journal
   * @param journal journal to search within
   * @return String indicating template location
   * @throws IOException
   */
  @RequestMapping(name = "eLocationSearch", value = "/{site}/search", params = {"eLocationId!="})
  public String eLocationSearch(HttpServletRequest request, Model model, @SiteParam Site site,
      @RequestParam(value = "eLocationId", required = true) String eLocationId,
      @RequestParam(value = "filterJournals", required = true) String journal) throws IOException {
    Map<?, ?> searchResults = searchService.lookupArticleByELocationId(eLocationId, journal);
    return renderSingleResult(searchResults, "elocation_id:" + eLocationId, request, model, site);
  }

  /**
   * Searches for all articles in the volume identified by the value of the volume parameter.
   *
   * @param request HttpServletRequest
   * @param model model that will be passed to the template
   * @param site site the request originates from
   * @param params all URL parameters
   * @return String indicating template location
   * @throws IOException
   */
  @RequestMapping(name = "volumeSearch", value = "/{site}/search", params = {"volume!="})
  public String volumeSearch(HttpServletRequest request, Model model, @SiteParam Site site,
      @RequestParam MultiValueMap<String, String> params) throws IOException {
    CommonParams commonParams = new CommonParams(siteSet, site);
    commonParams.parseParams(params);
    commonParams.addToModel(model, request);
    addOptionsToModel(model);
    int volume;
    try {
      volume = Integer.parseInt(params.getFirst("volume"));
    } catch (NumberFormatException nfe) {
      return renderEmptyResults(null, "volume:" + params.getFirst("volume"), model, site);
    }
    Map<?, ?> searchResults = searchService.volumeSearch(volume, commonParams.journalKeys, commonParams.articleTypes,
        commonParams.start, commonParams.resultsPerPage, commonParams.sortOrder, commonParams.dateRange);
    model.addAttribute("searchResults", searchService.addArticleLinks(searchResults, request, site, siteSet, true));
    model.addAttribute("otherQuery", String.format("volume:%d", volume));
    return site.getKey() + "/ftl/search/searchResults";
  }

  /**
   * Renders either an article page, or an empty search results page.
   *
   * @param searchResults deserialized JSON that should be either empty, or contain a single article
   * @param searchTerm the search term (suitable for display) that was input
   * @param request HttpServletRequest
   * @param model model that will be passed to the template
   * @param site site the request originates from
   * @return String indicating template location
   * @throws IOException
   */
  private String renderSingleResult(Map<?, ?> searchResults, String searchTerm, HttpServletRequest request, Model model,
      Site site) throws IOException {
    int numFound = ((Double) searchResults.get("numFound")).intValue();
    if (numFound > 1) {
      throw new IllegalStateException("Valid DOIs should return exactly one article");
    }
    if (numFound == 1) {
      searchResults = searchService.addArticleLinks(searchResults, request, site, siteSet, false);
      List docs = (List) searchResults.get("docs");
      Map doc = (Map) docs.get(0);
      return "redirect:" + doc.get("link");
    } else {
      return renderEmptyResults(searchResults, searchTerm, model, site);
    }
  }

  /**
   * Renders an empty search results page.
   *
   * @param searchResults empty search results.  If null, one will be constructed.
   * @param searchTerm the search term (suitable for display) that was input
   * @param model model that will be passed to the template
   * @param site site the request originates from
   * @return String indicating template location
   */
  private String renderEmptyResults(Map searchResults, String searchTerm, Model model, Site site) {
    if (searchResults == null) {
      searchResults = new HashMap<>();
      searchResults.put("numFound", 0);
    }
    model.addAttribute("searchResults", searchResults);
    model.addAttribute("otherQuery", searchTerm);
    addOptionsToModel(model);

    // Add minimum model attributes necessary to render the form.
    model.addAttribute("selectedSortOrder", SolrSearchService.SolrSortOrder.RELEVANCE);
    model.addAttribute("selectedDateRange", SolrSearchService.SolrEnumeratedDateRange.ALL_TIME);
    model.addAttribute("isFiltered", false);
    model.addAttribute("resultsPerPage", 15);
    return site.getKey() + "/ftl/search/searchResults";
  }

  private void addOptionsToModel(Model model) {
    model.addAttribute("sortOrders", SolrSearchService.SolrSortOrder.values());
    model.addAttribute("dateRanges", SolrSearchService.SolrEnumeratedDateRange.values());
  }
}
