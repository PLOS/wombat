package org.ambraproject.wombat.service.remote;

import org.ambraproject.wombat.model.SearchFilter;
import org.ambraproject.wombat.model.SearchFilterFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Responsible for performing faceted search on different fields used for filtering on the search result
 */
public class SearchFilterService {

  @Autowired
  private SearchService searchService;

  @Autowired
  private SearchFilterFactory searchFilterFactory;

  private final String JOURNAL = "journal";

  private final String JOURNAL_FACET_FIELD = "cross_published_journal_name";

  public Map<?,?> getSimpleSearchFilters(String query, List<String> journalKeys, List<String> articleTypes,
      SearchService.SearchCriterion dateRange) throws IOException {
    SearchQuery.Builder queryObj = SearchQuery.builder()
        .setQuery(query)
        .setFacet(JOURNAL_FACET_FIELD)
        .setSimple(true)
        .setArticleTypes(articleTypes)
        .setDateRange(dateRange);

    Map<?, ?> results = searchService.search(queryObj.build());

    SearchFilter journalFilter = searchFilterFactory.parseFacetedSearchResult(results, JOURNAL);
    Map<String, SearchFilter> filters = new HashMap<>();
    filters.put(JOURNAL, journalFilter);
    // TODO: add other filters here
    return filters;
  }

  public Map<?, ?> getAdvancedSearchFilters(String query, List<String> journalKeys,
                                            List<String> articleTypes, List<String> subjectList, SearchService.SearchCriterion dateRange) throws
      IOException {
    SearchQuery.Builder queryObj = SearchQuery.builder()
        .setFacet(JOURNAL_FACET_FIELD)
        .setQuery(query)
        .setSimple(false)
        .setArticleTypes(articleTypes)
        .setSubjects(subjectList)
        .setDateRange(dateRange);

    Map<?, ?> results = searchService.search(queryObj.build());

    SearchFilter journalFilter = searchFilterFactory.parseFacetedSearchResult(results, JOURNAL);
    Map<String, SearchFilter> filters = new HashMap<>();
    filters.put(JOURNAL, journalFilter);
    // TODO: add other filters here
    return filters;
  }

  public Map<?, ?> getVolumeSearchFilters(int volume, List<String> journalKeys, List<String> articleTypes,
      SearchService.SearchCriterion dateRange) throws IOException {
    Map<String, SearchFilter> filters = new HashMap<>();
    // TODO: add other filters here (filter by journal is not applicable here)
    return filters;
  }
}
