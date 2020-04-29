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

package org.ambraproject.wombat.service;

import com.google.common.collect.Multimap;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.model.JournalFilterType;
import org.ambraproject.wombat.model.SearchFilter;
import org.ambraproject.wombat.model.SearchFilterFactory;
import org.ambraproject.wombat.model.SingletonSearchFilterType;
import org.ambraproject.wombat.service.remote.ArticleSearchQuery;
import org.ambraproject.wombat.service.remote.SolrSearchApi;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Responsible for performing faceted search on different fields used for filtering search results
 * Note that the Date Filter is a special case and need not be added here
 */
public class SearchFilterService {

  @Autowired
  private SolrSearchApi solrSearchApi;

  @Autowired
  private SearchFilterFactory searchFilterFactory;

  private final String JOURNAL = JournalFilterType.JOURNAL_FILTER_MAP_KEY;

  private final String JOURNAL_FACET_FIELD = "journal_name";

  private final String SUBJECT_AREA = SingletonSearchFilterType.SUBJECT_AREA.getFilterMapKey();

  private final String SUBJECT_AREA_FACET_FIELD = "subject_facet";

  private final String AUTHOR = SingletonSearchFilterType.AUTHOR.getFilterMapKey();

  private final String AUTHOR_FACET = "author_facet";

  private final String ARTICLE_TYPE = SingletonSearchFilterType.ARTICLE_TYPE.getFilterMapKey();

  private final String ARTICLE_TYPE_FACET = "article_type_facet";

  private final String SECTION = SingletonSearchFilterType.SECTION.getFilterMapKey();

  private final String SECTION_FACET = "doc_partial_type";

  /**
   * Retrieves a map of search filters to be added to the model. The filters displayed will change
   * depending on the query executed, but the number and type of filters is constant.
   *
   * @param query Execute query to determine the search filter results.
   *              Must be set as faceted with the setFacet() method
   * @param urlParams search URL parameters that have been rebuilt from the ArticleSearchQuery object
   * @param site The site to perform the searches in
   * @return HashMap containing all applicable filters
   * @throws IOException
   */
  public Map<String, SearchFilter> getSearchFilters(ArticleSearchQuery query,
                                                    Multimap<String, String> urlParams,
                                                    Site site) throws IOException {
    ArticleSearchQuery journalFacetQuery =
      query.toBuilder().setFacet(JOURNAL_FACET_FIELD).build();

    Map<?, ?> journalFacetResults = solrSearchApi.search(journalFacetQuery, site);
    SearchFilter journalFilter = searchFilterFactory
        .createSearchFilter(journalFacetResults, JOURNAL, urlParams);

    ArticleSearchQuery subjectAreaFacetQuery = query.toBuilder().setFacet(SUBJECT_AREA_FACET_FIELD).build();

    Map<?, ?> subjectAreaFacetResults = solrSearchApi.search(subjectAreaFacetQuery, site);
    SearchFilter subjectAreaFilter = searchFilterFactory
        .createSearchFilter(subjectAreaFacetResults, SUBJECT_AREA, urlParams);

    ArticleSearchQuery authorFacetQuery = query.toBuilder().setFacet(AUTHOR_FACET).build();

    Map<?, ?> authorFacetResults = solrSearchApi.search(authorFacetQuery, site);
    SearchFilter authorFilter = searchFilterFactory.createSearchFilter(authorFacetResults, AUTHOR, urlParams);

    ArticleSearchQuery articleTypeFacetQuery = query.toBuilder().setFacet(ARTICLE_TYPE_FACET).build();

    Map<?, ?> articleTypeFacetResults = solrSearchApi.search(articleTypeFacetQuery, site);
    SearchFilter articleTypeFilter = searchFilterFactory.createSearchFilter(articleTypeFacetResults,
        ARTICLE_TYPE, urlParams);

    ArticleSearchQuery sectionFacetQuery = query.toBuilder()
      .setFacet(SECTION_FACET)
      .setPartialSearch(true).build();

    Map<?, ?> sectionFacetResults = solrSearchApi.search(sectionFacetQuery, site);
    SearchFilter sectionFilter = searchFilterFactory.createSearchFilter(sectionFacetResults,
        SECTION, urlParams);

    // TODO: add other filters here

    Map<String, SearchFilter> filters = new HashMap<>();
    filters.put(JOURNAL, journalFilter);
    filters.put(SUBJECT_AREA, subjectAreaFilter);
    filters.put(AUTHOR, authorFilter);
    filters.put(ARTICLE_TYPE, articleTypeFilter);
    filters.put(SECTION, sectionFilter);

    return filters;
  }

}
