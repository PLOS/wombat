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

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;
import com.google.auto.value.AutoValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.JsonAdapter;
import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.site.url.Link;
import org.ambraproject.wombat.util.UriUtil;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of SearchService that queries a solr backend.
 */
public class SolrSearchApiImpl implements SolrSearchApi {

  private static final Logger log = LoggerFactory.getLogger(SolrSearchApiImpl.class);

  @Autowired
  private JsonService jsonService;
  @Autowired
  private CachedRemoteService<Reader> cachedRemoteReader;
  @Autowired
  private ArticleApi articleApi;
  @Autowired
  private RuntimeConfiguration runtimeConfiguration;

  @VisibleForTesting
  protected Map<String, String> eIssnToJournalKey;

  private static final int FACET_LIMIT = -1; //unlimited

  /**
   * Enumerates sort orders that we want to expose in the UI.
   */
  public static enum SolrSortOrder implements ArticleSearchQuery.SearchCriterion {

    // The order here determines the order in the UI.
    RELEVANCE("Relevance", "score desc,publication_date desc"),
    DATE_NEWEST_FIRST("Date, newest first", "publication_date desc"),
    DATE_OLDEST_FIRST("Date, oldest first", "publication_date asc"),
    MOST_VIEWS_30_DAYS("Most views, last 30 days", "counter_total_month desc"),
    MOST_VIEWS_ALL_TIME("Most views, all time", "counter_total_all desc"),
    MOST_CITED("Most cited, all time", "alm_scopusCiteCount desc"),
    MOST_BOOKMARKED("Most bookmarked", "alm_mendeleyCount desc"),
    MOST_SHARED("Most shared in social media", "sum(alm_twitterCount, alm_facebookCount) desc");

    private String description;

    private String value;

    SolrSortOrder(String description, String value) {
      this.description = description;
      this.value = value;
    }

    @Override
    public String getDescription() {
      return description;
    }

    @Override
    public String getValue() {
      return value;
    }
  }

  /**
   * Enumerates date ranges to expose in the UI.  Currently, these all start at some prior date and extend to today.
   */
  public static enum SolrEnumeratedDateRange implements ArticleSearchQuery.SearchCriterion {

    ALL_TIME("All time", -1),
    LAST_YEAR("Last year", 365),

    // Clearly these are approximations given the different lengths of months.
    LAST_6_MONTHS("Last 6 months", 182),
    LAST_3_MONTHS("Last 3 months", 91);

    private String description;

    private int daysAgo;

    SolrEnumeratedDateRange(String description, int daysAgo) {
      this.description = description;
      this.daysAgo = daysAgo;
    }

    @Override
    public String getDescription() {
      return description;
    }

    /**
     * @return a String representing part of the "fq" param to pass to solr that will restrict the date range
     * appropriately.  For example, "[2013-02-14T21:00:29.942Z TO 2013-08-15T21:00:29.942Z]". The String must be escaped
     * appropriately before being included in the URL.  The final http param passed to solr should look like
     * "fq=publication_date:[2013-02-14T21:00:29.942Z+TO+2013-08-15T21:00:29.942Z]". If this date range is ALL_TIME,
     * this method returns null.
     */
    @Override
    public String getValue() {
      if (daysAgo > 0) {
        Calendar today = Calendar.getInstance();
        today.setTimeZone(TimeZone.getTimeZone("UTC"));
        Calendar then = Calendar.getInstance();
        then.setTimeZone(TimeZone.getTimeZone("UTC"));
        then.add(Calendar.DAY_OF_YEAR, -daysAgo);
        return String.format("[%s TO %s]", DatatypeConverter.printDateTime(then),
            DatatypeConverter.printDateTime(today));
      } else {
        return null;
      }
    }
  }

  public static class SolrExplicitDateRange implements ArticleSearchQuery.SearchCriterion {

    private String description;
    private Calendar startDate;
    private Calendar endDate;

    public SolrExplicitDateRange(String description, String startDate, String endDate) {
      this.description = description;

      Calendar startCal = Calendar.getInstance();
      Calendar endCal = Calendar.getInstance();
      startCal.setTimeZone(TimeZone.getTimeZone("UTC"));
      endCal.setTimeZone(TimeZone.getTimeZone("UTC"));

      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
      // getValue() method uses DatatypeConverter.printDateTime to convert the calendar object to a string.
      // However, this method uses the local time zone. Setting the time zone for the Calendar object doesn't
      // enforce UTC in the result of the printDateTime method but setting it in the simpleDateFormat does.
      simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      try {
        startCal.setTime(simpleDateFormat.parse(startDate));
        endCal.setTime(simpleDateFormat.parse(endDate));
      } catch (ParseException e) {
        throw new RuntimeException(e);
      }

      this.startDate = startCal;
      this.endDate = endCal;
    }

    @Override
    public String getDescription() {
      return description;
    }

    /**
     * @return a String representing part of the "fq" param to pass to solr that will restrict the date range
     * appropriately.  For example, "[2013-02-14T21:00:29.942Z TO 2013-08-15T21:00:29.942Z]". The String must be escaped
     * appropriately before being included in the URL.  The final http param passed to solr should look like
     * "fq=publication_date:[2013-02-14T21:00:29.942Z+TO+2013-08-15T21:00:29.942Z]".
     */
    @Override
    public String getValue() {
      return String.format("[%s TO %s]", DatatypeConverter.printDateTime(startDate),
          DatatypeConverter.printDateTime(endDate));
    }

  }

  @Override
  public Map<String, ?> search(ArticleSearchQuery query) throws IOException {
    Map<String, Map> rawResults = rawSearch(query);

    if (query.getFacet().isPresent()) {
      Map<String, Map> facetFields =
          (Map<String, Map>) rawResults.get("facet_counts").get("facet_fields");
      return facetFields.get(query.getFacet().get()); //We expect facet field to be the first element of the list
    } else {
      return (Map<String, ?>) rawResults.get("response");
    }
  }

  @Override
  public Map<?, ?> lookupArticleByDoi(String doi, Site site) throws IOException {
    return lookupArticlesByDois(ImmutableList.of(doi), site);
  }

  @Override
  public Map<?, ?> lookupArticlesByDois(List<String> dois, Site site) throws IOException {
    String doiQueryString = dois.stream().map(doi -> "id:" + QueryParser.escape(doi))
        .collect(Collectors.joining(" OR "));

    ArticleSearchQuery query = ArticleSearchQuery.builder()
        .setQuery(doiQueryString)
        .setStart(0)
      .setRows(dois.size()).build();
    return search(query);
  }

  @Override
  public Map<?, ?> addArticleLinks(Map<?, ?> searchResults, HttpServletRequest request, Site site,
                                   SiteSet siteSet) throws IOException {
    initializeEIssnToJournalKeyMap(siteSet, site);
    List<Map> docs = (List<Map>) searchResults.get("docs");
    for (Map doc : docs) {
      String doi = (String) doc.get("id");
      String eIssn = (String) doc.get("eissn");
      String foreignJournalKey = eIssnToJournalKey.get(eIssn);
      String link = Link.toForeignSite(site, foreignJournalKey, siteSet).toPath("/article?id=" + doi).get(request);
      doc.put("link", link);
      doc.put("journalKey", foreignJournalKey);
    }
    return searchResults;
  }

  /**
   * Initializes the eIssnToJournalKey map if necessary by calling rhino to get eISSNs for all journals.
   *
   * @param siteSet     set of all sites
   * @param currentSite site associated with the current request
   * @throws IOException
   */
  @VisibleForTesting
  protected synchronized void initializeEIssnToJournalKeyMap(SiteSet siteSet, Site currentSite) throws IOException {
    if (eIssnToJournalKey == null) {
      Map<String, String> mutable = new HashMap<>();
      for (Site site : siteSet.getSites()) {
        Map<String, String> rhinoResult = (Map<String, String>) articleApi.requestObject(
            ApiAddress.builder("journals").addToken(site.getJournalKey()).build(), Map.class);
        mutable.put(rhinoResult.get("eIssn"), site.getJournalKey());
      }
      eIssnToJournalKey = ImmutableMap.copyOf(mutable);
    }
  }

  @Override
  public Map<String, String> getStats(String fieldName, String journalKey) throws IOException {
    ArticleSearchQuery query = ArticleSearchQuery.builder()
      .setStatsField(fieldName)
      .setSortOrder(SolrSortOrder.RELEVANCE)
      .setDateRange(SolrEnumeratedDateRange.ALL_TIME)
      .setJournalKeys(ImmutableList.of(journalKey))
      .build();

    Map<String, Map> rawResult = (Map<String, Map>) rawSearch(query);

    Map<String, Map> statsField = (Map<String, Map>) rawResult.get("stats").get("stats_fields");
    Map<String, String> field = (Map<String, String>) statsField.get(fieldName);
    return field;
  }

  private FacetedQueryResponse executeFacetedQuery(ArticleSearchQuery query) throws IOException {
    Map<String, Map> rawResults = (Map<String, Map>) rawSearch(query);
    Map<?, ?> facetCounts = rawResults.get("facet_counts");
    Map<?, ?> facetFields = (Map<?, ?>) facetCounts.get("facet_fields");
    Map<?, ?> resultsMap = (Map<?, ?>) facetFields.get(query.getFacet().get());
    return new FacetedQueryResponse(resultsMap, ((Double) rawResults.get("response").get("numFound")).longValue());
  }

  /**
   * @inheritDoc
   */
  @Override
  public List<String> getAllSubjects(String journalKey, Site site) throws IOException {
    ArticleSearchQuery query = ArticleSearchQuery.builder()
        .setFacet("subject_hierarchy")
        .setFacetLimit(FACET_LIMIT)
      .setJournalKeys(ImmutableList.of(journalKey)).build();

    FacetedQueryResponse response = executeFacetedQuery(query);
    return response.getResultsMap().keySet()
        .stream().map(Object::toString)
        .collect(Collectors.toList());
  }

  /**
   * @inheritDoc
   */
  @Override
  public Map<String, Long> getAllSubjectCounts(String journalKey, Site site) throws IOException {
    ArticleSearchQuery query = ArticleSearchQuery.builder()
      .setFacet("subject_facet")
      .setFacetLimit(FACET_LIMIT)
      .setJournalKeys(ImmutableList.of(journalKey))
      .build();

    FacetedQueryResponse response = executeFacetedQuery(query);
    Map<String, Long> subjectCounts = response
      .getResultsMap().entrySet().stream()
      .collect(Collectors.toMap(entry -> (String) entry.getKey(),
                                entry -> ((Double) entry.getValue()).longValue()));
    subjectCounts.put("ROOT", response.getTotalArticles());
    return ImmutableSortedMap.copyOf(subjectCounts);
  }

  /**
   * @inheritDoc
   */
  @Override
  public Map<String, Map> rawSearch(ArticleSearchQuery query) throws IOException {
    List<NameValuePair> params = SolrQueryBuilder.buildParameters(query);
    URI uri = getSolrUri(params);
    log.debug("Solr request executing: " + uri);
    Map<String, Map> rawResults = new HashMap<>();
    rawResults = jsonService.requestObject(cachedRemoteReader, new HttpGet(uri), Map.class);
    return rawResults;
  }

  @Override
  public Result cookedSearch(ArticleSearchQuery query) throws IOException {
    List<NameValuePair> params = SolrQueryBuilder.buildParameters(query);
    URI uri = getSolrUri(params);
    log.debug("Solr request executing: " + uri);
    return jsonService.requestObject(cachedRemoteReader, new HttpGet(uri), Result.class);
  }

  private URI getSolrUri(List<NameValuePair> params, Site site) throws SolrUndefinedException {
    try {
      URL solrServer = runtimeConfiguration.getSolrConfiguration().get().getUrl(site)
          .orElseThrow(() -> solrUndefinedException);
      return new URL(solrServer, "?" + UriUtil.formatParams(params)).toURI();
    } catch (MalformedURLException | URISyntaxException e) {
      //Solr server has already been validated - any exception here must be invalid values in params
      throw new IllegalArgumentException(e);
    }
  }

  private class FacetedQueryResponse {
    private final Map<?, ?> resultsMap;
    private final Long totalArticles;

    public FacetedQueryResponse(final Map<?, ?> resultsMap, final Long totalArticles) {
      this.resultsMap = resultsMap;
      this.totalArticles = totalArticles;
    }

    public Map<?, ?> getResultsMap() {
      return resultsMap;
    }

    public Long getTotalArticles() {
      return totalArticles;
    }
  }
}
