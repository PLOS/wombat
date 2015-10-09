package org.ambraproject.wombat.model;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchFilterFactory {

  @Autowired
  private SearchFilterTypeMap filterTypeMap;

  public SearchFilter parseFacetedSearchResult(Map<?, ?> results, String filterTypeMapKey,
      Multimap<String, String> params) {

    SearchFilterType filterType = filterTypeMap.getSearchFilterByKey(filterTypeMapKey);

    List<SearchFilterItem> searchFilterResult = new ArrayList<>();

    for (Map.Entry<?, ?> entry : results.entrySet()) {

      Double numberOfHits = (Double) entry.getValue();

      String displayName = entry.getKey().toString();
      //displayName may represent the filter value as well

      ListMultimap<String, String> changedParams = applyFilterToParams(displayName, params,
          filterType);
      SearchFilterItem filterItem = new SearchFilterItem(displayName, numberOfHits.floatValue(),
          filterType.getParameterName(), filterType.getFilterValue(displayName),
          Multimaps.asMap(changedParams));
      searchFilterResult.add(filterItem);
    }
    return new SearchFilter(searchFilterResult);
  }

  private ListMultimap<String, String> applyFilterToParams(String displayName,
      Multimap<String, String> params, SearchFilterType filterType) {
    String filterValue = filterType.getFilterValue(displayName);
    String parameterName = filterType.getParameterName();

    ListMultimap<String, String> changedParams = LinkedListMultimap.create(params);
    if (params.containsEntry(parameterName, filterValue)) {
      changedParams.remove(parameterName, filterValue);
    } else {
      changedParams.put(parameterName, filterValue);
    }
    return changedParams;
  }

}
