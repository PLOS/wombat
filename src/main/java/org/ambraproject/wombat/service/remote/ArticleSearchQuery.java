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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import com.google.auto.value.AutoValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

@AutoValue
public abstract class ArticleSearchQuery {

  /**
   * Specifies the article fields in the solr schema that we want returned in the results.
   */
  private static final String ARTICLE_FIELDS = Joiner.on(',').join("id", "eissn",
      "publication_date", "title", "title_display", "journal_name", "author_display",
      "article_type", "counter_total_all", "alm_scopusCiteCount", 
      "alm_mendeleyCount", "alm_twitterCount", "alm_facebookCount", "retraction",
      "expression_of_concern", "striking_image", "figure_table_caption", "journal_key");
  private static final String RSS_FIELDS = Joiner.on(',').join("id", "publication_date",
      "title", "title_display", "journal_name", "author_display", "abstract",
      "abstract_primary_display");
  private static final String CSV_FIELDS = Joiner.on(',').join(
      "id", "publication_date", "title", "author_display", "author_affiliate",
      "article_type", "received_date", "accepted_date", "counter_total_all", "alm_scopusCiteCount",
      "alm_connoteaCount", "alm_mendeleyCount", "alm_twitterCount",
      "alm_facebookCount", "alm_pmc_usage_total_all", "alm_webOfScienceCount", "editor_display",
      "abstract", "subject", "reference");
  private static final String JOURNAL_FIELDS = Joiner.on(',').join(
      "journal_key", "journal_name");


  @VisibleForTesting
  List<NameValuePair> buildParameters() {
    List<NameValuePair> params = new ArrayList<>();

    if (this.isCsvSearch()) {
      params.add(new BasicNameValuePair("wt", "csv"));
    } else {
      params.add(new BasicNameValuePair("wt", "json"));
    }

    if (this.isPartialSearch()) {
      params.add(new BasicNameValuePair("qf", "doc_partial_body"));
      params.add(new BasicNameValuePair("fl", "*"));
      params.add(new BasicNameValuePair("fq", "doc_type:partial"));
    } else {
      params.add(new BasicNameValuePair("fq", "doc_type:full"));
    }

    params.add(new BasicNameValuePair("fq", "!article_type_facet:\"Issue Image\""));
    for (String filterQuery : this.getFilterQueries()) {
      params.add(new BasicNameValuePair("fq", filterQuery));
    }

    if (this.getStart() > 0) {
      params.add(new BasicNameValuePair("start", Integer.toString(this.getStart())));
    }
    params.add(new BasicNameValuePair("rows", Integer.toString(this.getRows())));

    params.add(new BasicNameValuePair("hl", "false"));

    String queryString = this.getQuery();
    params.add(new BasicNameValuePair("q", queryString));
    if (this.isSimple()) {
      // Use the dismax query parser, recommended for all user-entered queries.
      // See https://wiki.apache.org/solr/DisMax
      params.add(new BasicNameValuePair("defType", "dismax"));
    }

    if (this.getFacet().isPresent()) {
      params.add(new BasicNameValuePair("facet", "true"));
      params.add(new BasicNameValuePair("facet.field", this.getFacet().get()));
      params.add(new BasicNameValuePair("facet.mincount", Integer.toString(this.getFacetMinCount())));
      params.add(new BasicNameValuePair("facet.limit", Integer.toString(this.getFacetLimit())));
      params.add(new BasicNameValuePair("json.nl", "map"));
    } else if (this.isRssSearch()) {
      params.add(new BasicNameValuePair("facet", "false"));
      params.add(new BasicNameValuePair("fl", RSS_FIELDS));
    } else if (this.isCsvSearch()) {
      params.add(new BasicNameValuePair("facet", "false"));
      params.add(new BasicNameValuePair("fl", CSV_FIELDS));
    } else if (this.isJournalSearch()) {
      params.add(new BasicNameValuePair("facet", "false"));
      params.add(new BasicNameValuePair("fl", JOURNAL_FIELDS));
    } else {
      params.add(new BasicNameValuePair("facet", "false"));
      params.add(new BasicNameValuePair("fl", ARTICLE_FIELDS));
    }

    this.getCursor().ifPresent(cursor -> params.add(new BasicNameValuePair("cursorMark", cursor)));

    setQueryFilters(params);

    for (Map.Entry<String, String> entry : this.getRawParameters().entrySet()) {
      params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
    }

    return params;
  }

  void setQueryFilters(List<NameValuePair> params) {
    if (this.getSortOrder().isPresent()) {
      String sortOrderStr = this.getSortOrder().get().getValue() + ",id desc";
      params.add(new BasicNameValuePair("sort", sortOrderStr));
    }

    if (this.getDateRange().isPresent()) {
      String dateRangeStr = this.getDateRange().get().getValue();
      if (!Strings.isNullOrEmpty(dateRangeStr)) {
        params.add(new BasicNameValuePair("fq", "publication_date:" + dateRangeStr));
      }
    }
    if (!CollectionUtils.isEmpty(this.getJournalKeys())) {
      List<String> crossPublishedJournals = this.getJournalKeys().stream()
          .map(journalKey -> "journal_key:" + journalKey).collect(Collectors.toList());
      params.add(new BasicNameValuePair("fq", Joiner.on(" OR ").join(crossPublishedJournals)));
    }

    if (!CollectionUtils.isEmpty(this.getArticleTypes())) {
      List<String> articleTypeQueryList = this.getArticleTypes().stream()
          .map(articleType ->
          {
            String articleTypeStr = articleType.equals("*") ? articleType : "\"" + articleType + "\"";
            return "article_type_facet:" + articleTypeStr;
          })
          .collect(Collectors.toList());
      params.add(new BasicNameValuePair("fq", Joiner.on(" OR ").join(articleTypeQueryList)));
    }

    if (!CollectionUtils.isEmpty(this.getArticleTypesToExclude())) {
      List<String> articleTypeToExcludeQueryList = this.getArticleTypesToExclude().stream()
          .map(articleType -> "!article_type_facet:\"" + articleType + "\"").collect(Collectors.toList());
      params.add(new BasicNameValuePair("fq", Joiner.on(" AND ").join(articleTypeToExcludeQueryList)));
    }

    if (!CollectionUtils.isEmpty(this.getSubjects())) {
      params.add(new BasicNameValuePair("fq", buildSubjectClause(this.getSubjects())));
    }

    if (!CollectionUtils.isEmpty(this.getAuthors())) {
      params.add(new BasicNameValuePair("fq", buildAuthorClause(this.getAuthors())));
    }

    if (!CollectionUtils.isEmpty(this.getSections())) {
      List<String> sectionQueryList = new ArrayList<>();
      for (String section : this.getSections()) {
        //Convert friendly section name to Solr field name TODO:clean this up
        section = section.equals("References") ? "reference" : section;
        sectionQueryList.add(section.toLowerCase().replace(' ', '_'));
      }
      params.add(new BasicNameValuePair("qf", Joiner.on(" OR ").join(sectionQueryList)));
    }
  }

  static String buildSubjectClause(List<String> subjects) {
    List<String> quotedSubjects = new ArrayList<>();
    for (String subject : subjects) {
      StringBuilder sb = new StringBuilder();
      sb.append("subject:\"");
      sb.append(subject);
      sb.append('"');
      quotedSubjects.add(sb.toString());
    }
    return Joiner.on(" AND ").join(quotedSubjects);
  }

  static String buildAuthorClause(List<String> authors) {
    List<String> quotedAuthors = new ArrayList<>();
    for (String author : authors) {
      StringBuilder sb = new StringBuilder();
      sb.append("author:\"");
      sb.append(author);
      sb.append('"');
      quotedAuthors.add(sb.toString());
    }
    return Joiner.on(" AND ").join(quotedAuthors);
  }


  /**
   * Callback object for exposing search service functionality.
   */
  public static interface QueryExecutor {
    /**
     * Send a raw query to the Solr service.
     *
     * @param params raw parameters to send to the Solr service
     * @return raw results from the Solr service
     * @throws IOException
     */
    Map<String, Map> executeQuery(List<NameValuePair> params) throws IOException;
  }

  /**
   * Build a Solr query, execute it, and return formatted results.
   *
   * @param queryExecutor a callback that executes the query on a Solr service
   * @return the search results matching this query object
   * @throws IOException
   */
  public Map<String, ?> search(QueryExecutor queryExecutor) throws IOException {
    List<NameValuePair> params = buildParameters();
    Map<String, Map> rawResults = queryExecutor.executeQuery(params);
    return unpackResults(rawResults);
  }

  /**
   * Get a value from raw Solr results according to how the query was set up.
   *
   * @param rawResults the full map of results deserialized from Solr's response
   * @return the subset of those results that were queried for
   */
  private Map<String, ?> unpackResults(Map<String, Map> rawResults) {
    if (this.isForRawResults()) {
      return rawResults;
    }
    if (this.getFacet().isPresent()) {
      Map<String, Map> facetFields =
          (Map<String, Map>) rawResults.get("facet_counts").get("facet_fields");
      return facetFields.get(this.getFacet().get()); //We expect facet field to be the first element of the list
    } else {
      return (Map<String, ?>) rawResults.get("response");
    }
  }

  public abstract Builder toBuilder();

  public abstract int getFacetLimit();

  public abstract String getQuery();

  public abstract Optional<String> getCursor();

  public abstract boolean isSimple();

  public abstract boolean isForRawResults();

  public abstract boolean isCsvSearch();

  public abstract boolean isRssSearch();

  public abstract boolean isJournalSearch();

  public abstract boolean isPartialSearch();

  public abstract Optional<String> getFacet();

  public abstract int getFacetMinCount();

  public abstract int getStart();

  public abstract int getRows();

  public abstract Optional<SolrSearchApi.SearchCriterion> getSortOrder();

  public abstract List<String> getJournalKeys();

  public abstract List<String> getArticleTypes();
  public abstract List<String> getArticleTypesToExclude();

  public abstract List<String> getSubjects();

  public abstract List<String> getAuthors();

  public abstract List<String> getSections();

  public abstract Optional<SolrSearchApi.SearchCriterion> getDateRange();

  @Nullable public abstract String getStartDate();

  @Nullable  public abstract String getEndDate();

  public abstract Map<String, String> getRawParameters();

  public abstract List<String> getFilterQueries();

  public static Builder builder() {
    return new AutoValue_ArticleSearchQuery.Builder()
      .setArticleTypes(ImmutableList.of())
      .setArticleTypesToExclude(ImmutableList.of())
      .setAuthors(ImmutableList.of())
      .setCsvSearch(false)
      .setFacetLimit(100)
      .setFacetMinCount(0)
      .setFilterQueries(ImmutableList.of())
      .setForRawResults(false)
      .setJournalKeys(ImmutableList.of())
      .setJournalSearch(false)
      .setQuery("*:*")
      .setPartialSearch(false)
      .setRawParameters(ImmutableMap.of())
      .setRows(0)
      .setRssSearch(false)
      .setSections(ImmutableList.of())
      .setSimple(false)
      .setStart(0)
      .setSubjects(ImmutableList.of());
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract ArticleSearchQuery build();

    /**
     * Set the raw search query
     *
     * @param query raw string of text to search for
     */
    public abstract Builder setQuery(String query);

    /**
     * Set the search type. Simple search uses dismax in Solr, and is represented in the search URL
     * as the "q" parameter. Advanced search does not use dismax in Solr, and is represented in the
     * URL as the "unformattedQuery" parameter.
     */
    public abstract Builder setSimple(boolean isSimple);

    /**
     * @param isForRawResults Flag the search to return raw results. Is only used to retrieve Solr stats.
     */
    public abstract Builder setForRawResults(boolean forRawResults);

    /**
     * @param isPartialSearch Flag the search to search partial documents. Only used when searching
     *                        For which section a keyword appears in.
     */
    public abstract Builder setPartialSearch(boolean partialSearch);

    /**
     * @param isRssSearch Flag the search to return only fields used by the RSS view
     */
    public abstract Builder setRssSearch(boolean rssSearch);

    /**
     * @param isCsvSearch Flag the search to return only fields used by the RSS view
     */
    public abstract Builder setCsvSearch(boolean csvSearch);

    /**
     * @param isJournalSearch Flag the search to return only fields used by the DoiToJournalResolutionService
     */
    public abstract Builder setJournalSearch(boolean journalSearch);

    /**
     * @param filterQueries a list of additional filter queries to be executed in Solr
     */
    public abstract Builder setFilterQueries(List<String> filterQueries);

    /**
     * @param facet the facet to search for as it is stored in Solr. Setting this will also set the
     *              search itself as a "faceted" search.
     */
    public abstract Builder setFacet(String facet);

    /**
     * @param facetLimit maximum number of faceted results to return
     */
    public abstract Builder setFacetLimit(int facetLimit);

    /**
     * @param facetMinCount minimum number of facets to use
     */
    public abstract Builder setFacetMinCount(int facetMinCount);

    /**
     * @param start the start position to query from in Solr
     */
    public abstract Builder setStart(int start);

    /**
     * @param rows the number of results to return from the Solr search
     */
    public abstract Builder setRows(int rows);

    /**
     * @param sortOrder the sort order of the results returned from Solr
     */
    public abstract Builder setSortOrder(SolrSearchApi.SearchCriterion sortOrder);

    /**
     * @param journalKeys set the journals to filter by
     */
    public abstract Builder setJournalKeys(List<String> journalKeys);

    /**
     * @param articleTypes set the article types to filter by
     */
    public abstract Builder setArticleTypes(List<String> articleTypes);

    /**
     * @param articleTypesToExclude set the article types to exclude
     */
    public abstract Builder setArticleTypesToExclude(List<String> articleTypesToExclude);

    /**
     * @param subjects set the subjects to filter by
     */
    public abstract Builder setSubjects(List<String> subjects);

    /**
     * @param authors set the authors to filter by
     */
    public abstract Builder setAuthors(List<String> authors);

    /**
     * @param sections set the sections to filter by
     */
    public abstract Builder setSections(List<String> sections);

    /**
     * @param dateRange set the date range to filter by
     */
    public abstract Builder setDateRange(@Nullable SolrSearchApi.SearchCriterion dateRange);

    public abstract Builder setStartDate(String startDate);

    public abstract Builder setEndDate(String endDate);

    public abstract Builder setCursor(String cursor);

    /**
     * @param rawParameters flag the query to use raw parameters. Is only used to retrieve Solr stats.
     */
    public abstract Builder setRawParameters(Map<String, String> rawParameters);
  }
}
