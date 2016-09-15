package org.ambraproject.wombat.service;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

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

  private SolrArticleAdapter(String doi, String title, String eIssn, String date, String strkImgURI,
                             boolean hasFigures, List<Author> authors, String articleType) {
    this.doi = Objects.requireNonNull(doi);
    this.title = Objects.requireNonNull(title);
    this.eIssn = Objects.requireNonNull(eIssn);
    this.date = Objects.requireNonNull(date);
    this.strkImgURI = strkImgURI;
    this.hasFigures = hasFigures;
    this.authors = ImmutableList.copyOf(authors);
    this.articleType = Objects.requireNonNull(articleType);
  }

  /**
   * Adapt a set of results, as provided by {@link org.ambraproject.wombat.service.remote.SolrSearchApi#search(org.ambraproject.wombat.service.remote.ArticleSearchQuery)}.
   */
  public static List<SolrArticleAdapter> unpackSolrQuery(Map<String, ?> solrResult) {
    List<Map<String, ?>> docs = (List<Map<String, ?>>) solrResult.get("docs");
    return docs.stream().map(SolrArticleAdapter::adaptFromSolr).collect(Collectors.toList());
  }

  /**
   * Adapt an article object from a Solr query.
   *
   * @param solrArticle the map of Solr results representing the article
   * @return the extracted fields
   */
  public static SolrArticleAdapter adaptFromSolr(Map<String, ?> solrArticle) {
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

    return new SolrArticleAdapter(doi, title, eIssn, date, strkImgURI, hasFigures, authors, articleType);
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
}

