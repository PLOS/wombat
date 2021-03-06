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

import static org.ambraproject.wombat.service.remote.ArticleSearchQuery.ARTICLE_TYPE_FACET_FIELD;
import static org.ambraproject.wombat.service.remote.ArticleSearchQuery.ARTICLE_TYPE_TAG;
import static org.ambraproject.wombat.service.remote.ArticleSearchQuery.JOURNAL_FACET_FIELD;
import static org.ambraproject.wombat.service.remote.ArticleSearchQuery.JOURNAL_TAG;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.google.common.collect.ImmutableList;
import org.ambraproject.wombat.model.JournalFilterType;
import org.ambraproject.wombat.model.SearchFilter;
import org.ambraproject.wombat.model.SearchFilterFactory;
import org.ambraproject.wombat.model.SingletonSearchFilterType;
import org.ambraproject.wombat.service.remote.ArticleSearchQuery;
import org.ambraproject.wombat.service.remote.SolrSearchApi;
import org.springframework.beans.factory.annotation.Autowired;

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

  private final String SUBJECT_AREA = SingletonSearchFilterType.SUBJECT_AREA.getFilterMapKey();

  private final String SUBJECT_AREA_FACET_FIELD = "subject_facet";

  private final String AUTHOR = SingletonSearchFilterType.AUTHOR.getFilterMapKey();

  private final String AUTHOR_FACET_FIELD = "author_facet";

  private final String ARTICLE_TYPE = SingletonSearchFilterType.ARTICLE_TYPE.getFilterMapKey();

  private final String SECTION = SingletonSearchFilterType.SECTION.getFilterMapKey();

  private final String SECTION_FACET_FIELD = "doc_partial_type";

  /**
   * Retrieves a map of search filters to be added to the model. The filters displayed will change
   * depending on the query executed, but the number and type of filters is constant.
   *
   * @param query Execute query to determine the search filter results.
   *              Must be set as faceted with the addFacet() method
   * @param urlParams search URL parameters that have been rebuilt from the ArticleSearchQuery object
   * @param site The site to perform the searches in
   * @return HashMap containing all applicable filters
   * @throws IOException
   */
  public Map<String, SearchFilter> getSearchFilters(ArticleSearchQuery query) throws IOException {
    ImmutableList<String> facetFields = ImmutableList.of(JOURNAL_FACET_FIELD,
                                                         ARTICLE_TYPE_FACET_FIELD,
                                                         AUTHOR_FACET_FIELD,
                                                         SUBJECT_AREA_FACET_FIELD);
    /* The journal and article type facets are special because we want
     * to do an OR query with them, not an AND query. For example, a
     * client wants to find articles in One OR in Medicine - an
     * article can't be in both. For the rest of the facets the user
     * wants to find content that matches ALL the conditions (AND) -
     * not articles about neurons OR organisms, but articles about
     * neurons AND organisms. This complicates the facets because we
     * want to exclude the existing certain filter queries (fq) when
     * calculating these facet. See
     * https://lucene.apache.org/solr/guide/7_4/faceting.html#Faceting-TaggingandExcludingFilters
     * for how this mechanism works.
     */
    ArticleSearchQuery.Facet journalFacet = ArticleSearchQuery.Facet.builder()
      .setField(JOURNAL_FACET_FIELD)
      .setExcludeKey(JOURNAL_TAG).build();
    ArticleSearchQuery.Facet articleTypeFacet = ArticleSearchQuery.Facet.builder()
      .setField(ARTICLE_TYPE_FACET_FIELD)
      .setExcludeKey(ARTICLE_TYPE_TAG).build();

    ArticleSearchQuery facetQuery = query.toBuilder().setRows(0).setSortOrder(null)
      .addFacet(journalFacet)
      .addFacet(articleTypeFacet)
      .addFacet(AUTHOR_FACET_FIELD)
      .addFacet(SUBJECT_AREA_FACET_FIELD)
      .build();
    SolrSearchApi.Result results = solrSearchApi.search(facetQuery);
    Map<String, Map<String, Integer>> facets = results.getFacets();
    
    SearchFilter journalFilter = searchFilterFactory.createSearchFilter(facets.get(JOURNAL_FACET_FIELD), JOURNAL);
    SearchFilter subjectAreaFilter = searchFilterFactory.createSearchFilter(facets.get(SUBJECT_AREA_FACET_FIELD), SUBJECT_AREA);
    SearchFilter authorFilter = searchFilterFactory.createSearchFilter(facets.get(AUTHOR_FACET_FIELD), AUTHOR);
    SearchFilter articleTypeFilter = searchFilterFactory.createSearchFilter(facets.get(ARTICLE_TYPE_FACET_FIELD), ARTICLE_TYPE);

    ArticleSearchQuery sectionFacetQuery = query.toBuilder()
      .addFacet(SECTION_FACET_FIELD)
      .setRows(0)
      .setSortOrder(null)
      .setPartialSearch(true).build();

    Map<String, Integer> sectionFacetResults = solrSearchApi.search(sectionFacetQuery)
      .getFacets().get(SECTION_FACET_FIELD);
    SearchFilter sectionFilter = searchFilterFactory.createSearchFilter(sectionFacetResults,
        SECTION);

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
