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

import static org.ambraproject.wombat.service.remote.ArticleSearchQuery.ARTICLE_TYPE_FACET_FIELD;
import static org.ambraproject.wombat.service.remote.ArticleSearchQuery.ARTICLE_TYPE_TAG;
import static org.ambraproject.wombat.service.remote.ArticleSearchQuery.JOURNAL_KEY_FIELD;
import static org.ambraproject.wombat.service.remote.ArticleSearchQuery.JOURNAL_TAG;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import org.ambraproject.wombat.util.UrlParamBuilder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.NameValuePair;

public class SolrQueryBuilder {
  private static final int THREAD_PER_FACET = -1;

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
      for (ArticleSearchQuery.Facet field: asq.getFacetFields()) {
        String query = field.getExcludeKey()
          .map(excludeKey->String.format("{!ex=%s}", excludeKey)).orElse("") +
          field.getField();
        params.add("facet.field", query);
      }
      params.add("facet.mincount", Integer.toString(asq.getFacetMinCount()));
      params.add("facet.limit", Integer.toString(asq.getFacetLimit()));
      params.add("facet.threads", Integer.toString(THREAD_PER_FACET));
      params.add("json.nl", "map");
    } else {
      params.add("facet", "false");
    }

    List<String> fields = asq.getFields().orElse(ArticleSearchQuery.ARTICLE_FIELDS);
    params.add("fl", Joiner.on(",").join(fields));

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
      params.add("fq", buildOrSearchClause(JOURNAL_KEY_FIELD,
                                           asq.getJournalKeys(),
                                           JOURNAL_TAG));
    }

    if (!CollectionUtils.isEmpty(asq.getArticleTypes())) {
      params.add("fq", buildOrSearchClause(ARTICLE_TYPE_FACET_FIELD,
                                           asq.getArticleTypes(),
                                           ARTICLE_TYPE_TAG));
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

  private static String buildSearchClause(String field, List<String> clauses, String joiner, Optional<String> rawTag) {
    String tag = rawTag.map(t->String.format("{!tag=%s}", t)).orElse("");
    return tag + clauses.stream().map(
      clause -> field + ":" + (clause.equals("*") ? "*" : ("\"" + clause + "\""))
    ).collect(Collectors.joining(joiner));
  }

  static String buildAndSearchClause(String what, List<String> clauses) {
    return buildSearchClause(what, clauses, " AND ", Optional.empty());
  }

  static String buildOrSearchClause(String what, List<String> clauses) {
    return buildSearchClause(what, clauses, " OR ", Optional.empty());
  }

  static String buildOrSearchClause(String what, List<String> clauses, String tag) {
    return buildSearchClause(what, clauses, " OR ", Optional.of(tag));
  }
}
