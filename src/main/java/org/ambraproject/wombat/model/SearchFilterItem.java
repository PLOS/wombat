package org.ambraproject.wombat.model;

public class SearchFilterItem {

  String displayName;
  float numberOfHits;
  String filterParamName;
  String filterValue;

  public SearchFilterItem(String displayName, float numberOfHits, String filterParamName, String filterValue) {
    this.displayName = displayName;
    this.numberOfHits = numberOfHits;
    this.filterParamName = filterParamName;
    this.filterValue = filterValue;
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

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public void setNumberOfHits(float numberOfHits) {
    this.numberOfHits = numberOfHits;
  }

  public void setFilterParamName(String filterParamName) {
    this.filterParamName = filterParamName;
  }

  public void setFilterValue(String filterValue) {
    this.filterValue = filterValue;
  }
}
