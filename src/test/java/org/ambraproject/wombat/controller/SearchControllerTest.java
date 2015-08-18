package org.ambraproject.wombat.controller;

import org.ambraproject.wombat.config.TestSpringConfiguration;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.service.remote.SolrSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = TestSpringConfiguration.class)
public class SearchControllerTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private SiteSet siteSet;

  @Test
  public void testCommonParams() throws IOException {
    Map<String, List<String>> params = new HashMap<>();
    params.put("page", Collections.singletonList("7"));
    params.put("filterSubjects", Arrays.asList("subject1", "subject2"));

    Site site = siteSet.getSites("journal1Key").get(0);
    SearchController.CommonParams commonParams = new SearchController.CommonParams(siteSet, site);
    commonParams.parseParams(params);

    assertEquals(commonParams.start, 90);  // Default results per page should be 15
    assertEquals(commonParams.sortOrder, SolrSearchService.SolrSortOrder.RELEVANCE);
    assertEquals(commonParams.dateRange, SolrSearchService.SolrEnumeratedDateRange.ALL_TIME);
    assertTrue(commonParams.articleTypes.isEmpty());
    assertEquals(commonParams.journalKeys.size(), 1);
    assertEquals(commonParams.journalKeys.get(0), "journal1Key");
    assertEquals(commonParams.filterJournalNames.size(), 1);
    for (String journalName : commonParams.filterJournalNames) {
      assertEquals(journalName, "Journal One");
    }
    assertEquals(commonParams.subjectList.size(), 2);
    assertEquals(commonParams.subjectList.get(0), "subject1");
    assertEquals(commonParams.subjectList.get(1), "subject2");
    assertTrue(commonParams.isFiltered);
  }
}
