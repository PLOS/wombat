package org.ambraproject.wombat.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import org.ambraproject.wombat.service.ArticleResolutionServiceTest;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;


@ContextConfiguration(classes = {ArticleResolutionServiceTest.class})
public class SearchFilterFactoryTest extends AbstractJUnit4SpringContextTests {

  @Mock
  private SearchFilterTypeMap filterTypeMap;

  @InjectMocks
  private SearchFilterFactory searchFilterFactory;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testCreateSearchFilter() {
    ImmutableListMultimap.Builder<String, String> builder = ImmutableListMultimap.builder();
    builder.put("q", "reward");
    builder.put("resultsPerPage", "15");
    builder.put("page", "1");
    builder.putAll("filterJournals", ImmutableList.of("PlosCompBiol"));
    builder.putAll("filterStartDate", "");
    builder.putAll("filterEndDate", "");
    ImmutableListMultimap<String, String> params = builder.build();

    JournalFilterType filterType = spy(JournalFilterType.class);
    doReturn("PLoSONE").when(filterType).getFilterValue("PLOS ONE");
    doReturn("PLoSCompBiol").when(filterType).getFilterValue("PLOS Computational Biology");
    when(filterTypeMap.getSearchFilterByKey("journal")).thenReturn(filterType);

    ImmutableMap<String, Integer> results = ImmutableMap.of("PLOS ONE", 19, "PLOS Computational Biology", 412);
    String filterTypeMapKey = "journal";
    SearchFilter searchFilter = searchFilterFactory.createSearchFilter(results, filterTypeMapKey, params);

    assertEquals(searchFilter.getFilterTypeMapKey(), "journal");
    assertEquals(searchFilter.getSearchFilterResult().get(0).getDisplayName(), "PLOS ONE");
    assertEquals(new Float(searchFilter.getSearchFilterResult().get(0).getNumberOfHits()), new Float(19.0f));
    assertEquals(searchFilter.getSearchFilterResult().get(1).getDisplayName(), "PLOS Computational Biology");
    assertEquals(new Float(searchFilter.getSearchFilterResult().get(1).getNumberOfHits()), new Float(412.0f));
  }
}
