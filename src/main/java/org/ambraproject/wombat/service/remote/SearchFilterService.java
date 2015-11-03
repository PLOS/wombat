package org.ambraproject.wombat.service.remote;

import com.google.common.collect.Multimap;
import org.ambraproject.wombat.model.SearchFilter;
import org.ambraproject.wombat.model.SearchFilterFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Responsible for performing faceted search on different fields used for filtering search results
 * Note that the Date Filter is a special case and need not be added here
 */
public class SearchFilterService {

  @Autowired
  private SolrSearchService solrSearchService;

  @Autowired
  private SearchFilterFactory searchFilterFactory;

  private final String JOURNAL = "journal";

  private final String JOURNAL_FACET_FIELD = "cross_published_journal_name";

  private final String SUBJECT_AREA = "subject_area";

  private final String SUBJECT_AREA_FACET_FIELD = "subject_facet";

  private final String ARTICLE_TYPE = "article_type";

  private final String ARTICLE_TYPE_FACET = "article_type_facet";

  /**
   * Retrieves a map of search filters to be added to the model. The filters displayed will change
   * depending on the query executed, but the number and type of filters is constant.
   *
   * @param query Execute query to determine the search filter results.
   *              Must be set as faceted with the setFacet() method
   * @param urlParams search URL parameters that have been rebuilt from the ArticleSearchQuery object
   * @return HashMap containing all applicable filters
   * @throws IOException
   */
  public Map<?, ?> getSearchFilters(ArticleSearchQuery query, Multimap<String, String> urlParams)
      throws IOException {

    ArticleSearchQuery.Builder journalFacetQuery = ArticleSearchQuery.builder()
        .setFacet(JOURNAL_FACET_FIELD)
        .setQuery(query.getQuery().orNull())
        .setSimple(query.isSimple())
        .setArticleTypes(query.getArticleTypes())
        .setSubjects(query.getSubjects())
        .setDateRange(query.getDateRange().orNull());

    Map<?, ?> journalFacetResults = solrSearchService.search(journalFacetQuery.build());
    SearchFilter journalFilter = searchFilterFactory
        .createSearchFilter(journalFacetResults, JOURNAL, urlParams);

    ArticleSearchQuery.Builder subjectAreaFacetQuery = ArticleSearchQuery.builder()
        .setFacet(SUBJECT_AREA_FACET_FIELD)
        .setQuery(query.getQuery().orNull())
        .setSimple(query.isSimple())
        .setArticleTypes(query.getArticleTypes())
        .setDateRange(query.getDateRange().orNull())
        .setJournalKeys(query.getJournalKeys());

    Map<?, ?> subjectAreaFacetResults = solrSearchService.search(subjectAreaFacetQuery.build());
    SearchFilter subjectAreaFilter = searchFilterFactory
        .createSearchFilter(subjectAreaFacetResults, SUBJECT_AREA, urlParams);

    ArticleSearchQuery.Builder articleTypeFacetQuery = ArticleSearchQuery.builder()
        .setFacet(ARTICLE_TYPE_FACET)
        .setQuery(query.getQuery().orNull())
        .setSimple(query.isSimple())
        .setDateRange(query.getDateRange().orNull())
        .setJournalKeys(query.getJournalKeys())
        .setSubjects(query.getSubjects());

    Map<?, ?> articleTypeFacetResults = solrSearchService.search(articleTypeFacetQuery.build());
    SearchFilter articleTypeFilter = searchFilterFactory.createSearchFilter(articleTypeFacetResults,
        ARTICLE_TYPE, urlParams);

    // TODO: add other filters here

    Map<String, SearchFilter> filters = new HashMap<>();
    filters.put(JOURNAL, journalFilter);
    filters.put(SUBJECT_AREA, subjectAreaFilter);
    filters.put(ARTICLE_TYPE, articleTypeFilter);

    return filters;
  }

  public Map<?, ?> getVolumeSearchFilters(int volume, List<String> journalKeys, List<String> articleTypes,
      SolrSearchService.SearchCriterion dateRange) throws IOException {
    Map<String, SearchFilter> filters = new HashMap<>();
    // TODO: add other filters here (filter by journal is not applicable here)
    return filters;
  }
}
