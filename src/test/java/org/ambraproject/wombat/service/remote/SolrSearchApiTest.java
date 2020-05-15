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

import static org.ambraproject.wombat.util.FileUtils.read;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.SetMultimap;
import com.google.gson.Gson;
import org.ambraproject.wombat.config.TestSpringConfiguration;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.util.MockSiteUtil;
import org.ambraproject.wombat.util.UrlParamBuilder;
import org.apache.http.NameValuePair;
import java.time.LocalDateTime;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration(classes = TestSpringConfiguration.class)
public class SolrSearchApiTest extends AbstractJUnit4SpringContextTests {

  @Autowired
  private SiteSet siteSet;

  @Autowired
  private Gson gson;

  private static List<NameValuePair> buildCommonParams(String query, boolean useDisMax, int start,
                                                       int rows, ArticleSearchQuery.SearchCriterion sortOrder,
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
    List<NameValuePair> actual = buildCommonParams("foo", true, 0, 10, ArticleSearchQuery.SolrSortOrder.MOST_CITED, true);
    SetMultimap<String, String> actualMap = convertToMap(actual);
    assertCommonParams(2, actualMap);
    assertSingle("10", actualMap.get("rows"));

    // if start == 0 no param should be present.
    assertEquals(0, actualMap.get("start").size());
    assertSingle("alm_scopusCiteCount desc,id desc", actualMap.get("sort"));
    assertSingle("dismax", actualMap.get("defType"));
    assertSingle("foo", actualMap.get("q"));

    // empty query string
    actual = buildCommonParams("*:*", false, 0, 10, ArticleSearchQuery.SolrSortOrder.MOST_CITED, true);
    actualMap = convertToMap(actual);
    assertCommonParams(2, actualMap);
    assertEquals(0, actualMap.get("defType").size());
    assertSingle("*:*", actualMap.get("q"));

    // not home page
    actual = buildCommonParams("", false, 0, 10, ArticleSearchQuery.SolrSortOrder.RELEVANCE, false);
    actualMap = convertToMap(actual);
    assertSingle("score desc,publication_date desc,id desc", actualMap.get("sort"));
    assertCommonParams(2, actualMap);

  }

  private List<NameValuePair> buildFacetParams(String facetField, String query, boolean useDisMax) {
    ArticleSearchQuery asq = ArticleSearchQuery.builder()
      .setFacetFields(ImmutableList.of(facetField))
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

  private static void setQueryFilters(UrlParamBuilder params, List<String> journalKeys,
                                      List<String> articleTypes, List<String> subjects,
                                      ArticleSearchQuery.SearchCriterion dateRange) {
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
    UrlParamBuilder actual = UrlParamBuilder.params();
    List<String> articleTypes = new ArrayList<>();
    List<String> subjects = new ArrayList<>();

    // Multiple journals
    setQueryFilters(actual, Arrays.asList("foo", "bar", "blaz"), articleTypes, subjects,
        ArticleSearchQuery.SolrEnumeratedDateRange.ALL_TIME);
    SetMultimap<String, String> actualMap = convertToMap(actual.build());
    assertJournals(actualMap.get("fq"), "foo", "bar", "blaz");

    // date range
    actual = UrlParamBuilder.params();
    setQueryFilters(actual, ImmutableList.of("foo"), articleTypes, subjects,
        ArticleSearchQuery.SolrEnumeratedDateRange.LAST_3_MONTHS);
    actualMap = convertToMap(actual.build());
    assertEquals(2, actualMap.get("fq").size());
    for (String s : actualMap.get("fq")) {
      if (!s.contains("publication_date:") && !s.contains("journal_key:")) {
        fail(s);
      }
    }

    // null date range
    actual = UrlParamBuilder.params();
    setQueryFilters(UrlParamBuilder.params(), ImmutableList.of("foo"), articleTypes, subjects, null);
    actualMap = convertToMap(actual.build());
    Set<String> fq = actualMap.get("fq");
    for (String s : fq) {
      if (s.startsWith("publication_date:")) {
        fail(s);
      }
    }
  }

  @Test
  public void testSetQueryFilters_ExplicitDateRange() throws IOException {
    UrlParamBuilder actual = UrlParamBuilder.params();
    ArticleSearchQuery.SolrExplicitDateRange edr
        = new ArticleSearchQuery.SolrExplicitDateRange("test", "2011-01-01", "2015-06-01");

    setQueryFilters(actual, ImmutableList.of("foo"), new ArrayList<String>(), new ArrayList<String>(), edr);
    SetMultimap<String, String> actualMap = convertToMap(actual.build());
    assertPubDate(actualMap.get("fq"));
    assertJournals(actualMap.get("fq"), "foo");
  }

  @Test
  public void testSetQueryFilters_IncludeArticleTypes() throws IOException {
    UrlParamBuilder actual = UrlParamBuilder.params();
    ArticleSearchQuery.SolrExplicitDateRange edr
        = new ArticleSearchQuery.SolrExplicitDateRange("test", "2011-01-01", "2015-06-01");
    ArrayList<String> articleTypes = new ArrayList<>();
    articleTypes.add("Research Article");
    setQueryFilters(actual, ImmutableList.of("foo"), articleTypes, new ArrayList<String>(), edr);
    SetMultimap<String, String> actualMap = convertToMap(actual.build());
    assertPubDate(actualMap.get("fq"));
    assertJournals(actualMap.get("fq"), "foo");
    assertArticleTypes(actualMap.get("fq"));
  }

  @Test
  public void testSetQueryFilters_IncludeSubjects() throws IOException {
    UrlParamBuilder actual = UrlParamBuilder.params();
    ArticleSearchQuery.SolrExplicitDateRange edr
        = new ArticleSearchQuery.SolrExplicitDateRange("test", "2011-01-01", "2015-06-01");
    ArrayList<String> articleTypes = new ArrayList<>();
    articleTypes.add("Research Article");
    setQueryFilters(actual, ImmutableList.of("foo"), articleTypes, Arrays.asList("Skull", "Head", "Teeth"), edr);
    SetMultimap<String, String> actualMap = convertToMap(actual.build());
    assertPubDate(actualMap.get("fq"));
    assertJournals(actualMap.get("fq"), "foo");
    assertArticleTypes(actualMap.get("fq"));
    assertSubjects(actualMap.get("fq"), "\"Skull\"", "\"Head\"", "\"Teeth\"");
  }

  @Test
  public void testBuildSearchClause() {
    assertEquals("subject:\"foo\"", SolrQueryBuilder.buildAndSearchClause("subject", Arrays.asList("foo")));
    assertEquals("subject:\"foo\" AND subject:\"2nd subject\"",
                 SolrQueryBuilder.buildAndSearchClause("subject", Arrays.asList("foo", "2nd subject")));
    assertEquals("author:\"author1\"", SolrQueryBuilder.buildAndSearchClause("author", Arrays.asList("author1")));
    assertEquals("author:\"author1\" AND author:\"author2\"",
                 SolrQueryBuilder.buildAndSearchClause("author", Arrays.asList("author1", "author2")));
    assertEquals("author:\"author1\" AND author:\"author2\"",
                 SolrQueryBuilder.buildAndSearchClause("author", Arrays.asList("author1", "author2")));
    assertEquals("author:\"author1\" OR author:\"author2\"",
                 SolrQueryBuilder.buildOrSearchClause("author", Arrays.asList("author1", "author2")));
    assertEquals("author:*", SolrQueryBuilder.buildOrSearchClause("author", Arrays.asList("*")));
  }

  @Test
  public void deserializeStatsResult() throws IOException {
    SolrSearchApi.Result result = gson.fromJson(read("queries/stats.json"), SolrSearchApi.Result.class);
    assertEquals(4510, result.getNumFound(), 4510);
    SolrSearchApi.FieldStatsResult<Date> publicationDateStats = result.getPublicationDateStats();
    Date minDate = publicationDateStats.getMin();
    assertEquals(2003, minDate.getYear() + 1900);
    assertEquals(7, minDate.getMonth());
    assertEquals(17, minDate.getDate());
    Date maxDate = publicationDateStats.getMax();
    assertEquals(2019, maxDate.getYear() + 1900);
    assertEquals(11, maxDate.getMonth());
    assertEquals(15, maxDate.getDate());
  }

  @Test
  public void deserializeSimpleResult() throws IOException {
    SolrSearchApi.Result result = gson.fromJson(read("queries/simple.json"), SolrSearchApi.Result.class);
    assertEquals(1784487, result.getNumFound());
    assertEquals("AoE/HjEwLjEzNzEvYW5ub3RhdGlvbi8wMGEzYjIyZS0zNmE5LTRkNTEtODllNS0xZTY1NjFlN2ExZTkvdGl0bGU=",
                 result.getNextCursorMark());
    assertEquals(10, result.getDocs().size());
    Map<String, Object> doc = result.getDocs().get(0);
    assertEquals("10.1371/annotation/008b05a8-229b-4aca-94ae-91e6dd5ca5ba", doc.get("id"));
  }

  @Test
  public void deserializeFacetResult() throws IOException {
    SolrSearchApi.Result result = gson.fromJson(read("queries/facet.json"), SolrSearchApi.Result.class);
    assertEquals(2, result.getNumFound());
    Map<String, Map<String, Integer>> facets = result.getFacets();
    assert(facets.keySet().contains("subject_facet"));
    assert(facets.keySet().contains("author_facet"));
    Map<String, Integer> subjectFacet = facets.get("subject_facet");
    Map<String, Integer> authorFacet = facets.get("author_facet");
    assertEquals(Integer.valueOf(2), subjectFacet.get("Biology and life sciences"));
    assertEquals(Integer.valueOf(1), authorFacet.get("Henriëtte Moll"));
  }

  @Test
  public void testDateParse() throws IOException {
    Date map = gson.fromJson("'2014-12-05T04:00:00.000Z'", Date.class);
    assertEquals(map.getClass(), Date.class);
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
    // "journal_key:\"PLoSBiology\" OR journal_key:\"PLoSONE\""
    String[] parts = journals.split(" OR ");
    assertEquals(expectedJournals.length, parts.length);
    Set<String> actualJournals = new HashSet<>();
    for (String part : parts) {
      actualJournals.add(part.substring("journal_key:".length() + 1, part.length() - 1));
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
