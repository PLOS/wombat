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

public class SearchQuery {

  /**
   * Specifies the article fields in the solr schema that we want returned in the results.
   */
  private static final String FL = "id,eissn,publication_date,title,cross_published_journal_name,author_display,"
      + "article_type,counter_total_all,alm_scopusCiteCount,alm_citeulikeCount,alm_mendeleyCount,alm_twitterCount,"
      + "alm_facebookCount,retraction,expression_of_concern";
  private static final int MAX_FACET_SIZE = 100;
  private static final int MIN_FACET_COUNT = 1;


  private final Optional<String> query;
  private final boolean isSimple;
  private final boolean isForRawResults;

  private final Optional<String> facet;

  private final int start;
  private final int rows;

  private final Optional<SolrSearchService.SearchCriterion> sortOrder;

  private final ImmutableList<String> journalKeys;
  private final ImmutableList<String> articleTypes;
  private final ImmutableList<String> subjects;
  private final Optional<SolrSearchService.SearchCriterion> dateRange;

  private final ImmutableMap<String, String> rawParameters;

  private SearchQuery(Builder builder) {
    this.query = getQueryString(builder.query);
    this.isSimple = builder.isSimple;
    this.isForRawResults = builder.isForRawResults;
    this.facet = Optional.fromNullable(builder.facet);
    this.start = builder.start;
    this.rows = builder.rows;
    this.sortOrder = Optional.fromNullable(builder.sortOrder);
    this.journalKeys = ImmutableList.copyOf(builder.journalKeys);
    this.articleTypes = ImmutableList.copyOf(builder.articleTypes);
    this.subjects = ImmutableList.copyOf(builder.subjects);
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
    params.add(new BasicNameValuePair("fq", "doc_type:full"));
    params.add(new BasicNameValuePair("fq", "!article_type_facet:\"Issue Image\""));

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
      params.add(new BasicNameValuePair("fl", FL));
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


  public static interface QueryExecutor {
    Map<String, Map> executeQuery(List<NameValuePair> params) throws IOException;
  }

  public Map<?, ?> getResults(QueryExecutor queryExecutor) throws IOException {
    List<NameValuePair> params = buildParameters();
    Map<String, Map> rawResults = queryExecutor.executeQuery(params);
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
    builder.facet = this.facet.orNull();
    builder.start = this.start;
    builder.rows = this.rows;
    builder.sortOrder = this.sortOrder.orNull();
    builder.journalKeys = this.journalKeys;
    builder.articleTypes = this.articleTypes;
    builder.subjects = this.subjects;
    builder.dateRange = this.dateRange.orNull();
    builder.rawParameters = this.rawParameters;
    return builder;
  }

  public static class Builder {
    private String query;
    private boolean isSimple;
    private boolean isForRawResults;

    private String facet;

    private int start;
    private int rows;

    private SolrSearchService.SearchCriterion sortOrder;

    private List<String> journalKeys = ImmutableList.of();
    private List<String> articleTypes = ImmutableList.of();
    private List<String> subjects = ImmutableList.of();
    private SolrSearchService.SearchCriterion dateRange;

    private Map<String, String> rawParameters = ImmutableMap.of();

    private Builder() {
    }

    public Builder setQuery(String query) {
      this.query = query;
      return this;
    }

    public Builder setSimple(boolean isSimple) {
      this.isSimple = isSimple;
      return this;
    }

    public Builder setForRawResults(boolean isForRawResults) {
      this.isForRawResults = isForRawResults;
      return this;
    }

    public Builder setFacet(String facet) {
      this.facet = facet;
      return this;
    }

    public Builder setStart(int start) {
      this.start = start;
      return this;
    }

    public Builder setRows(int rows) {
      this.rows = rows;
      return this;
    }

    public Builder setSortOrder(SolrSearchService.SearchCriterion sortOrder) {
      this.sortOrder = sortOrder;
      return this;
    }

    public Builder setJournalKeys(List<String> journalKeys) {
      this.journalKeys = journalKeys;
      return this;
    }

    public Builder setArticleTypes(List<String> articleTypes) {
      this.articleTypes = articleTypes;
      return this;
    }

    public Builder setSubjects(List<String> subjects) {
      this.subjects = subjects;
      return this;
    }

    public Builder setDateRange(SolrSearchService.SearchCriterion dateRange) {
      this.dateRange = dateRange;
      return this;
    }

    public Builder setRawParameters(Map<String, String> rawParameters) {
      this.rawParameters = rawParameters;
      return this;
    }

    public SearchQuery build() {
      return new SearchQuery(this);
    }

    public Builder setCommonQueryParams(SearchQuery searchQuery) {
      this.setQuery(searchQuery.getQuery().orNull())
          .setSimple(searchQuery.isSimple())
          .setArticleTypes(searchQuery.getArticleTypes())
          .setSubjects(searchQuery.getSubjects())
          .setDateRange(searchQuery.getDateRange().orNull());
      return this;
    }
  }

}
