package org.ambraproject.wombat.service.remote;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.ambraproject.wombat.util.ListUtil;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ArticleSearchQuery {

  /**
   * Specifies the article fields in the solr schema that we want returned in the results.
   */
  private static final String ARTICLE_FIELDS = Joiner.on(',').join(ImmutableList.copyOf(new String[]{
      "id", "eissn", "publication_date", "title", "cross_published_journal_name", "author_display", "article_type",
      "counter_total_all", "alm_scopusCiteCount", "alm_citeulikeCount", "alm_mendeleyCount", "alm_twitterCount",
      "alm_facebookCount", "retraction", "expression_of_concern"}));
  private static final int MAX_FACET_SIZE = 100;
  private static final int MIN_FACET_COUNT = 1;


  private final Optional<String> query;
  private final boolean isSimple;
  private final boolean isForRawResults;
  private final boolean isPartialSearch;

  private final ImmutableList<String> filterQueries;

  private final Optional<String> facet;

  private final int start;
  private final int rows;

  private final Optional<SolrSearchService.SearchCriterion> sortOrder;

  private final ImmutableList<String> journalKeys;
  private final ImmutableList<String> articleTypes;
  private final ImmutableList<String> subjects;
  private final ImmutableList<String> authors;
  private final ImmutableList<String> sections;
  private final Optional<SolrSearchService.SearchCriterion> dateRange;

  private final ImmutableMap<String, String> rawParameters;

  private ArticleSearchQuery(Builder builder) {
    this.query = getQueryString(builder.query);
    this.isSimple = builder.isSimple;
    this.isForRawResults = builder.isForRawResults;
    this.isPartialSearch = builder.isPartialSearch;
    this.filterQueries = ImmutableList.copyOf(builder.filterQueries);
    this.facet = Optional.fromNullable(builder.facet);
    this.start = builder.start;
    this.rows = builder.rows;
    this.sortOrder = Optional.fromNullable(builder.sortOrder);
    this.journalKeys = ImmutableList.copyOf(builder.journalKeys);
    this.articleTypes = ImmutableList.copyOf(builder.articleTypes);
    this.subjects = ImmutableList.copyOf(builder.subjects);
    this.authors = ImmutableList.copyOf(builder.authors);
    this.sections = ImmutableList.copyOf(builder.sections);
    this.dateRange = Optional.fromNullable(builder.dateRange);
    this.rawParameters = ImmutableMap.copyOf(builder.rawParameters);
  }

  private static Optional<String> getQueryString(String query) {
    // Treat empty string as absent query, which will be sent to Solr as "*:*"
    return Strings.isNullOrEmpty(query) ? Optional.<String>absent() : Optional.of(query);
  }

  @VisibleForTesting
  List<NameValuePair> buildParameters() {
    List<NameValuePair> params = new ArrayList<>();
    params.add(new BasicNameValuePair("wt", "json"));

    if (isPartialSearch) {
      params.add(new BasicNameValuePair("qf", "doc_partial_body"));
      params.add(new BasicNameValuePair("fl", "*"));
      params.add(new BasicNameValuePair("fq", "doc_type:partial"));
    } else {
      params.add(new BasicNameValuePair("fq", "doc_type:full"));
    }

    params.add(new BasicNameValuePair("fq", "!article_type_facet:\"Issue Image\""));
    for (String filterQuery : filterQueries) {
      params.add(new BasicNameValuePair("fq", filterQuery));
    }

    if (start > 0) {
      params.add(new BasicNameValuePair("start", Integer.toString(start)));
    }
    params.add(new BasicNameValuePair("rows", Integer.toString(rows)));

    params.add(new BasicNameValuePair("hl", "false"));

    String queryString = query.or("*:*");
    params.add(new BasicNameValuePair("q", queryString));
    if (query.isPresent() && isSimple) {
      // Use the dismax query parser, recommended for all user-entered queries.
      // See https://wiki.apache.org/solr/DisMax
      params.add(new BasicNameValuePair("defType", "dismax"));
    }

    if (facet.isPresent()) {
      params.add(new BasicNameValuePair("facet", "true"));
      params.add(new BasicNameValuePair("facet.field", facet.get()));
      params.add(new BasicNameValuePair("facet.mincount", Integer.toString(MIN_FACET_COUNT)));
      params.add(new BasicNameValuePair("facet.limit", Integer.toString(MAX_FACET_SIZE)));
      params.add(new BasicNameValuePair("json.nl", "map"));
    } else {
      params.add(new BasicNameValuePair("facet", "false"));
      params.add(new BasicNameValuePair("fl", ARTICLE_FIELDS));
    }

    setQueryFilters(params);

    for (Map.Entry<String, String> entry : rawParameters.entrySet()) {
      params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
    }

    return params;
  }

  @VisibleForTesting
  void setQueryFilters(List<NameValuePair> params) {
    if (sortOrder.isPresent()) {
      String sortOrderStr = sortOrder.get().getValue() + ",id desc";
      params.add(new BasicNameValuePair("sort", sortOrderStr));
    }

    if (dateRange.isPresent()) {
      String dateRangeStr = dateRange.get().getValue();
      if (!Strings.isNullOrEmpty(dateRangeStr)) {
        params.add(new BasicNameValuePair("fq", "publication_date:" + dateRangeStr));
      }
    }
    if (!ListUtil.isNullOrEmpty(journalKeys)) {
      List<String> crossPublishedJournals = new ArrayList<>();
      for (String journalKey : journalKeys) {
        crossPublishedJournals.add("cross_published_journal_key:" + journalKey);
      }
      params.add(new BasicNameValuePair("fq", Joiner.on(" OR ").join(crossPublishedJournals)));
    }

    if (!ListUtil.isNullOrEmpty(articleTypes)) {
      List<String> articleTypeQueryList = new ArrayList<>();
      for (String articleType : articleTypes) {
        articleTypeQueryList.add("article_type_facet:\"" + articleType + "\"");
      }
      params.add(new BasicNameValuePair("fq", Joiner.on(" OR ").join(articleTypeQueryList)));
    }

    if (!ListUtil.isNullOrEmpty(subjects)) {
      params.add(new BasicNameValuePair("fq", buildSubjectClause(subjects)));
    }

    if (!ListUtil.isNullOrEmpty(authors)) {
      params.add(new BasicNameValuePair("fq", buildAuthorClause(authors)));
    }

    if (!ListUtil.isNullOrEmpty(sections)) {
      List<String> sectionQueryList = new ArrayList<>();
      for (String section : sections) {
        //Convert friendly section name to Solr field name TODO:clean this up
        section = section.equals("References") ? "reference" : section;
        sectionQueryList.add(section.toLowerCase().replace(' ', '_'));
      }
      params.add(new BasicNameValuePair("qf", Joiner.on(" OR ").join(sectionQueryList)));
    }
  }

  @VisibleForTesting
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

  @VisibleForTesting
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
  public Map<?, ?> search(QueryExecutor queryExecutor) throws IOException {
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
  private Map<?, ?> unpackResults(Map<String, Map> rawResults) {
    if (isForRawResults) {
      return rawResults;
    }
    if (facet.isPresent()) {
      Map<String, Map> facetFields = (Map<String, Map>) rawResults.get("facet_counts").get("facet_fields");
      return facetFields.get(facet.get()); //We expect facet field to be the first element of the list
    } else {
      return (Map<?, ?>) rawResults.get("response");
    }
  }


  /*
   * These getters exist mainly for the benefit of SearchController.rebuildUrlParameters.
   * In general, avoid calling them in favor of encapsulating the fields privately.
   */

  public Optional<String> getQuery() {
    return query;
  }

  public boolean isSimple() {
    return isSimple;
  }

  public boolean isForRawResults() {
    return isForRawResults;
  }

  public Optional<String> getFacet() {
    return facet;
  }

  public int getStart() {
    return start;
  }

  public int getRows() {
    return rows;
  }

  public Optional<SolrSearchService.SearchCriterion> getSortOrder() {
    return sortOrder;
  }

  public ImmutableList<String> getJournalKeys() {
    return journalKeys;
  }

  public ImmutableList<String> getArticleTypes() {
    return articleTypes;
  }

  public ImmutableList<String> getSubjects() {
    return subjects;
  }

  public ImmutableList<String> getAuthors() {
    return authors;
  }

  public ImmutableList<String> getSections() {
    return sections;
  }

  public Optional<SolrSearchService.SearchCriterion> getDateRange() {
    return dateRange;
  }

  public ImmutableMap<String, String> getRawParameters() {
    return rawParameters;
  }


  public static Builder builder() {
    return new Builder();
  }

  public Builder copy() {
    Builder builder = builder();
    builder.query = this.query.orNull();
    builder.isSimple = this.isSimple;
    builder.isForRawResults = this.isForRawResults;
    builder.filterQueries = this.filterQueries;
    builder.facet = this.facet.orNull();
    builder.start = this.start;
    builder.rows = this.rows;
    builder.sortOrder = this.sortOrder.orNull();
    builder.journalKeys = this.journalKeys;
    builder.articleTypes = this.articleTypes;
    builder.subjects = this.subjects;
    builder.dateRange = this.dateRange.orNull();
    builder.authors = this.authors;
    builder.sections = this.sections;
    builder.rawParameters = this.rawParameters;
    return builder;
  }

  public static class Builder {
    private String query;
    private boolean isSimple;
    private boolean isForRawResults;
    private boolean isPartialSearch;

    private List<String> filterQueries = ImmutableList.of();

    private String facet;

    private int start;
    private int rows;

    private SolrSearchService.SearchCriterion sortOrder;

    private List<String> journalKeys = ImmutableList.of();
    private List<String> articleTypes = ImmutableList.of();
    private List<String> subjects = ImmutableList.of();
    private List<String> authors = ImmutableList.of();
    private List<String> sections = ImmutableList.of();
    private SolrSearchService.SearchCriterion dateRange;

    private Map<String, String> rawParameters = ImmutableMap.of();

    private Builder() {
    }

    /**
     * Set the raw search query
     *
     * @param query raw string of text to search for
     */
    public Builder setQuery(String query) {
      this.query = query;
      return this;
    }

    /**
     * Set the search type. Simple search uses dismax in Solr, and is represented in the search URL
     * as the "q" parameter. Advanced search does not use dismax in Solr, and is represented in the
     * URL as the "unformattedQuery" parameter.
     */
    public Builder setSimple(boolean isSimple) {
      this.isSimple = isSimple;
      return this;
    }

    /**
     * @param isForRawResults Flag the search to return raw results. Is only used to retrieve Solr stats.
     */
    public Builder setForRawResults(boolean isForRawResults) {
      this.isForRawResults = isForRawResults;
      return this;
    }

    /**
     * @param isPartialSearch flag the search to search partial documents. Only used when searching
     *                        For which section a keyword appears in.
     */
    public Builder setIsPartialSearch(boolean isPartialSearch) {
      this.isPartialSearch = isPartialSearch;
      return this;
    }

    /**
     * @param filterQueries a list of additional filter queries to be executed in Solr
     */
    public Builder setFilterQueries(List<String> filterQueries) {
      this.filterQueries = filterQueries;
      return this;
    }

    /**
     * @param facet the facet to search for as it is stored in Solr. Setting this will also set the
     *              search itself as a "faceted" search.
     */
    public Builder setFacet(String facet) {
      this.facet = facet;
      return this;
    }

    /**
     * @param start the start position to query from in Solr
     */
    public Builder setStart(int start) {
      this.start = start;
      return this;
    }

    /**
     * @param rows the number of results to return from the Solr search
     */
    public Builder setRows(int rows) {
      this.rows = rows;
      return this;
    }

    /**
     * @param sortOrder the sort order of the results returned from Solr
     */
    public Builder setSortOrder(SolrSearchService.SearchCriterion sortOrder) {
      this.sortOrder = sortOrder;
      return this;
    }

    /**
     * @param journalKeys set the journals to filter by
     */
    public Builder setJournalKeys(List<String> journalKeys) {
      this.journalKeys = journalKeys;
      return this;
    }

    /**
     * @param articleTypes set the article types to filter by
     */
    public Builder setArticleTypes(List<String> articleTypes) {
      this.articleTypes = articleTypes;
      return this;
    }

    /**
     * @param subjects set the subjects to filter by
     */
    public Builder setSubjects(List<String> subjects) {
      this.subjects = subjects;
      return this;
    }

    /**
     * @param authors set the authors to filter by
     */
    public Builder setAuthors(List<String> authors) {
      this.authors = authors;
      return this;
    }

    /**
     * @param sections set the sections to filter by
     */
    public Builder setSections(List<String> sections) {
      this.sections = sections;
      return this;
    }

    /**
     * @param dateRange set the date range to filter by
     */
    public Builder setDateRange(SolrSearchService.SearchCriterion dateRange) {
      this.dateRange = dateRange;
      return this;
    }

    /**
     * @param rawParameters flag the query to use raw parameters. Is only used to retrieve Solr stats.
     */
    public Builder setRawParameters(Map<String, String> rawParameters) {
      this.rawParameters = rawParameters;
      return this;
    }

    public ArticleSearchQuery build() {
      return new ArticleSearchQuery(this);
    }
  }
}
