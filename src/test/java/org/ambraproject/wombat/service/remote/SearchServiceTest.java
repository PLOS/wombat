package org.ambraproject.wombat.service.remote;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.SetMultimap;
import org.ambraproject.wombat.config.TestSpringConfiguration;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
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
public class SearchServiceTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private SolrSearchService searchService;

  @Autowired
  private SiteSet siteSet;

  @Test
  public void testBuildCommonParams() {
    List<String> articleTypes = new ArrayList<>();

    // Single journal
    List<NameValuePair> actual = searchService.buildCommonParams(Collections.singletonList("foo"), articleTypes, 0, 10,
        SolrSearchService.SolrSortOrder.MOST_CITED, SolrSearchService.SolrEnumeratedDateRange.LAST_6_MONTHS, true);
    SetMultimap<String, String> actualMap = convertToMap(actual);
    assertCommonParams(actualMap, 4);
    assertSingle(actualMap.get("rows"), "10");

    // if start == 0 no param should be present.
    assertEquals(actualMap.get("start").size(), 0);
    assertSingle(actualMap.get("sort"), "alm_scopusCiteCount desc");
    assertPubDate(actualMap.get("fq"));
    assertJournals(actualMap.get("fq"), "foo");

    // Multiple journals
    actual = searchService.buildCommonParams(Arrays.asList("foo", "bar", "blaz"), articleTypes, 20, 25,
        SolrSearchService.SolrSortOrder.RELEVANCE, SolrSearchService.SolrEnumeratedDateRange.ALL_TIME, false);
    actualMap = convertToMap(actual);
    assertCommonParams(actualMap, 3);
    assertSingle(actualMap.get("rows"), "25");
    assertSingle(actualMap.get("start"), "20");
    assertSingle(actualMap.get("sort"), "score desc,publication_date desc,id desc");
    assertJournals(actualMap.get("fq"), "foo", "bar", "blaz");

    // null date range
    actual = searchService.buildCommonParams(Collections.singletonList("foo"), articleTypes, 0, 15,
        SolrSearchService.SolrSortOrder.RELEVANCE, null, false);
    actualMap = convertToMap(actual);
    assertCommonParams(actualMap, 3);
    Set<String> fq = actualMap.get("fq");
    for (String s : fq) {
      if (s.startsWith("publication_date:")) {
        fail(s);
      }
    }
  }

  @Test
  public void testBuildCommonParams_ExplicitDateRange() throws IOException {
    SolrSearchService.SolrExplicitDateRange edr
        = new SolrSearchService.SolrExplicitDateRange("test", "2011-01-01", "2015-06-01");
    List<NameValuePair> actual = searchService.buildCommonParams(Collections.singletonList("foo"),
        new ArrayList<String>(), 0, 10, SolrSearchService.SolrSortOrder.MOST_CITED, edr, true);
    SetMultimap<String, String> actualMap = convertToMap(actual);
    assertCommonParams(actualMap, 4);
    assertSingle(actualMap.get("rows"), "10");
    assertEquals(actualMap.get("start").size(), 0);
    assertSingle(actualMap.get("sort"), "alm_scopusCiteCount desc");
    assertPubDate(actualMap.get("fq"));
    assertJournals(actualMap.get("fq"), "foo");
  }

  @Test
  public void testBuildCommonParams_IncludeArticleTypes() throws IOException {
    SolrSearchService.SolrExplicitDateRange edr
        = new SolrSearchService.SolrExplicitDateRange("test", "2011-01-01", "2015-06-01");
    ArrayList<String> articleTypes = new ArrayList<>();
    articleTypes.add("Research Article");
    List<NameValuePair> actual = searchService.buildCommonParams(Collections.singletonList("foo"),
      articleTypes, 0, 10, SolrSearchService.SolrSortOrder.MOST_CITED, edr, true);
    SetMultimap<String, String> actualMap = convertToMap(actual);
    assertCommonParams(actualMap, 5);
    assertSingle(actualMap.get("rows"), "10");
    assertEquals(actualMap.get("start").size(), 0);
    assertSingle(actualMap.get("sort"), "alm_scopusCiteCount desc");
    assertPubDate(actualMap.get("fq"));
    assertJournals(actualMap.get("fq"), "foo");
    assertArticleTypes(actualMap.get("fq"));
  }

  private static class SearchServiceForAddArticleLinksTest extends SolrSearchService {

    @Override
    protected void initializeEIssnToSiteMap(SiteSet siteSet, Site currentSite) throws IOException {
      ImmutableMap.Builder<String, Site> builder = new ImmutableMap.Builder<>();
      builder.put("123", siteSet.getSites("journal1Key").get(0))
          .put("456", siteSet.getSites("journal2Key").get(0))
          .put("789", siteSet.getSites("collectionJournalKey").get(0));
      eIssnToSite = builder.build();
    }
  }

  @Test
  public void testAddArticleLinks() throws IOException {
    SearchService searchServiceForTest = new SearchServiceForAddArticleLinksTest();
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
    List<Site> sites = siteSet.getSites("journal2Key");
    assertEquals(sites.size(), 1);  // For the purposes of this test

    Map<?, ?> actual = searchServiceForTest.addArticleLinks(searchResults, request, sites.get(0), siteSet);
    List<Map> actualDocs = (List) actual.get("docs");
    assertEquals(actualDocs.size(), 1);
    Map actualDoc = (Map) actualDocs.get(0);
    assertEquals(actualDoc.get("id"), "12345");
    assertEquals(actualDoc.get("link"), "someContextPath/site1/article?id=12345");
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
   * Asserts that the given set (all values of a single parameter) has exactly one entry
   * with the expected value.
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
}
