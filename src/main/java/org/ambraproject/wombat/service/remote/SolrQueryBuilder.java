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
import java.util.stream.Collectors;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class SolrQueryBuilder {
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


  public static List<NameValuePair> buildParameters(ArticleSearchQuery asq) {
    List<NameValuePair> params = new ArrayList<>();

    if (asq.isCsvSearch()) {
      params.add(new BasicNameValuePair("wt", "csv"));
    } else {
      params.add(new BasicNameValuePair("wt", "json"));
    }

    if (asq.isPartialSearch()) {
      params.add(new BasicNameValuePair("qf", "doc_partial_body"));
      params.add(new BasicNameValuePair("fl", "*"));
      params.add(new BasicNameValuePair("fq", "doc_type:partial"));
    } else {
      params.add(new BasicNameValuePair("fq", "doc_type:full"));
    }

    params.add(new BasicNameValuePair("fq", "!article_type_facet:\"Issue Image\""));
    for (String filterQuery : asq.getFilterQueries()) {
      params.add(new BasicNameValuePair("fq", filterQuery));
    }

    if (asq.getStart() > 0) {
      params.add(new BasicNameValuePair("start", Integer.toString(asq.getStart())));
    }
    params.add(new BasicNameValuePair("rows", Integer.toString(asq.getRows())));

    params.add(new BasicNameValuePair("hl", "false"));

    String queryString = asq.getQuery();
    params.add(new BasicNameValuePair("q", queryString));
    if (asq.isSimple()) {
      // Use the dismax query parser, recommended for all user-entered queries.
      // See https://wiki.apache.org/solr/DisMax
      params.add(new BasicNameValuePair("defType", "dismax"));
    }

    if (asq.getFacet().isPresent()) {
      params.add(new BasicNameValuePair("facet", "true"));
      params.add(new BasicNameValuePair("facet.field", asq.getFacet().get()));
      params.add(new BasicNameValuePair("facet.mincount", Integer.toString(asq.getFacetMinCount())));
      params.add(new BasicNameValuePair("facet.limit", Integer.toString(asq.getFacetLimit())));
      params.add(new BasicNameValuePair("json.nl", "map"));
    } else if (asq.isRssSearch()) {
      params.add(new BasicNameValuePair("facet", "false"));
      params.add(new BasicNameValuePair("fl", RSS_FIELDS));
    } else if (asq.isCsvSearch()) {
      params.add(new BasicNameValuePair("facet", "false"));
      params.add(new BasicNameValuePair("fl", CSV_FIELDS));
    } else if (asq.isJournalSearch()) {
      params.add(new BasicNameValuePair("facet", "false"));
      params.add(new BasicNameValuePair("fl", JOURNAL_FIELDS));
    } else {
      params.add(new BasicNameValuePair("facet", "false"));
      params.add(new BasicNameValuePair("fl", ARTICLE_FIELDS));
    }

    asq.getCursor().ifPresent(cursor -> params.add(new BasicNameValuePair("cursorMark", cursor)));

    setQueryFilters(asq, params);

    for (Map.Entry<String, String> entry : asq.getRawParameters().entrySet()) {
      params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
    }

    return params;
  }

  public static void setQueryFilters(ArticleSearchQuery asq, List<NameValuePair> params) {
    if (asq.getSortOrder().isPresent()) {
      String sortOrderStr = asq.getSortOrder().get().getValue() + ",id desc";
      params.add(new BasicNameValuePair("sort", sortOrderStr));
    }

    if (asq.getDateRange().isPresent()) {
      String dateRangeStr = asq.getDateRange().get().getValue();
      if (!Strings.isNullOrEmpty(dateRangeStr)) {
        params.add(new BasicNameValuePair("fq", "publication_date:" + dateRangeStr));
      }
    }
    if (!CollectionUtils.isEmpty(asq.getJournalKeys())) {
      List<String> crossPublishedJournals = asq.getJournalKeys().stream()
          .map(journalKey -> "journal_key:" + journalKey).collect(Collectors.toList());
      params.add(new BasicNameValuePair("fq", Joiner.on(" OR ").join(crossPublishedJournals)));
    }

    if (!CollectionUtils.isEmpty(asq.getArticleTypes())) {
      List<String> articleTypeQueryList = asq.getArticleTypes().stream()
          .map(articleType ->
          {
            String articleTypeStr = articleType.equals("*") ? articleType : "\"" + articleType + "\"";
            return "article_type_facet:" + articleTypeStr;
          })
          .collect(Collectors.toList());
      params.add(new BasicNameValuePair("fq", Joiner.on(" OR ").join(articleTypeQueryList)));
    }

    if (!CollectionUtils.isEmpty(asq.getArticleTypesToExclude())) {
      List<String> articleTypeToExcludeQueryList = asq.getArticleTypesToExclude().stream()
          .map(articleType -> "!article_type_facet:\"" + articleType + "\"").collect(Collectors.toList());
      params.add(new BasicNameValuePair("fq", Joiner.on(" AND ").join(articleTypeToExcludeQueryList)));
    }

    if (!CollectionUtils.isEmpty(asq.getSubjects())) {
      params.add(new BasicNameValuePair("fq", buildSearchClause("subject", asq.getSubjects())));
    }

    if (!CollectionUtils.isEmpty(asq.getAuthors())) {
      params.add(new BasicNameValuePair("fq", buildSearchClause("author", asq.getAuthors())));
    }

    if (!CollectionUtils.isEmpty(asq.getSections())) {
      List<String> sectionQueryList = new ArrayList<>();
      for (String section : asq.getSections()) {
        //Convert friendly section name to Solr field name TODO:clean this up
        section = section.equals("References") ? "reference" : section;
        sectionQueryList.add(section.toLowerCase().replace(' ', '_'));
      }
      params.add(new BasicNameValuePair("qf", Joiner.on(" OR ").join(sectionQueryList)));
    }
  }

  static String buildSearchClause(String what, List<String> clauses) {
    return clauses.stream().map(clause -> what + ":\"" + clause + "\"").collect(Collectors.joining(" AND "));
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
  public static Map<String, ?> search(ArticleSearchQuery asq, QueryExecutor queryExecutor) throws IOException {
    List<NameValuePair> params = buildParameters(asq);
    Map<String, Map> rawResults = queryExecutor.executeQuery(params);
    return unpackResults(asq, rawResults);
  }

  /**
   * Get a value from raw Solr results according to how the query was set up.
   *
   * @param rawResults the full map of results deserialized from Solr's response
   * @return the subset of those results that were queried for
   */
  private static Map<String, ?> unpackResults(ArticleSearchQuery asq, Map<String, Map> rawResults) {
    if (asq.isForRawResults()) {
      return rawResults;
    }
    if (asq.getFacet().isPresent()) {
      Map<String, Map> facetFields =
          (Map<String, Map>) rawResults.get("facet_counts").get("facet_fields");
      return facetFields.get(asq.getFacet().get()); //We expect facet field to be the first element of the list
    } else {
      return (Map<String, ?>) rawResults.get("response");
    }
  }
}
