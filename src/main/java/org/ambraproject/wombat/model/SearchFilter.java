package org.ambraproject.wombat.model;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchFilter {

  @Autowired
  private SearchFilterTypeMap filterTypeMap;

  private List<SearchFilterItem> searchFilterResult;

  public SearchFilter parseFacetedSearchResult(Map<?, ?> results, String filterTypeMapKey) {

    SearchFilterType filterType = filterTypeMap.getSearchFilterByKey(filterTypeMapKey);

    searchFilterResult = new ArrayList<>();

    for (Object filterValue : results.keySet()) {
      Double numberOfHits = (Double) results.get(filterValue);

      String displayName = filterValue.toString();
      //displayName may represent the filter value as well
      searchFilterResult.add(new SearchFilterItem(displayName, numberOfHits.floatValue(),
          filterType.getParameterName(), filterType.getFilterValue(displayName)));
    }
    return this;
  }

  public List<SearchFilterItem> getSearchFilterResult() {
    return searchFilterResult;
  }
}