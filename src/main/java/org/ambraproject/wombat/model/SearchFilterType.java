package org.ambraproject.wombat.model;

public interface SearchFilterType {
    String getFilterValue(String value);
    String getParameterName();
  }

