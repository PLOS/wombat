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

package org.ambraproject.wombat.controller;

import org.ambraproject.wombat.config.TestSpringConfiguration;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.service.remote.ArticleSearchQuery;
import org.ambraproject.wombat.service.remote.SolrSearchApi;
import org.ambraproject.wombat.util.MockSiteUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(classes = TestSpringConfiguration.class)
public class SearchControllerTest extends AbstractJUnit4SpringContextTests {
  @Autowired
  private SiteSet siteSet;

  @Test
  public void testCommonParams() throws IOException {
    Map<String, List<String>> params = new HashMap<>();
    params.put("page", ImmutableList.of("7"));
    params.put("filterSubjects", Arrays.asList("subject1", "subject2"));

    Site site = MockSiteUtil.getByUniqueJournalKey(siteSet, "journal1Key");
    CommonParams commonParams = new CommonParams(siteSet, site);
    commonParams.parseParams(params);

    assertEquals(90, commonParams.start);  // Default results per page should be 15
    assertEquals(ArticleSearchQuery.SolrSortOrder.RELEVANCE, commonParams.sortOrder);
    assertEquals(ArticleSearchQuery.SolrEnumeratedDateRange.ALL_TIME, commonParams.dateRange);
    assertTrue(commonParams.articleTypes.isEmpty());
    assertTrue(commonParams.journalKeys.isEmpty());
    assertTrue(commonParams.filterJournalNames.isEmpty());
    assertEquals(2, commonParams.subjectList.size());
    assertEquals("subject1", commonParams.subjectList.get(0));
    assertEquals("subject2", commonParams.subjectList.get(1));
    assertTrue(commonParams.isFiltered);
  }

  private class MySearchController extends SearchController {

    protected void initializeEIssnToJournalKeyMap(SiteSet siteSet, Site currentSite)
        throws IOException {
      ImmutableMap.Builder<String, String> builder = new ImmutableMap.Builder<>();
      builder.put("123", "journal1Key").put("456", "journal2Key").put("789",
          "collectionJournalKey");
      this.eIssnToJournalKey = builder.build();
    }
  }    
  @Test
  public void testAddArticleLinks() throws IOException {
    SearchController searchController = new MySearchController();
    List<Map<String,Object>> docs = new ArrayList<>(1);
    Map<String,Object> doc = new HashMap<>();
    List<String> crossPubbedJournals = new ArrayList<>(1);
    crossPubbedJournals.add("journal1Key");
    doc.put("id", "12345");
    doc.put("eissn", "123");
    docs.add(doc);
    SolrSearchApi.Result searchResults =
      SolrSearchApi.Result.builder()
      .setDocs(docs)
      .setNumFound(1)
      .setStart(0)
      .build();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setContextPath("someContextPath");
    Site site = MockSiteUtil.getByUniqueJournalKey(siteSet, "journal2Key");

    List<Map<String, Object>> actualDocs = searchController.addArticleLinks(searchResults, request, site, siteSet).getDocs();
    assertEquals(1, actualDocs.size());
    Map<String, Object> actualDoc = actualDocs.get(0);
    assertEquals("12345", actualDoc.get("id"));
    assertTrue(actualDoc.get("link").toString().endsWith("someContextPath/site1/article?id=12345"));
  }
}
