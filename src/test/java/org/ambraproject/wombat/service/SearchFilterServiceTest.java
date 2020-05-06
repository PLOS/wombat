package org.ambraproject.wombat.service;

import com.google.common.collect.ImmutableMap;
import org.ambraproject.wombat.model.SearchFilter;
import org.ambraproject.wombat.model.SearchFilterFactory;
import org.ambraproject.wombat.service.remote.ArticleSearchQuery;
import org.ambraproject.wombat.service.remote.SolrSearchApi;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {SearchFilterServiceTest.class})
public class SearchFilterServiceTest extends AbstractJUnit4SpringContextTests {

  @Mock
  private SolrSearchApi solrSearchApi;

  @Mock
  private SearchFilterFactory searchFilterFactory;

  @InjectMocks
  private SearchFilterService searchFilterService;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testGetSearchFilters() throws IOException {
    SearchFilter mockFilter = mock(SearchFilter.class);
    when(searchFilterFactory.createSearchFilter(any(), any())).thenReturn(mockFilter);
    ArticleSearchQuery query = ArticleSearchQuery.builder()
        .setQuery("blah")
        .setSimple(true)
        .build();
    SolrSearchApi.Result result = mock(SolrSearchApi.Result.class);
    Map<String, Map<String, Integer>> facetMap = mock(Map.class);
    Optional<Map<String, Map<String, Integer>>> facets = Optional.of(facetMap);
    when(result.getFacets()).thenReturn(facets);
    when(facetMap.get(any())).thenReturn(ImmutableMap.of());
    
    when(solrSearchApi.search(any())).thenReturn(result);

    ImmutableMap<String, SearchFilter> expected = ImmutableMap.of(
        "subject_area", mockFilter,
        "journal", mockFilter,
        "article_type", mockFilter,
        "author", mockFilter,
        "section", mockFilter);

    Map<String, SearchFilter> searchFilters = searchFilterService.getSearchFilters(query);
    assertEquals(expected, searchFilters);
  }

}
