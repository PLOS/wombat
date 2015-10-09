package org.ambraproject.wombat.service.remote;

import com.google.common.collect.Multimap;
import org.ambraproject.wombat.model.SearchFilter;
import org.ambraproject.wombat.model.SearchFilterFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Responsible for performing faceted search on different fields used for filtering on the search result
 */
public class SearchFilterService {

  @Autowired
  private SolrSearchService solrSearchService;

  @Autowired
  private SearchFilterFactory searchFilterFactory;

  private final String JOURNAL = "journal";

  private final String JOURNAL_FACET_FIELD = "cross_published_journal_name";

  private final String SUBJECT_AREA = "subject_area";

  private final String SUBJECT_AREA_FACET_FIELD = "subject_facet";

  public Map<?, ?> getSearchFilters(SearchQuery searchQuery, Multimap<String, String> urlParams)
      throws IOException {
    SearchQuery.Builder journalFacetSearchQuery = SearchQuery.builder()
        .setFacet(JOURNAL_FACET_FIELD)
        .setCommonQueryParams(searchQuery);

    Map<?, ?> journalFacetResults = solrSearchService.search(journalFacetSearchQuery.build());

    SearchFilter journalFilter = searchFilterFactory.parseFacetedSearchResult(journalFacetResults,
        JOURNAL, urlParams);

    SearchQuery.Builder subjectAreaFacetSearchQuery = SearchQuery.builder()
        .setFacet(SUBJECT_AREA_FACET_FIELD)
        .setCommonQueryParams(searchQuery);

    Map<?, ?> subjectAreaFacetResults = solrSearchService.search(subjectAreaFacetSearchQuery.build());

    SearchFilter subjectAreaFilter = searchFilterFactory.parseFacetedSearchResult(
        subjectAreaFacetResults, SUBJECT_AREA, urlParams);

    Map<String, SearchFilter> filters = new HashMap<>();
    filters.put(JOURNAL, journalFilter);
    filters.put(SUBJECT_AREA, subjectAreaFilter);
    // TODO: add other filters here
    return filters;
  }

  public Map<?, ?> getVolumeSearchFilters(int volume, List<String> journalKeys, List<String> articleTypes,
                                          SolrSearchService.SearchCriterion dateRange) throws IOException {
    Map<String, SearchFilter> filters = new HashMap<>();
    // TODO: add other filters here (filter by journal is not applicable here)
    return filters;
  }
}
