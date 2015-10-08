package org.ambraproject.wombat.model;

import java.util.List;
import java.util.Map;

public class SearchFilterItem {

  String displayName;
  float numberOfHits;
  String filterParamName;
  String filterValue;
  Map<String, List<String>> filteredResultsParameters;

  public SearchFilterItem(String displayName, float numberOfHits, String filterParamName,
      String filterValue, Map<String, List<String>> filteredResultsParameters) {
    this.displayName = displayName;
    this.numberOfHits = numberOfHits;
    this.filterParamName = filterParamName;
    this.filterValue = filterValue;
    this.filteredResultsParameters = filteredResultsParameters;
  }

  public String getDisplayName() {
    return displayName;
  }

  public float getNumberOfHits() {
    return numberOfHits;
  }

  public String getFilterParamName() {
    return filterParamName;
  }

  public String getFilterValue() {
    return filterValue;
  }

  public Map<String, List<String>> getFilteredResultsParameters() {
    return filteredResultsParameters;
  }
}
