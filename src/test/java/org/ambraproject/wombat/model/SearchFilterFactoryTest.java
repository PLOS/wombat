package org.ambraproject.wombat.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import org.ambraproject.wombat.service.ArticleResolutionServiceTest;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;


@ContextConfiguration(classes = {ArticleResolutionServiceTest.class})
public class SearchFilterFactoryTest extends AbstractTestNGSpringContextTests {

  @Mock
  private SearchFilterTypeMap filterTypeMap;

  @InjectMocks
  private SearchFilterFactory searchFilterFactory;

  @BeforeMethod
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

    ImmutableMap<String, Double> results = ImmutableMap.of("PLOS ONE", 19.0d, "PLOS Computational Biology", 412.0d);
    String filterTypeMapKey = "journal";
    SearchFilter searchFilter = searchFilterFactory.createSearchFilter(results, filterTypeMapKey, params);

    assertEquals("journal", searchFilter.getFilterTypeMapKey());
    assertEquals("PLOS ONE", searchFilter.getSearchFilterResult().get(0).getDisplayName());
    assertEquals(new Float(19.0f), new Float(searchFilter.getSearchFilterResult().get(0).getNumberOfHits()));
    assertEquals("PLOS Computational Biology", searchFilter.getSearchFilterResult().get(1).getDisplayName());
    assertEquals(new Float(412.0f), new Float(searchFilter.getSearchFilterResult().get(1).getNumberOfHits()));
  }
}