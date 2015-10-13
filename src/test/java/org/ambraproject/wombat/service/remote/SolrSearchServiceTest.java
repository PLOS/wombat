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
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = TestSpringConfiguration.class)
public class SolrSearchServiceTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private SolrSearchService searchService;

  @Autowired
  private SiteSet siteSet;

  private static List<NameValuePair> buildCommonParams(String query, boolean useDisMax, int start,
                                                       int rows, SolrSearchService.SearchCriterion sortOrder,
                                                       boolean forHomePage) {
    return ArticleSearchQuery.builder()
        .setQuery(query)
        .setSimple(useDisMax)
        .setStart(start)
        .setRows(rows)
        .setSortOrder(sortOrder)
        .build().buildParameters();
  }

  @Test
  public void testBuildCommonParams() {
    // query string not null
    List<NameValuePair> actual = buildCommonParams("foo", true, 0, 10, SolrSearchServiceImpl.SolrSortOrder.MOST_CITED, true);
    SetMultimap<String, String> actualMap = convertToMap(actual);
    assertCommonParams(actualMap, 2);
    assertSingle(actualMap.get("rows"), "10");

    // if start == 0 no param should be present.
    assertEquals(actualMap.get("start").size(), 0);
    assertSingle(actualMap.get("sort"), "alm_scopusCiteCount desc,id desc");
    assertSingle(actualMap.get("defType"), "dismax");
    assertSingle(actualMap.get("q"), "foo");

    // empty query string
    actual = buildCommonParams("", false, 0, 10, SolrSearchServiceImpl.SolrSortOrder.MOST_CITED, true);
    actualMap = convertToMap(actual);
    assertCommonParams(actualMap, 2);
    assertEquals(actualMap.get("defType").size(), 0);
    assertSingle(actualMap.get("q"), "*:*");

    // not home page
    actual = buildCommonParams("", false, 0, 10, SolrSearchServiceImpl.SolrSortOrder.RELEVANCE, false);
    actualMap = convertToMap(actual);
    assertCommonParams(actualMap, 2);
    assertSingle(actualMap.get("sort"), "score desc,publication_date desc,id desc");

  }

  private List<NameValuePair> buildFacetParams(String facetField, String query, boolean useDisMax) {
    return ArticleSearchQuery.builder()
        .setFacet(facetField)
        .setQuery(query)
        .setSimple(useDisMax)
        .build().buildParameters();
  }

  @Test
  public void testFacetParams() {
    // query string not null
    List<NameValuePair> actual = buildFacetParams("journal", "foo", true);
    SetMultimap<String, String> actualMap = convertToMap(actual);
    assertFacetParams(actualMap, 2);
    assertSingle(actualMap.get("facet.field"), "journal");
    assertSingle(actualMap.get("defType"), "dismax");
    assertSingle(actualMap.get("q"), "foo");

    // empty query string
    actual = buildFacetParams("journal", "", false);
    actualMap = convertToMap(actual);
    assertFacetParams(actualMap, 2);
    assertSingle(actualMap.get("facet.field"), "journal");
    assertEquals(actualMap.get("defType").size(), 0);
    assertSingle(actualMap.get("q"), "*:*");
  }

  private static void setQueryFilters(List<NameValuePair> params, List<String> journalKeys,
                                      List<String> articleTypes, List<String> subjects,
                                      SolrSearchService.SearchCriterion dateRange) {
    ArticleSearchQuery.builder()
        .setJournalKeys(journalKeys)
        .setArticleTypes(articleTypes)
        .setSubjects(subjects)
        .setDateRange(dateRange)
        .build().setQueryFilters(params);
  }

  @Test
  public void testSetQueryFilters() {
    List<NameValuePair> actual = new ArrayList<>();
    List<String> articleTypes = new ArrayList<>();
    List<String> subjects = new ArrayList<>();

    // Multiple journals
    setQueryFilters(actual, Arrays.asList("foo", "bar", "blaz"), articleTypes, subjects,
        SolrSearchServiceImpl.SolrEnumeratedDateRange.ALL_TIME);
    SetMultimap<String, String> actualMap = convertToMap(actual);
    assertJournals(actualMap.get("fq"), "foo", "bar", "blaz");

    // date range
    actual = new ArrayList<>();
    setQueryFilters(actual, Collections.singletonList("foo"), articleTypes, subjects,
        SolrSearchServiceImpl.SolrEnumeratedDateRange.LAST_3_MONTHS);
    actualMap = convertToMap(actual);
    assertEquals(actualMap.get("fq").size(), 2);
    for (String s : actualMap.get("fq")) {
      if (!s.contains("publication_date:") && !s.contains("cross_published_journal_key:")) {
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
    SolrSearchServiceImpl.SolrExplicitDateRange edr
        = new SolrSearchServiceImpl.SolrExplicitDateRange("test", "2011-01-01", "2015-06-01");

    setQueryFilters(actual, Collections.singletonList("foo"), new ArrayList<String>(), new ArrayList<String>(), edr);
    SetMultimap<String, String> actualMap = convertToMap(actual);
    assertPubDate(actualMap.get("fq"));
    assertJournals(actualMap.get("fq"), "foo");
  }

  @Test
  public void testSetQueryFilters_IncludeArticleTypes() throws IOException {
    List<NameValuePair> actual = new ArrayList<>();
    SolrSearchServiceImpl.SolrExplicitDateRange edr
        = new SolrSearchServiceImpl.SolrExplicitDateRange("test", "2011-01-01", "2015-06-01");
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
    SolrSearchServiceImpl.SolrExplicitDateRange edr
        = new SolrSearchServiceImpl.SolrExplicitDateRange("test", "2011-01-01", "2015-06-01");
    ArrayList<String> articleTypes = new ArrayList<>();
    articleTypes.add("Research Article");
    setQueryFilters(actual, Collections.singletonList("foo"), articleTypes, Arrays.asList("Skull", "Head", "Teeth"), edr);
    SetMultimap<String, String> actualMap = convertToMap(actual);
    assertPubDate(actualMap.get("fq"));
    assertJournals(actualMap.get("fq"), "foo");
    assertArticleTypes(actualMap.get("fq"));
    assertSubjects(actualMap.get("fq"), "\"Skull\"", "\"Head\"", "\"Teeth\"");
  }

  private static class SearchServiceForAddArticleLinksTest extends SolrSearchServiceImpl {

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
    SolrSearchService solrSearchServiceForTest = new SearchServiceForAddArticleLinksTest();
    Map<String, List<Map>> searchResults = new HashMap<>();
    List<Map> docs = new ArrayList<>(1);
    Map doc = new HashMap();
    List<String> crossPubbedJournals = new ArrayList<>(1);
    crossPubbedJournals.add("journal1Key");
    doc.put("id", "12345");
    doc.put("eissn", "123");
    docs.add(doc);
    searchResults.put("docs", docs);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setContextPath("someContextPath");
    Site site = MockSiteUtil.getByUniqueJournalKey(siteSet, "journal2Key");

    Map<?, ?> actual = solrSearchServiceForTest.addArticleLinks(searchResults, request, site, siteSet);
    List<Map> actualDocs = (List) actual.get("docs");
    assertEquals(actualDocs.size(), 1);
    Map actualDoc = (Map) actualDocs.get(0);
    assertEquals(actualDoc.get("id"), "12345");
    assertTrue(actualDoc.get("link").toString().endsWith("someContextPath/site1/article?id=12345"));
  }

  @Test
  public void testBuildSubjectClause() {
    assertEquals(ArticleSearchQuery.buildSubjectClause(Arrays.asList("foo")), "subject:\"foo\"");
    assertEquals(ArticleSearchQuery.buildSubjectClause(Arrays.asList("foo", "2nd subject")),
        "subject:\"foo\" AND subject:\"2nd subject\"");
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
  private void assertCommonParams(SetMultimap<String, String> actual, int expectedNumFqParams) {
    assertSingle(actual.get("wt"), "json");
    Set<String> fq = actual.get("fq");

    // We check two of the fq params here; the caller should check the others.
    Joiner joiner = Joiner.on(",");
    assertEquals(fq.size(), expectedNumFqParams, joiner.join(fq));
    assertTrue(fq.contains("doc_type:full"));
    assertTrue(fq.contains("!article_type_facet:\"Issue Image\""));
    assertSingle(actual.get("hl"), "false");
    assertSingle(actual.get("facet"), "false");
  }

  /**
   * Tests for parameters expected to be the same across all tests.
   */
  private void assertFacetParams(SetMultimap<String, String> actual, int expectedNumFqParams) {
    assertSingle(actual.get("wt"), "json");
    assertSingle(actual.get("json.nl"), "map");
    Set<String> fq = actual.get("fq");

    // We check two of the fq params here; the caller should check the others.
    Joiner joiner = Joiner.on(",");
    assertEquals(fq.size(), expectedNumFqParams, joiner.join(fq));
    assertTrue(fq.contains("doc_type:full"));
    assertTrue(fq.contains("!article_type_facet:\"Issue Image\""));
    assertSingle(actual.get("hl"), "false");
    assertSingle(actual.get("facet"), "true");
  }

  /**
   * Asserts that the given set (all values of a single parameter) has exactly one entry with the expected value.
   */
  private void assertSingle(Set<String> multimapSet, String expected) {
    assertEquals(multimapSet.size(), 1);
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
      if (s.startsWith("cross_published_journal_key:")) {
        journals = s;
        break;
      }
    }
    assertNotNull(journals);

    // For multiple journals, the expected format of the param is
    // "cross_published_journal_key:PLoSBiology OR cross_published_journal_key:PLoSONE"
    String[] parts = journals.split(" OR ");
    assertEquals(parts.length, expectedJournals.length);
    Set<String> actualJournals = new HashSet<>();
    for (String part : parts) {
      actualJournals.add(part.substring("cross_published_journal_key:".length()));
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
    assertEquals(parts.length, expectedSubjects.length);
    Set<String> actualSubjects = new HashSet<>();
    for (String part : parts) {
      actualSubjects.add(part.substring("subject:".length()));
    }
    for (String expected : expectedSubjects) {
      assertTrue(actualSubjects.contains(expected));
    }
  }
}
