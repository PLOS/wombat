package org.ambraproject.wombat.model;

import com.google.common.collect.ImmutableList;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SearchFilter {

  private final ImmutableList<SearchFilterItem> searchFilterResult;

  private Set<SearchFilterItem> activeFilterItems;

  private Set<SearchFilterItem> inactiveFilterItems;

  private final String filterTypeMapKey;

  public SearchFilter(List<SearchFilterItem> searchFilterResult, String filterTypeMapKey) {
    this.searchFilterResult = ImmutableList.copyOf(searchFilterResult);
    this.filterTypeMapKey = filterTypeMapKey;
  }

  public List<SearchFilterItem> getSearchFilterResult() {
    return searchFilterResult;
  }

  public String getFilterTypeMapKey() {
    return filterTypeMapKey;
  }

  public Set<SearchFilterItem> getActiveFilterItems() {
    return activeFilterItems;
  }

  public Set<SearchFilterItem> getInactiveFilterItems() {
    return inactiveFilterItems;
  }

  public void setActiveAndInactiveFilterItems(List<String> filterDisplayNames) {
    this.activeFilterItems = getSearchFilterResult().stream()
        .filter((SearchFilterItem filterItem) -> isFilterItemActive(filterDisplayNames, filterItem))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    this.inactiveFilterItems = getSearchFilterResult().stream()
        .filter((SearchFilterItem filterItem) -> isFilterItemInactive(filterDisplayNames, filterItem))
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  private boolean isFilterItemActive(List<String> filterDisplayNames,
      SearchFilterItem searchFilterItem) {
    return filterDisplayNames.stream()
        .anyMatch(filterDisplayName ->
            filterDisplayName.equalsIgnoreCase(searchFilterItem.getFilterValue()));
  }

  private boolean isFilterItemInactive(List<String> filterDisplayNames,
      SearchFilterItem searchFilterItem) {
    return filterDisplayNames.stream()
        .noneMatch(filterDisplayName ->
            filterDisplayName.equalsIgnoreCase(searchFilterItem.getFilterValue()));
  }
}