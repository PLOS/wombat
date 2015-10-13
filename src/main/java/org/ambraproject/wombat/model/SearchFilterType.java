package org.ambraproject.wombat.model;

/**
 * Implement this interface for new search filters. Static filters that do not require
 * advanced processing should be extended in @code{SingletonSearchFilterEnum}.
 * Advanced filters should be implemented in their own class. See @code{JournalFilterType}
 */
public interface SearchFilterType {
    String getFilterValue(String value);
    String getParameterName();
  }

