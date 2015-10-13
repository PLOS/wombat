package org.ambraproject.wombat.model;

public enum SingletonSearchFilterType implements SearchFilterType {
  //todo: add enum entries for singleton filter types such as author and dates
  example {
    @Override
    public String getFilterValue(String value) {
      return null;
    }

    @Override
    public String getParameterName() {
      return null;
    }
  }
}
