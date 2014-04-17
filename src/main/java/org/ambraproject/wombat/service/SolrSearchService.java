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

package org.ambraproject.wombat.service;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.ambraproject.wombat.config.Site;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Implementation of SearchService that queries a solr backend.
 */
public class SolrSearchService extends JsonService implements SearchService {

  /**
   * Enumerates sort orders that we want to expose in the UI.
   */
  public static enum SolrSortOrder implements SearchCriterion {

    // The order here determines the order in the UI.
    RELEVANCE("Relevance", "score desc,publication_date desc,id desc"),
    DATE_NEWEST_FIRST("Date, newest first", "publication_date desc,id desc"),
    DATE_OLDEST_FIRST("Date, oldest first", "publication_date asc,id desc"),

    // For some reason, ambra defines slightly different orderings for the "Recent" tab on the home page,
    // and the "Most Views, last 30 days" option on the search results ordering dropdown.  The former
    // omits the "id desc" clause.  To replicate this behavior, we define the following two values.
    MOST_VIEWS_30_DAYS("Most views, last 30 days", "counter_total_month desc,id desc"),
    POPULAR("Popular", "counter_total_month desc"),
    MOST_VIEWS_ALL_TIME("Most views, all time", "counter_total_all desc,id desc"),
    MOST_CITED("Most cited, all time", "alm_scopusCiteCount desc,id desc"),
    MOST_BOOKMARKED("Most bookmarked", "sum(alm_citeulikeCount, alm_mendeleyCount) desc,id desc"),
    MOST_SHARED("Most shared in social media", "sum(alm_twitterCount, alm_facebookCount) desc,id desc");

    /**
     * SolrSortOrders that can be used to sort search results.
     */
    public static final ImmutableList<SolrSortOrder> SEARCH_SORT_ORDERS = ImmutableList.of(
        RELEVANCE,
        DATE_NEWEST_FIRST,
        DATE_OLDEST_FIRST,
        MOST_VIEWS_30_DAYS,
        MOST_VIEWS_ALL_TIME,
        MOST_CITED,
        MOST_BOOKMARKED,
        MOST_SHARED
    );

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
  public static enum SolrDateRange implements SearchCriterion {

    ALL_TIME("All time", -1),
    LAST_YEAR("Last year", 365),

    // Clearly these are approximations given the different lengths of months.
    LAST_6_MONTHS("Last 6 months", 182),
    LAST_3_MONTHS("Last 3 months", 91);

    private String description;

    private int daysAgo;

    SolrDateRange(String description, int daysAgo) {
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

  /**
   * Specifies the article fields in the solr schema that we want returned in the results.
   */
  private static final String FL = "id,publication_date,title,cross_published_journal_name,author_display,article_type,"
      + "counter_total_all,alm_scopusCiteCount,alm_citeulikeCount,alm_mendeleyCount,alm_twitterCount,alm_facebookCount";

  @Autowired
  private RuntimeConfiguration runtimeConfiguration;

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<?, ?> simpleSearch(String query, Site site, int start, int rows, SearchCriterion sortOrder,
                                SearchCriterion dateRange) throws IOException {
    List<NameValuePair> params = buildCommonParams(site, start, rows, sortOrder, dateRange);

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

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<?, ?> subjectSearch(String subject, Site site, int start, int rows, SearchCriterion sortOrder,
                                 SearchCriterion dateRange) throws IOException {
    List<NameValuePair> params = buildCommonParams(site, start, rows, sortOrder, dateRange);
    params.add(new BasicNameValuePair("q", "*:*"));
    params.add(new BasicNameValuePair("fq", String.format("subject:\"%s\"", subject)));
    return executeQuery(params);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<?, ?> authorSearch(String author, Site site, int start, int rows, SearchCriterion sortOrder,
                                SearchCriterion dateRange) throws IOException {
    List<NameValuePair> params = buildCommonParams(site, start, rows, sortOrder, dateRange);
    params.add(new BasicNameValuePair("q", String.format("author:\"%s\"", author)));
    return executeQuery(params);
  }

  private List<NameValuePair> buildCommonParams(Site site, int start, int rows, SearchCriterion sortOrder,
                                                SearchCriterion dateRange) {

    // Fascinating how painful it is to construct a longish URL and escape it properly in Java.
    // This is the easiest way I found...
    List<NameValuePair> params = new ArrayList<NameValuePair>();
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
    params.add(new BasicNameValuePair("sort", sortOrder.getValue()));
    String dateRangeStr = dateRange.getValue();
    if (!Strings.isNullOrEmpty(dateRangeStr)) {
      params.add(new BasicNameValuePair("fq", "publication_date:" + dateRangeStr));
    }
    params.add(new BasicNameValuePair("fq", "cross_published_journal_key:" + site.getJournalKey()));
    return params;
  }

  private Map<?, ?> executeQuery(List<NameValuePair> params) throws IOException {
    URI uri;
    try {
      uri = new URL(runtimeConfiguration.getSolrServer(), "?" + URLEncodedUtils.format(params, "UTF-8")).toURI();
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException(e);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
    Map<?, ?> rawResults = requestObject(uri, Map.class);
    return (Map<?, ?>) rawResults.get("response");
  }
}
