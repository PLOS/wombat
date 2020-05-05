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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import org.ambraproject.wombat.util.UrlParamBuilder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.NameValuePair;

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
  private static final String JOURNAL_FIELDS = Joiner.on(',').join(
      "journal_key", "journal_name");

  public static List<NameValuePair> buildParameters(ArticleSearchQuery asq) {
    UrlParamBuilder params = UrlParamBuilder.params();

    params.add("wt", "json");

    if (asq.isPartialSearch()) {
      params.add("qf", "doc_partial_body");
      params.add("fl", "*");
      params.add("fq", "doc_type:partial");
    } else {
      params.add("fq", "doc_type:full");
    }

    params.add("fq", "!article_type_facet:\"Issue Image\"");

    if (asq.getStart() > 0) {
      params.add("start", Integer.toString(asq.getStart()));
    }
    params.add("rows", Integer.toString(asq.getRows()));

    params.add("hl", "false");

    String queryString = asq.getQuery();
    params.add("q", queryString);
    if (asq.isSimple()) {
      // Use the dismax query parser, recommended for all user-entered queries.
      // See https://wiki.apache.org/solr/DisMax
      params.add("defType", "dismax");
    }

    if (asq.getFacetFields().size() > 0) {
      params.add("facet", "true");
      for (String field: asq.getFacetFields()) {
        params.add("facet.field", field);
      }
      params.add("facet.mincount", Integer.toString(asq.getFacetMinCount()));
      params.add("facet.limit", Integer.toString(asq.getFacetLimit()));
      params.add("json.nl", "map");
    } else if (asq.isRssSearch()) {
      params.add("facet", "false");
      params.add("fl", RSS_FIELDS);
    } else if (asq.isJournalSearch()) {
      params.add("facet", "false");
      params.add("fl", JOURNAL_FIELDS);
    } else {
      params.add("facet", "false");
      params.add("fl", ARTICLE_FIELDS);
    }

    asq.getCursor().ifPresent(cursor -> params.add("cursorMark", cursor));

    setQueryFilters(asq, params);

    if (asq.getStatsField().isPresent()) {
      params.add("stats", "true");
      params.add("stats.field", asq.getStatsField().get());
    }

    return params.build();
  }

  public static void setQueryFilters(ArticleSearchQuery asq, UrlParamBuilder params) {
    if (asq.getSortOrder().isPresent()) {
      String sortOrderStr = asq.getSortOrder().get().getValue() + ",id desc";
      params.add("sort", sortOrderStr);
    }

    if (asq.getDateRange().isPresent()) {
      String dateRangeStr = asq.getDateRange().get().getValue();
      if (!Strings.isNullOrEmpty(dateRangeStr)) {
        params.add("fq", "publication_date:" + dateRangeStr);
      }
    }
    if (!CollectionUtils.isEmpty(asq.getJournalKeys())) {
      params.add("fq", buildOrSearchClause("journal_key", asq.getJournalKeys()));
    }

    if (!CollectionUtils.isEmpty(asq.getArticleTypes())) {
      params.add("fq", buildOrSearchClause("article_type_facet", asq.getArticleTypes()));
    }

    if (!CollectionUtils.isEmpty(asq.getArticleTypesToExclude())) {
      params.add("fq", buildAndSearchClause("!article_type_facet", asq.getArticleTypesToExclude()));
    }

    if (!CollectionUtils.isEmpty(asq.getSubjects())) {
      params.add("fq", buildAndSearchClause("subject", asq.getSubjects()));
    }

    if (!CollectionUtils.isEmpty(asq.getAuthors())) {
      params.add("fq", buildAndSearchClause("author", asq.getAuthors()));
    }

    if (!CollectionUtils.isEmpty(asq.getSections())) {
      List<String> sectionQueryList = new ArrayList<>();
      for (String section : asq.getSections()) {
        //Convert friendly section name to Solr field name TODO:clean this up
        section = section.equals("References") ? "reference" : section;
        sectionQueryList.add(section.toLowerCase().replace(' ', '_'));
      }
      params.add("qf", Joiner.on(" OR ").join(sectionQueryList));
    }
  }

  private static String buildSearchClause(String what, List<String> clauses, String joiner) {
    return clauses.stream().map(
      clause -> what + ":" + (clause.equals("*") ? "*" : ("\"" + clause + "\""))
    ).collect(Collectors.joining(joiner));
  }

  static String buildAndSearchClause(String what, List<String> clauses) {
    return buildSearchClause(what, clauses, " AND ");
  }

  static String buildOrSearchClause(String what, List<String> clauses) {
    return buildSearchClause(what, clauses, " OR ");
  }
}
