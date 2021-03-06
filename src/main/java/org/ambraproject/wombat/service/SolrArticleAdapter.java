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

package org.ambraproject.wombat.service;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.ambraproject.wombat.service.remote.ArticleSearchQuery;
import org.ambraproject.wombat.service.remote.SolrSearchApi;
import org.apache.lucene.queryparser.classic.QueryParser;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Translates a Solr query result, representing an article's metadata
 */
public class SolrArticleAdapter implements Serializable {

  /**
   * An object representing one article author.
   * <p>
   * Solr provides bare strings for author names - {@code fullName} is the only field.
   */
  public static class Author implements Serializable {
    private final String fullName;

    private Author(String fullName) {
      this.fullName = Objects.requireNonNull(fullName);
    }

    public String getFullName() {
      return fullName;
    }
  }

  private final String doi; // non-null
  private final String title; // non-null
  private final String eIssn; // non-null
  private final String date; // non-null
  private final String strkImgURI; // nullable (forego Optional because we want it to be clean as an FTL model)
  private final boolean hasFigures;
  private final ImmutableList<Author> authors; // non-null
  private final String articleType; // non-null
  private final String journalKey; // non-null

  private SolrArticleAdapter(String doi, String title, String eIssn, String date, String strkImgURI,
                             boolean hasFigures, List<Author> authors, String articleType,
                             String journalKey) {
    this.doi = Objects.requireNonNull(doi);
    this.title = Objects.requireNonNull(title);
    this.eIssn = Objects.requireNonNull(eIssn);
    this.date = Objects.requireNonNull(date);
    this.strkImgURI = strkImgURI;
    this.hasFigures = hasFigures;
    this.authors = ImmutableList.copyOf(authors);
    this.articleType = Objects.requireNonNull(articleType);
    this.journalKey = Objects.requireNonNull(journalKey);
  }

  public static ArticleSearchQuery lookupArticlesByDoisQuery(List<String> dois) {
    String doiQueryString = dois.stream().map(doi -> "id:" + QueryParser.escape(doi))
        .collect(Collectors.joining(" OR "));

    return ArticleSearchQuery.builder()
      .setQuery(doiQueryString)
      .setStart(0)
      .setRows(dois.size()).build();
  }

  /**
   * Adapt a set of results, as provided by {@link org.ambraproject.wombat.service.remote.SolrSearchApi#search(org.ambraproject.wombat.service.remote.ArticleSearchQuery, org.ambraproject.wombat.config.site.Site)}.
   */
  public static List<SolrArticleAdapter> unpackSolrQuery(SolrSearchApi.Result solrResult) {
    List<Map<String, Object>> docs = solrResult.getDocs();
    return docs.stream().map(SolrArticleAdapter::adaptFromSolr).collect(Collectors.toList());
  }

  /**
   * Adapt an article object from a Solr query.
   *
   * @param solrArticle the map of Solr results representing the article
   * @return the extracted fields
   */
  public static SolrArticleAdapter adaptFromSolr(Map<String, Object> solrArticle) {
    String doi = (String) solrArticle.get("id");
    String title = Strings.isNullOrEmpty((String) solrArticle.get("title_display")) ? (String) solrArticle.get("title")
        : (String) solrArticle.get("title_display");
    String eIssn = (String) solrArticle.get("eissn");
    String date = (String) solrArticle.get("publication_date");
    String strkImgURI = (String) solrArticle.get("striking_image");

    Collection<?> figureTableCaption = (Collection<?>) solrArticle.get("figure_table_caption");
    boolean hasFigures = (figureTableCaption != null) && !figureTableCaption.isEmpty();

    List<String> solrAuthors = (List<String>) solrArticle.get("author_display");
    List<Author> authors = (solrAuthors != null) ? Lists.transform(solrAuthors, Author::new) : ImmutableList.of();
    String articleType = (String) solrArticle.get("article_type");
    String journalKey = (String) solrArticle.get("journal_key");

    return new SolrArticleAdapter(doi, title, eIssn, date, strkImgURI, hasFigures, authors,
        articleType, journalKey);
  }

  public String getDoi() {
    return doi;
  }

  public String getTitle() {
    return title;
  }

  public String geteIssn() {
    return eIssn;
  }

  public String getDate() {
    return date;
  }

  public String getStrkImgURI() {
    return strkImgURI;
  }

  public boolean getHasFigures() {
    return hasFigures;
  }

  public ImmutableList<Author> getAuthors() {
    return authors;
  }

  public String getArticleType() {
    return articleType;
  }

  public String getJournalKey() {
    return journalKey;
  }
}

