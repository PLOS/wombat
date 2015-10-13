package org.ambraproject.wombat.model;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * This class is is a wrapper for @code{Map<String, SearchFilterType>}. The map is populated as a
 * bean in @code{SpringConfiguration}.
 */
public class SearchFilterTypeMap {
  private final Map<String, SearchFilterType> map;

  public SearchFilterTypeMap(Map<String, SearchFilterType> map) {
    this.map = ImmutableMap.copyOf(map);
  }

  public SearchFilterType getSearchFilterByKey(String key) {
    return map.get(key);
  }
}
