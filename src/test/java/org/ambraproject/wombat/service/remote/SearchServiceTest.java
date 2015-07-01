package org.ambraproject.wombat.service.remote;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.ambraproject.wombat.config.TestSpringConfiguration;
import org.apache.http.NameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.testng.Assert.*;

@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = TestSpringConfiguration.class)
public class SearchServiceTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private SolrSearchService searchService;

  @Test
  public void testBuildCommonParams() {

    // Single journal
    List<NameValuePair> actual = searchService.buildCommonParams(Collections.singletonList("foo"), 0, 10,
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
    actual = searchService.buildCommonParams(Arrays.asList("foo", "bar", "blaz"), 20, 25,
        SolrSearchService.SolrSortOrder.RELEVANCE, SolrSearchService.SolrEnumeratedDateRange.ALL_TIME, false);
    actualMap = convertToMap(actual);
    assertCommonParams(actualMap, 3);
    assertSingle(actualMap.get("rows"), "25");
    assertSingle(actualMap.get("start"), "20");
    assertSingle(actualMap.get("sort"), "score desc,publication_date desc,id desc");
    assertJournals(actualMap.get("fq"), "foo", "bar", "blaz");
  }

  @Test
  public void testBuildCommonParams_ExplicitDateRange() throws IOException {
    SolrSearchService.SolrExplicitDateRange edr
        = new SolrSearchService.SolrExplicitDateRange("test", "2011-01-01", "2015-06-01");
    List<NameValuePair> actual = searchService.buildCommonParams(Collections.singletonList("foo"),
        0, 10, SolrSearchService.SolrSortOrder.MOST_CITED, edr, true);
    SetMultimap<String, String> actualMap = convertToMap(actual);
    assertCommonParams(actualMap, 4);
    assertSingle(actualMap.get("rows"), "10");
    assertEquals(actualMap.get("start").size(), 0);
    assertSingle(actualMap.get("sort"), "alm_scopusCiteCount desc");
    assertPubDate(actualMap.get("fq"));
    assertJournals(actualMap.get("fq"), "foo");
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
}
