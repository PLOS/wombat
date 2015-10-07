package org.ambraproject.wombat.model;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class SearchFilter {

  private final ImmutableList<SearchFilterItem> searchFilterResult;

  public SearchFilter(List<SearchFilterItem> searchFilterResult) {
    this.searchFilterResult = ImmutableList.copyOf(searchFilterResult);
  }

  public List<SearchFilterItem> getSearchFilterResult() {
    return searchFilterResult;
  }
}