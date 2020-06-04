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
import java.util.Map;


@ContextConfiguration(classes = {ArticleResolutionServiceTest.class})
public class SearchFilterFactoryTest extends AbstractJUnit4SpringContextTests {

  @Mock
  private Map<String,SearchFilterType> filterTypeMap;

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

    JournalFilterType filterType = spy(JournalFilterType.class);
    doReturn("PLoSONE").when(filterType).getFilterValue("PLOS ONE");
    doReturn("PLoSCompBiol").when(filterType).getFilterValue("PLOS Computational Biology");
    when(filterTypeMap.get("journal")).thenReturn(filterType);

    ImmutableMap<String, Integer> results = ImmutableMap.of("PLOS ONE", 19, "PLOS Computational Biology", 412);
    String filterTypeMapKey = "journal";
    SearchFilter searchFilter = searchFilterFactory.createSearchFilter(results, filterTypeMapKey);
    searchFilter.setActiveAndInactiveFilterItems(ImmutableList.of("plosone"));
    assertEquals(searchFilter.getFilterTypeMapKey(), "journal");
    SearchFilterItem inactive = searchFilter.getInactiveFilterItems().iterator().next();
    SearchFilterItem active = searchFilter.getActiveFilterItems().iterator().next();
    assertEquals(active.getDisplayName(), "PLOS ONE");
    assertEquals(active.getNumberOfHits(), 19);
    assertEquals(inactive.getDisplayName(), "PLOS Computational Biology");
    assertEquals(inactive.getNumberOfHits(), 412);
  }
}
