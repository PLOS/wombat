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

package org.ambraproject.wombat.service.remote;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.SetMultimap;
import org.ambraproject.wombat.config.TestSpringConfiguration;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.util.MockSiteUtil;
import org.apache.http.NameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@ContextConfiguration(classes = TestSpringConfiguration.class)
public class SolrSearchApiTest extends AbstractJUnit4SpringContextTests {

  @Autowired
  private SiteSet siteSet;

  private static List<NameValuePair> buildCommonParams(String query, boolean useDisMax, int start,
                                                       int rows, SolrSearchApi.SearchCriterion sortOrder,
                                                       boolean forHomePage) {
    ArticleSearchQuery asq = ArticleSearchQuery.builder()
        .setQuery(query)
        .setSimple(useDisMax)
        .setStart(start)
        .setRows(rows)
        .setSortOrder(sortOrder)
      .build();
    return SolrQueryBuilder.buildParameters(asq);
  }

  @Test
  public void testBuildCommonParams() {
    // query string not null
    List<NameValuePair> actual = buildCommonParams("foo", true, 0, 10, SolrSearchApiImpl.SolrSortOrder.MOST_CITED, true);
    SetMultimap<String, String> actualMap = convertToMap(actual);
    assertCommonParams(2, actualMap);
    assertSingle("10", actualMap.get("rows"));

    // if start == 0 no param should be present.
    assertEquals(0, actualMap.get("start").size());
    assertSingle("alm_scopusCiteCount desc,id desc", actualMap.get("sort"));
    assertSingle("dismax", actualMap.get("defType"));
    assertSingle("foo", actualMap.get("q"));

    // empty query string
    actual = buildCommonParams("*:*", false, 0, 10, SolrSearchApiImpl.SolrSortOrder.MOST_CITED, true);
    actualMap = convertToMap(actual);
    assertCommonParams(2, actualMap);
    assertEquals(0, actualMap.get("defType").size());
    assertSingle("*:*", actualMap.get("q"));

    // not home page
    actual = buildCommonParams("", false, 0, 10, SolrSearchApiImpl.SolrSortOrder.RELEVANCE, false);
    actualMap = convertToMap(actual);
    assertSingle("score desc,publication_date desc,id desc", actualMap.get("sort"));
    assertCommonParams(2, actualMap);

  }

  private List<NameValuePair> buildFacetParams(String facetField, String query, boolean useDisMax) {
    ArticleSearchQuery asq = ArticleSearchQuery.builder()
        .setFacet(facetField)
        .setQuery(query)
        .setSimple(useDisMax)
      .build();
    return SolrQueryBuilder.buildParameters(asq);
  }

  @Test
  public void testFacetParams() {
    // query string not null
    List<NameValuePair> actual = buildFacetParams("journal", "foo", true);
    SetMultimap<String, String> actualMap = convertToMap(actual);
    assertFacetParams(2, actualMap);
    assertSingle("journal", actualMap.get("facet.field"));
    assertSingle("dismax", actualMap.get("defType"));
    assertSingle("foo", actualMap.get("q"));

    // empty query string
    actual = buildFacetParams("journal", "*:*", false);
    actualMap = convertToMap(actual);
    assertFacetParams(2, actualMap);
    assertSingle("journal", actualMap.get("facet.field"));
    assertEquals(actualMap.get("defType").size(), 0);
    assertSingle("*:*", actualMap.get("q"));
  }

  private static void setQueryFilters(List<NameValuePair> params, List<String> journalKeys,
                                      List<String> articleTypes, List<String> subjects,
                                      SolrSearchApi.SearchCriterion dateRange) {
    ArticleSearchQuery asq = ArticleSearchQuery.builder()
        .setJournalKeys(journalKeys)
        .setArticleTypes(articleTypes)
        .setSubjects(subjects)
        .setDateRange(dateRange)
      .build();
    SolrQueryBuilder.setQueryFilters(asq, params);
  }

  @Test
  public void testSetQueryFilters() {
    List<NameValuePair> actual = new ArrayList<>();
    List<String> articleTypes = new ArrayList<>();
    List<String> subjects = new ArrayList<>();

    // Multiple journals
    setQueryFilters(actual, Arrays.asList("foo", "bar", "blaz"), articleTypes, subjects,
        SolrSearchApiImpl.SolrEnumeratedDateRange.ALL_TIME);
    SetMultimap<String, String> actualMap = convertToMap(actual);
    assertJournals(actualMap.get("fq"), "foo", "bar", "blaz");

    // date range
    actual = new ArrayList<>();
    setQueryFilters(actual, Collections.singletonList("foo"), articleTypes, subjects,
        SolrSearchApiImpl.SolrEnumeratedDateRange.LAST_3_MONTHS);
    actualMap = convertToMap(actual);
    assertEquals(2, actualMap.get("fq").size());
    for (String s : actualMap.get("fq")) {
      if (!s.contains("publication_date:") && !s.contains("journal_key:")) {
        fail(s);
      }
    }

    // null date range
    actual = new ArrayList<>();
    setQueryFilters(new ArrayList<NameValuePair>(), Collections.singletonList("foo"), articleTypes, subjects, null);
    actualMap = convertToMap(actual);
    Set<String> fq = actualMap.get("fq");
    for (String s : fq) {
      if (s.startsWith("publication_date:")) {
        fail(s);
      }
    }
  }

  @Test
  public void testSetQueryFilters_ExplicitDateRange() throws IOException {
    List<NameValuePair> actual = new ArrayList<>();
    SolrSearchApiImpl.SolrExplicitDateRange edr
        = new SolrSearchApiImpl.SolrExplicitDateRange("test", "2011-01-01", "2015-06-01");

    setQueryFilters(actual, Collections.singletonList("foo"), new ArrayList<String>(), new ArrayList<String>(), edr);
    SetMultimap<String, String> actualMap = convertToMap(actual);
    assertPubDate(actualMap.get("fq"));
    assertJournals(actualMap.get("fq"), "foo");
  }

  @Test
  public void testSetQueryFilters_IncludeArticleTypes() throws IOException {
    List<NameValuePair> actual = new ArrayList<>();
    SolrSearchApiImpl.SolrExplicitDateRange edr
        = new SolrSearchApiImpl.SolrExplicitDateRange("test", "2011-01-01", "2015-06-01");
    ArrayList<String> articleTypes = new ArrayList<>();
    articleTypes.add("Research Article");
    setQueryFilters(actual, Collections.singletonList("foo"), articleTypes, new ArrayList<String>(), edr);
    SetMultimap<String, String> actualMap = convertToMap(actual);
    assertPubDate(actualMap.get("fq"));
    assertJournals(actualMap.get("fq"), "foo");
    assertArticleTypes(actualMap.get("fq"));
  }

  @Test
  public void testSetQueryFilters_IncludeSubjects() throws IOException {
    List<NameValuePair> actual = new ArrayList<>();
    SolrSearchApiImpl.SolrExplicitDateRange edr
        = new SolrSearchApiImpl.SolrExplicitDateRange("test", "2011-01-01", "2015-06-01");
    ArrayList<String> articleTypes = new ArrayList<>();
    articleTypes.add("Research Article");
    setQueryFilters(actual, Collections.singletonList("foo"), articleTypes, Arrays.asList("Skull", "Head", "Teeth"), edr);
    SetMultimap<String, String> actualMap = convertToMap(actual);
    assertPubDate(actualMap.get("fq"));
    assertJournals(actualMap.get("fq"), "foo");
    assertArticleTypes(actualMap.get("fq"));
    assertSubjects(actualMap.get("fq"), "\"Skull\"", "\"Head\"", "\"Teeth\"");
  }

  private static class SearchApiForAddArticleLinksTest extends SolrSearchApiImpl {

    @Override
    protected void initializeEIssnToJournalKeyMap(SiteSet siteSet, Site currentSite) throws IOException {
      ImmutableMap.Builder<String, String> builder = new ImmutableMap.Builder<>();
      builder.put("123", "journal1Key")
          .put("456", "journal2Key")
          .put("789", "collectionJournalKey");
      eIssnToJournalKey = builder.build();
    }
  }

  @Test
  public void testAddArticleLinks() throws IOException {
    SolrSearchApi solrSearchApiForTest = new SearchApiForAddArticleLinksTest();
    Map<String, List<Map<String,String>>> searchResults = new HashMap<>();
    List<Map<String,String>> docs = new ArrayList<>(1);
    Map<String,String> doc = new HashMap<>();
    List<String> crossPubbedJournals = new ArrayList<>(1);
    crossPubbedJournals.add("journal1Key");
    doc.put("id", "12345");
    doc.put("eissn", "123");
    docs.add(doc);
    searchResults.put("docs", docs);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setContextPath("someContextPath");
    Site site = MockSiteUtil.getByUniqueJournalKey(siteSet, "journal2Key");

    Map<?, ?> actual = solrSearchApiForTest.addArticleLinks(searchResults, request, site, siteSet);
    List<Map> actualDocs = (List) actual.get("docs");
    assertEquals(1, actualDocs.size());
    Map actualDoc = (Map) actualDocs.get(0);
    assertEquals("12345", actualDoc.get("id"));
    assertTrue(actualDoc.get("link").toString().endsWith("someContextPath/site1/article?id=12345"));
  }

  @Test
  public void testBuildSearchClause() {
    assertEquals("subject:\"foo\"", SolrQueryBuilder.buildSearchClause("subject", Arrays.asList("foo")));
    assertEquals("subject:\"foo\" AND subject:\"2nd subject\"",
                 SolrQueryBuilder.buildSearchClause("subject", Arrays.asList("foo", "2nd subject")));
    assertEquals("author:\"author1\"", SolrQueryBuilder.buildSearchClause("author", Arrays.asList("author1")));
    assertEquals("author:\"author1\" AND author:\"author2\"",
                 SolrQueryBuilder.buildSearchClause("author", Arrays.asList("author1", "author2")));
  }

  /**
   * Converts a list of params to a multimap from key to value(s).
   */
  private SetMultimap<String, String> convertToMap(List<NameValuePair> nameValuePairs) {
    SetMultimap<String, String> map = HashMultimap.create();
    for (NameValuePair pair : nameValuePairs) {
      map.put(pair.getName(), pair.getValue());
    }
    return map;
  }

  /**
   * Tests for parameters expected to be the same across all tests.
   */
  private void assertCommonParams(int expectedNumFqParams, SetMultimap<String, String> actual) {
    assertSingle("json", actual.get("wt"));
    Set<String> fq = actual.get("fq");

    // We check two of the fq params here; the caller should check the others.
    Joiner joiner = Joiner.on(",");
    assertEquals(expectedNumFqParams, fq.size());
    assertTrue(fq.contains("doc_type:full"));
    assertTrue(fq.contains("!article_type_facet:\"Issue Image\""));
    assertSingle("false", actual.get("hl"));
    assertSingle("false", actual.get("facet"));
  }

  /**
   * Tests for parameters expected to be the same across all tests.
   */
  private void assertFacetParams(int expectedNumFqParams, SetMultimap<String, String> actual) {
    assertSingle("json", actual.get("wt"));
    assertSingle("map", actual.get("json.nl"));
    Set<String> fq = actual.get("fq");

    // We check two of the fq params here; the caller should check the others.
    assertEquals(expectedNumFqParams, fq.size());
    assertTrue(fq.contains("doc_type:full"));
    assertTrue(fq.contains("!article_type_facet:\"Issue Image\""));
    assertSingle("false", actual.get("hl"));
    assertSingle("true", actual.get("facet"));
  }

  /**
   * Asserts that the given set (all values of a single parameter) has exactly one entry with the expected value.
   */
  private void assertSingle(String expected, Set<String> multimapSet) {
    assertEquals(1, multimapSet.size());
    assertTrue(multimapSet.contains(expected));
  }

  // Regex used to validate the publication_date fq param.  This is not very strict, since we only
  // look for anything that appears to resemble a date range, without actually parsing the dates.

  private static final String TIMESTAMP_RE = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}\\:\\d{2}(\\.\\d+)?Z";

  private static final Pattern PUB_DATE_RE = Pattern.compile(String.format("publication_date:\\[%s TO %s\\]",
      TIMESTAMP_RE, TIMESTAMP_RE));

  private static final Pattern ARTICLE_TYPE_RE = Pattern.compile(String.format("article_type_facet:\"[A-z ]+\""));

  private void assertPubDate(Set<String> actualFq) {
    String pubDate = null;
    for (String s : actualFq) {
      if (s.startsWith("publication_date:")) {
        pubDate = s;
        break;
      }
    }
    assertNotNull(pubDate);
    Matcher matcher = PUB_DATE_RE.matcher(pubDate);
    assertTrue(matcher.matches());
  }

  private void assertJournals(Set<String> actualFq, String... expectedJournals) {
    String journals = null;
    for (String s : actualFq) {
      if (s.startsWith("journal_key:")) {
        journals = s;
        break;
      }
    }
    assertNotNull(journals);

    // For multiple journals, the expected format of the param is
    // "journal_key:PLoSBiology OR journal_key:PLoSONE"
    String[] parts = journals.split(" OR ");
    assertEquals(expectedJournals.length, parts.length);
    Set<String> actualJournals = new HashSet<>();
    for (String part : parts) {
      actualJournals.add(part.substring("journal_key:".length()));
    }
    for (String expected : expectedJournals) {
      assertTrue(actualJournals.contains(expected));
    }
  }

  private void assertArticleTypes(Set<String> actualFq) {
    String articleType = null;
    for (String s : actualFq) {
      if (s.startsWith("article_type_facet:")) {
        articleType = s;
        break;
      }
    }
    assertNotNull(articleType);
    Matcher matcher = ARTICLE_TYPE_RE.matcher(articleType);
    assertTrue(matcher.matches());
  }

  private void assertSubjects(Set<String> actualFq, String... expectedSubjects) {
    String subjects = null;
    for (String s : actualFq) {
      if (s.startsWith("subject:")) {
        subjects = s;
        break;
      }
    }

    assertNotNull(subjects);
    // For multiple subjects, the expected format of the param is
    // "subject:Teeth AND subject:Head"
    String[] parts = subjects.split(" AND ");
    assertEquals(expectedSubjects.length, parts.length);
    Set<String> actualSubjects = new HashSet<>();
    for (String part : parts) {
      actualSubjects.add(part.substring("subject:".length()));
    }
    for (String expected : expectedSubjects) {
      assertTrue(actualSubjects.contains(expected));
    }
  }
}
