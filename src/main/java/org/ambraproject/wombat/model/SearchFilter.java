/*
 * Copyright (c) 2017 Public Library of Science
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package org.ambraproject.wombat.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SearchFilter {

  private final transient ImmutableList<SearchFilterItem> searchFilterResult;

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
    Map<Boolean, List<SearchFilterItem>> results =
        this.searchFilterResult.stream().collect(Collectors.partitioningBy(
            filterItem -> isFilterItemActive(filterDisplayNames, filterItem)));
    this.activeFilterItems = ImmutableSet.copyOf(results.get(true));
    this.inactiveFilterItems = ImmutableSet.copyOf(results.get(false));
  }

  private boolean isFilterItemActive(List<String> filterDisplayNames,
                                     SearchFilterItem searchFilterItem) {
    return filterDisplayNames.stream().anyMatch(
        filterDisplayName
        -> filterDisplayName.equalsIgnoreCase(
            searchFilterItem.getFilterValue()));
  }
}
