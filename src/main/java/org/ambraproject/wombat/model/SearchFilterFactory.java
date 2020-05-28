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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is used exclusively in the @code{SearchFilterService} to create @code{SearchFilter}s
 * from faceted search results returned from Solr.
 */
public class SearchFilterFactory {

  @Autowired
  @Qualifier("searchFilters")
  private Map<String,SearchFilterType> filterTypeMap;

  /**
   * The main create method of the factory. Creates a @code{SearchFilter} in three steps:
   *
   * 1. Retrieve the specified @code{SearchFilterType}
   * 2. Parse the results from a faceted search into individual @code{SearchFilterItem}s
   * 3. Combine all SearchFilterItems into a @code{SearchFilter} object.
   *
   * Note that the @code{ArticleSearchQuery} query must be set as a faceted search
   * by calling addFacet()
   *
   * @param results faceted search results returned from Solr
   * @param filterTypeMapKey key for the filter type
   * @param params URL parameters applicable to the search
   * @return @code{SearchFilter} object made up of one or many @code{SearchFilterItems} that contain
   * the faceted search results. The SearchFilterItems also house a /search URL that represents
   * how the filter would be applied or removed from a search.
   */
  public SearchFilter createSearchFilter(Map<String, Integer> results, String filterTypeMapKey) {

    SearchFilterType filterType = filterTypeMap.get(filterTypeMapKey);

    List<SearchFilterItem> searchFilterResult = new ArrayList<>();

    for (Map.Entry<String, Integer> entry : results.entrySet()) {

      Integer numberOfHits = entry.getValue();

      //displayName is often represented by the filter value
      String displayName = entry.getKey();

      SearchFilterItem filterItem = SearchFilterItem.builder()
        .setDisplayName(displayName)
        .setNumberOfHits(numberOfHits)
        .setFilterParamName(filterType.getParameterName())
        .setFilterValue(filterType.getFilterValue(displayName))
        .build();

      searchFilterResult.add(filterItem);
    }
    return new SearchFilter(searchFilterResult, filterTypeMapKey);
  }
}
