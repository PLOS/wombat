package org.ambraproject.wombat.model;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SearchFilter {

  private final ImmutableList<SearchFilterItem> searchFilterResult;

  public SearchFilter(List<SearchFilterItem> searchFilterResult) {
    this.searchFilterResult = ImmutableList.copyOf(searchFilterResult);
  }

  public List<SearchFilterItem> getSearchFilterResult() {
    return searchFilterResult;
  }

  public List<SearchFilterItem> getActiveFilterItems(List<String> filterDisplayNames) {
    List<SearchFilterItem> activeFilters = new ArrayList<>();
    for (SearchFilterItem item : getSearchFilterResult()) {
      activeFilters.addAll(filterDisplayNames.stream()
          .filter(filterValue -> filterValue.equalsIgnoreCase(item.getFilterValue()))
          .map(filterValue -> item)
          .collect(Collectors.toList()));
    }
    return activeFilters;
  }
}