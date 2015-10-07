package org.ambraproject.wombat.model;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchFilterFactory {

  @Autowired
  private SearchFilterTypeMap filterTypeMap;

  public SearchFilter parseFacetedSearchResult(Map<?, ?> results, String filterTypeMapKey) {

    SearchFilterType filterType = filterTypeMap.getSearchFilterByKey(filterTypeMapKey);

    List<SearchFilterItem> searchFilterResult = new ArrayList<>();

    for (Object filterValue : results.keySet()) {
      Double numberOfHits = (Double) results.get(filterValue);

      String displayName = filterValue.toString();
      //displayName may represent the filter value as well
      searchFilterResult.add(new SearchFilterItem(displayName, numberOfHits.floatValue(),
          filterType.getParameterName(), filterType.getFilterValue(displayName)));
    }
    return new SearchFilter(searchFilterResult);
  }

}
