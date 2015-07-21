/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2013 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.wombat.service.remote;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Implementation of SearchService that queries a solr backend.
 */
public class SolrSearchService implements SearchService {

  @Autowired
  private JsonService jsonService;
  @Autowired
  private CachedRemoteService<Reader> cachedRemoteReader;

  @Autowired
  private SoaService soaService;

  @VisibleForTesting
  protected Map<String, Site> eIssnToSite;

  /**
   * Enumerates sort orders that we want to expose in the UI.
   */
  public static enum SolrSortOrder implements SearchCriterion {

    // The order here determines the order in the UI.
    RELEVANCE("Relevance", "score desc,publication_date desc"),
    DATE_NEWEST_FIRST("Date, newest first", "publication_date desc"),
    DATE_OLDEST_FIRST("Date, oldest first", "publication_date asc"),
    MOST_VIEWS_30_DAYS("Most views, last 30 days", "counter_total_month desc"),
    MOST_VIEWS_ALL_TIME("Most views, all time", "counter_total_all desc"),
    MOST_CITED("Most cited, all time", "alm_scopusCiteCount desc"),
    MOST_BOOKMARKED("Most bookmarked", "sum(alm_citeulikeCount, alm_mendeleyCount) desc"),
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
  public static enum SolrEnumeratedDateRange implements SearchCriterion {

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

  public static class SolrExplicitDateRange implements SearchCriterion {

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

  /**
   * Specifies the article fields in the solr schema that we want returned in the results.
   */
  private static final String FL = "id,eissn,publication_date,title,cross_published_journal_name,author_display,"
      + "article_type,counter_total_all,alm_scopusCiteCount,alm_citeulikeCount,alm_mendeleyCount,alm_twitterCount,"
      + "alm_facebookCount,retraction,expression_of_concern";

  @Autowired
  private RuntimeConfiguration runtimeConfiguration;

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<?, ?> simpleSearch(String query, List<String> journalKeys, List<String> articleTypes,
      int start, int rows, SearchCriterion sortOrder, SearchCriterion dateRange) throws IOException {
    List<NameValuePair> params = buildCommonParams(journalKeys, articleTypes, start, rows, sortOrder,
        dateRange, false);

    // TODO: escape/quote the q param if needed.
    if (Strings.isNullOrEmpty(query)) {
      query = "*:*";
    } else {

      // Use the dismax query parser, recommended for all user-entered queries.
      // See https://wiki.apache.org/solr/DisMax
      params.add(new BasicNameValuePair("defType", "dismax"));
    }
    params.add(new BasicNameValuePair("q", query));
    return executeQuery(params);
  }

  @Override
  public Map<?, ?> advancedSearch(String query, List<String> journalKeys, List<String> articleTypes,
      int start, int rows, SearchCriterion sortOrder) throws IOException {
    List<NameValuePair> params = buildCommonParams(journalKeys, articleTypes, start, rows, sortOrder,
        null, false);
    params.add(new BasicNameValuePair("q", query));
    return executeQuery(params);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<?, ?> subjectSearch(String subject, List<String> journalKeys,
      int start, int rows, SearchCriterion sortOrder, SearchCriterion dateRange) throws IOException {
    List<NameValuePair> params = buildCommonParams(journalKeys, start, rows, sortOrder,
        dateRange, false);
    params.add(new BasicNameValuePair("q", "*:*"));
    params.add(new BasicNameValuePair("fq", String.format("subject:\"%s\"", subject)));
    return executeQuery(params);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<?, ?> authorSearch(String author, List<String> journalKeys,
      int start, int rows, SearchCriterion sortOrder, SearchCriterion dateRange) throws IOException {
    List<NameValuePair> params = buildCommonParams(journalKeys, start, rows, sortOrder, dateRange, false);
    params.add(new BasicNameValuePair("q", String.format("author:\"%s\"", author)));
    return executeQuery(params);
  }

  @Override
  public Map<?, ?> getHomePageArticles(String journalKey, int start, int rows,
      SearchCriterion sortOrder) throws IOException {
    List<NameValuePair> params = buildCommonParams(Collections.singletonList(journalKey), start,
        rows, sortOrder, SolrEnumeratedDateRange.ALL_TIME, true);
    params.add(new BasicNameValuePair("q", "*:*"));
    return executeQuery(params);
  }

  @Override
  public Map<?, ?> addArticleLinks(Map<?, ?> searchResults, HttpServletRequest request, Site site, SiteSet siteSet)
      throws IOException {
    initializeEIssnToSiteMap(siteSet, site);
    List<Map> docs = (List<Map>) searchResults.get("docs");
    for (Map doc : docs) {
      String doi = (String) doc.get("id");
      String eIssn = (String) doc.get("eissn");
      Site resultSite = eIssnToSite.get(eIssn);
      doc.put("link", resultSite.getRequestScheme().buildLink(request, "/article?id=" + doi));
    }
    return searchResults;
  }

  /**
   * Initializes the eIssnToSite map if necessary by calling rhino to get eISSNs for all journals.
   *
   * @param siteSet set of all sites
   * @param currentSite site associated with the current request
   * @throws IOException
   */
  @VisibleForTesting
  protected synchronized void initializeEIssnToSiteMap(SiteSet siteSet, Site currentSite) throws IOException {
    if (eIssnToSite == null) {
      Map<String, Site> mutable = new HashMap<>();
      for (Site site : siteSet.getSites()) {
        Map<String, String> rhinoResult = (Map<String, String>) soaService.requestObject(
            "journals/" + site.getJournalKey(), Map.class);

        // We have to call Theme.resolveForeignJournalKey(), instead of using the site that
        // comes from the loop, since resolveForeignJournalKey() contains logic to only return
        // the same "type" of site (desktop or mobile).

        // Note that this logic will have to be changed if mobile ever calls this method.  Currently,
        // mobile search results links always point within the same site, so they can be generated
        // in the templates.
        mutable.put(rhinoResult.get("eIssn"),
            currentSite.getTheme().resolveForeignJournalKey(siteSet, site.getJournalKey()));
      }
      eIssnToSite = ImmutableMap.copyOf(mutable);
    }
  }

  //Override for buildCommonParams with no article types
  @VisibleForTesting
  List<NameValuePair> buildCommonParams(List<String> journalKeys, int start, int rows,
      SearchCriterion sortOrder, SearchCriterion dateRange, boolean forHomePage) {
    return buildCommonParams(journalKeys, new ArrayList<String>(), start, rows, sortOrder,
        dateRange, forHomePage);
  }

  /**
   * Populates the SOLR parameters with values used across all searchs in the application.
   *
   * @param journalKeys names of the journals in which to search.
   * @param articleTypes types of articles in which to search. An empty list will search all article types.
   * @param start       starting result, zero-based.  0 will start at the first result.
   * @param rows        max number of results to return
   * @param sortOrder   specifies the desired ordering for results
   * @param dateRange   specifies the date range for the results
   * @param forHomePage if true, this search is for articles on a journal home page; if false it is for a specific
   *                    query
   * @return populated list of parameters
   */
  @VisibleForTesting
  List<NameValuePair> buildCommonParams(List<String> journalKeys, List<String> articleTypes, int start, int rows,
        SearchCriterion sortOrder, SearchCriterion dateRange, boolean forHomePage) {

    // Fascinating how painful it is to construct a longish URL and escape it properly in Java.
    // This is the easiest way I found...
    List<NameValuePair> params = new ArrayList<>();
    params.add(new BasicNameValuePair("wt", "json"));
    params.add(new BasicNameValuePair("fl", FL));
    params.add(new BasicNameValuePair("fq", "doc_type:full"));
    params.add(new BasicNameValuePair("fq", "!article_type_facet:\"Issue Image\""));
    params.add(new BasicNameValuePair("rows", Integer.toString(rows)));
    if (start > 0) {
      params.add(new BasicNameValuePair("start", Integer.toString(start)));
    }
    // The next two params improve solr performance significantly.
    params.add(new BasicNameValuePair("hl", "false"));
    params.add(new BasicNameValuePair("facet", "false"));
    String sortOrderStr = sortOrder.getValue();

    // This is a quirk from ambra: when doing "normal" searches, we append "id desc" as the final part of
    // the sort order.  However ambra doesn't do this for homepage-related searches, and the returned
    // results are slightly different.  We replicate that behavior here.
    if (!forHomePage) {
      sortOrderStr += ",id desc";
    }
    params.add(new BasicNameValuePair("sort", sortOrderStr));
    if (dateRange != null) {
      String dateRangeStr = dateRange.getValue();
      if (!Strings.isNullOrEmpty(dateRangeStr)) {
        params.add(new BasicNameValuePair("fq", "publication_date:" + dateRangeStr));
      }
    }
    List<String> crossPublishedJournals = new ArrayList<>();
    for (String journalKey : journalKeys) {
      crossPublishedJournals.add("cross_published_journal_key:" + journalKey);
    }
    params.add(new BasicNameValuePair("fq", Joiner.on(" OR ").join(crossPublishedJournals)));

    if (articleTypes != null && !articleTypes.isEmpty()) {
      List<String> articleTypeQueryList = new ArrayList<>();
      for (String articleType : articleTypes) {
        articleTypeQueryList.add("article_type_facet:\"" + articleType + "\"");
      }
      params.add(new BasicNameValuePair("fq", Joiner.on(" OR ").join(articleTypeQueryList)));
    }

    return params;
  }

  private Map<?, ?> executeQuery(List<NameValuePair> params) throws IOException {
    URI uri;
    try {
      uri = new URL(runtimeConfiguration.getSolrServer(), "?" + URLEncodedUtils.format(params, "UTF-8")).toURI();
    } catch (MalformedURLException | URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
    Map<?, ?> rawResults = jsonService.requestObject(cachedRemoteReader, uri, Map.class);
    return (Map<?, ?>) rawResults.get("response");
  }
}
