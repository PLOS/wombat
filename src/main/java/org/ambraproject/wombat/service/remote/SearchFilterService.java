package org.ambraproject.wombat.service.remote;

import org.ambraproject.wombat.model.SearchFilter;
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
  private SearchFilter searchFilter;

  private final String JOURNAL = "journal";

  private Map<String, SearchFilter> filters;

  private final String JOURNAL_FACET_FIELD = "cross_published_journal_name";

  public Map<?,?> getSimpleSearchFilters(String query, List<String> journalKeys, List<String> articleTypes,
      SearchService.SearchCriterion dateRange) throws IOException {
    filters = new HashMap<>();
    Map<?, ?> results = searchService.simpleSearch(JOURNAL_FACET_FIELD, query, new ArrayList<String>(), articleTypes,
        dateRange);

    SearchFilter journalFilter = searchFilter.parseFacetedSearchResult(results, JOURNAL);
    filters.put(JOURNAL, journalFilter);
    // TODO: add other filters here
    return filters;
  }

  public Map<?, ?> getAdvancedSearchFilers(String query, List<String> journalKeys,
      List<String> articleTypes, List<String> subjectList, SearchService.SearchCriterion dateRange) throws
      IOException {
    filters = new HashMap<>();
    Map<?, ?> results = searchService.advancedSearch(JOURNAL_FACET_FIELD, query, new ArrayList<String>(), articleTypes,
        subjectList, dateRange);
    SearchFilter journalFilter = searchFilter.parseFacetedSearchResult(results, JOURNAL);
    filters.put(JOURNAL, journalFilter);
    // TODO: add other filters here
    return filters;
  }

  public Map<?, ?> getSubjectSearchFilters(List<String> subjects, List<String> journalKeys,
      List<String> articleTypes, SearchService.SearchCriterion dateRange) throws IOException {
    filters = new HashMap<>();
    Map<?, ?> results = searchService.subjectSearch(JOURNAL_FACET_FIELD, subjects, new ArrayList<String>(), articleTypes,
        dateRange);
    SearchFilter journalFilter = searchFilter.parseFacetedSearchResult(results, JOURNAL);
    filters.put(JOURNAL, journalFilter);
    // TODO: add other filters here
    return filters;
  }

  public Map<?, ?> getAuthorSearchFilters(String author, List<String> journalKeys,
      List<String> articleTypes, SearchService.SearchCriterion dateRange) throws IOException {
    filters = new HashMap<>();
    Map<?, ?> results = searchService.authorSearch(JOURNAL_FACET_FIELD, author, new ArrayList<String>(), articleTypes,
        dateRange);
    SearchFilter journalFilter = searchFilter.parseFacetedSearchResult(results, JOURNAL);
    filters.put(JOURNAL, journalFilter);
    // TODO: add other filters here
    return filters;
  }

  public Map<?, ?> getVolumeSearchFilters(int volume, List<String> journalKeys, List<String> articleTypes,
      SearchService.SearchCriterion dateRange) throws IOException {
    filters = new HashMap<>();
    // TODO: add other filters here (filter by journal is not applicable here)
    return filters;
  }
}
