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

package org.ambraproject.wombat.service.remote;

import com.google.common.collect.Multimap;
import org.ambraproject.wombat.model.JournalFilterType;
import org.ambraproject.wombat.model.SearchFilter;
import org.ambraproject.wombat.model.SearchFilterFactory;
import org.ambraproject.wombat.model.SingletonSearchFilterType;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
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
   * @return HashMap containing all applicable filters
   * @throws IOException
   */
  public Map<String, SearchFilter> getSearchFilters(ArticleSearchQuery query,
      Multimap<String, String> urlParams) throws IOException {

    ArticleSearchQuery.Builder journalFacetQuery = ArticleSearchQuery.builder()
        .setFacet(JOURNAL_FACET_FIELD)
        .setQuery(query.getQuery().orElse(null))
        .setSimple(query.isSimple())
        .setArticleTypes(query.getArticleTypes())
        .setSubjects(query.getSubjects())
        .setAuthors(query.getAuthors())
        .setDateRange(query.getDateRange().orElse(null))
        .setSections(query.getSections());

    Map<?, ?> journalFacetResults = solrSearchApi.search(journalFacetQuery.build());
    SearchFilter journalFilter = searchFilterFactory
        .createSearchFilter(journalFacetResults, JOURNAL, urlParams);

    ArticleSearchQuery.Builder subjectAreaFacetQuery = ArticleSearchQuery.builder()
        .setFacet(SUBJECT_AREA_FACET_FIELD)
        .setQuery(query.getQuery().orElse(null))
        .setSimple(query.isSimple())
        .setArticleTypes(query.getArticleTypes())
        .setAuthors(query.getAuthors())
        .setDateRange(query.getDateRange().orElse(null))
        .setJournalKeys(query.getJournalKeys())
        .setSections(query.getSections())
        .setSubjects(query.getSubjects());  // pass the previously filtered subjects to narrow the results

    Map<?, ?> subjectAreaFacetResults = solrSearchApi.search(subjectAreaFacetQuery.build());
    SearchFilter subjectAreaFilter = searchFilterFactory
        .createSearchFilter(subjectAreaFacetResults, SUBJECT_AREA, urlParams);

    ArticleSearchQuery.Builder authorFacetQuery = ArticleSearchQuery.builder()
        .setFacet(AUTHOR_FACET)
        .setQuery(query.getQuery().orElse(null))
        .setSimple(query.isSimple())
        .setJournalKeys(query.getJournalKeys())
        .setArticleTypes(query.getArticleTypes())
        .setDateRange(query.getDateRange().orElse(null))
        .setAuthors(query.getAuthors()) // pass the previously filtered authors to narrow the results
        .setSubjects(query.getSubjects())
        .setSections(query.getSections());


    Map<?, ?> authorFacetResults = solrSearchApi.search(authorFacetQuery.build());
    SearchFilter authorFilter = searchFilterFactory.createSearchFilter(authorFacetResults, AUTHOR, urlParams);

    ArticleSearchQuery.Builder articleTypeFacetQuery = ArticleSearchQuery.builder()
        .setFacet(ARTICLE_TYPE_FACET)
        .setQuery(query.getQuery().orElse(null))
        .setSimple(query.isSimple())
        .setDateRange(query.getDateRange().orElse(null))
        .setJournalKeys(query.getJournalKeys())
        .setSubjects(query.getSubjects())
        .setAuthors(query.getAuthors())
        .setSections(query.getSections());

    Map<?, ?> articleTypeFacetResults = solrSearchApi.search(articleTypeFacetQuery.build());
    SearchFilter articleTypeFilter = searchFilterFactory.createSearchFilter(articleTypeFacetResults,
        ARTICLE_TYPE, urlParams);

    ArticleSearchQuery.Builder sectionFacetQuery = ArticleSearchQuery.builder()
        .setFacet(SECTION_FACET)
        .setIsPartialSearch(true)
        .setQuery(query.getQuery().orElse(null))
        .setSimple(query.isSimple())
        .setDateRange(query.getDateRange().orElse(null))
        .setJournalKeys(query.getJournalKeys())
        .setSubjects(query.getSubjects())
        .setAuthors(query.getAuthors());

    Map<?, ?> sectionFacetResults = solrSearchApi.search(sectionFacetQuery.build());
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

  public Map<String, SearchFilter> getVolumeSearchFilters(int volume, List<String> journalKeys, List<String> articleTypes,
      SolrSearchApi.SearchCriterion dateRange) throws IOException {
    Map<String, SearchFilter> filters = new HashMap<>();
    // TODO: add other filters here (filter by journal is not applicable here)
    return filters;
  }
}
