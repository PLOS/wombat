package org.ambraproject.wombat.service.remote;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchFilterService {

  @Autowired
  private SearchService searchService;

  private Map<String, Map<?, ?>> filters;

  private final String JOURNAL_FACET_FIELD = "cross_published_journal_name";

  public Map<?,?> getSimpleSearchFilters(String query, List<String> journalKeys, List<String> articleTypes,
      SearchService.SearchCriterion dateRange) throws IOException {
    filters = new HashMap<>();
    filters.put("journal", searchService.simpleSearch(JOURNAL_FACET_FIELD, query, null, articleTypes,
        dateRange));
    // TODO: add other filters here
    return filters;
  }

  public Map<?, ?> getAdvancedSearchFilers(String query, List<String> journalKeys,
      List<String> articleTypes, List<String> subjectList, SearchService.SearchCriterion dateRange) throws
      IOException {
    filters = new HashMap<>();
    filters.put("journal", searchService.advancedSearch(JOURNAL_FACET_FIELD, query, null, articleTypes,
        subjectList, dateRange));
    // TODO: add other filters here
    return filters;
  }

  public Map<?, ?> getSubjectSearchFilters(List<String> subjects, List<String> journalKeys,
      List<String> articleTypes, SearchService.SearchCriterion dateRange) throws IOException {
    filters = new HashMap<>();
    filters.put("journal", searchService.subjectSearch(JOURNAL_FACET_FIELD, subjects, null, articleTypes,
        dateRange));
    // TODO: add other filters here
    return filters;
  }

  public Map<?, ?> getAuthorSearchFilters(String author, List<String> journalKeys,
      List<String> articleTypes, SearchService.SearchCriterion dateRange) throws IOException {
    filters = new HashMap<>();
    filters.put("journal", searchService.authorSearch(JOURNAL_FACET_FIELD, author, null, articleTypes,
        dateRange));
    // TODO: add other filters here
    return filters;
  }

  public Map<?, ?> getVolumeSearchFilters(int volume, List<String> journalKeys, List<String> articleTypes,
      SearchService.SearchCriterion dateRange) throws IOException {
    filters = new HashMap<>();
    filters.put("journal", searchService.volumeSearch(JOURNAL_FACET_FIELD, volume, null, articleTypes,
        dateRange));
    // TODO: add other filters here
    return filters;
  }


}
