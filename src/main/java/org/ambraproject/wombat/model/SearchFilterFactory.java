package org.ambraproject.wombat.model;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is used exclusively in the @code{SearchFilterService} to create @code{SearchFilter}s
 * from faceted search results returned from Solr.
 */
public class SearchFilterFactory {

  @Autowired
  private SearchFilterTypeMap filterTypeMap;

  /**
   * The main create method of the factory. Creates a @code{SearchFilter} in three steps:
   *
   * 1. Retrieve the specified @code{SearchFilterType} using the @code{SearchFilterTypeMap}
   * 2. Parse the results from a faceted search into individual @code{SearchFilterItem}s
   * 3. Combine all SearchFilterItems into a @code{SearchFilter} object.
   *
   * Note that the @code{ArticleSearchQuery.Builder} query must be set as a faceted search
   * by calling setFacet()
   *
   * Note that no search filter items with a name containing "collections" will be included
   *
   * @param results faceted search results returned from Solr
   * @param filterTypeMapKey key for the filter type
   * @param params URL parameters applicable to the search
   * @return @code{SearchFilter} object made up of one or many @code{SearchFilterItems} that contain
   * the faceted search results. The SearchFilterItems also house a /search URL that represents
   * how the filter would be applied or removed from a search.
   */
  public SearchFilter createSearchFilter(Map<?, ?> results, String filterTypeMapKey,
      Multimap<String, String> params) {

    SearchFilterType filterType = filterTypeMap.getSearchFilterByKey(filterTypeMapKey);

    List<SearchFilterItem> searchFilterResult = new ArrayList<>();

    for (Map.Entry<?, ?> entry : results.entrySet()) {

      Double numberOfHits = (Double) entry.getValue();

      //displayName is often represented by the filter value
      String displayName = entry.getKey().toString();

      //todo: don't hardcode this
      if(displayName.toLowerCase().contains("collections")) {
        continue;
      }

      ListMultimap<String, String> changedParams = applyFilterToParams(displayName, params,
          filterType);
      SearchFilterItem filterItem = new SearchFilterItem(displayName, numberOfHits.floatValue(),
          filterType.getParameterName(), filterType.getFilterValue(displayName),
          Multimaps.asMap(changedParams));
      searchFilterResult.add(filterItem);
    }
    return new SearchFilter(searchFilterResult);
  }

  /**
   * Examines the current URL parameters, and toggles the selected parameter.
   *
   * @param displayName used to retrieve the filter value from the filter type map
   * @param params current URL parameters to be modified
   * @param filterType used to retrieve selected filter parameter name and value
   * @return filtered URL parameter List
   */
  private ListMultimap<String, String> applyFilterToParams(String displayName,
      Multimap<String, String> params, SearchFilterType filterType) {
    String filterValue = filterType.getFilterValue(displayName);
    String parameterName = filterType.getParameterName();

    ListMultimap<String, String> changedParams = LinkedListMultimap.create(params);
    if (params.containsEntry(parameterName, filterValue)) {
      changedParams.remove(parameterName, filterValue);
    } else {
      changedParams.put(parameterName, filterValue);
    }
    return changedParams;
  }

}
