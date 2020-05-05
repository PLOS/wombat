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

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

@AutoValue
public abstract class ArticleSearchQuery {
  /**
   * Type representing some restriction on the desired search results--for instance, a date range,
   * or a sort order. Implementations of SearchService should also provide appropriate
   * implementations of this interface.
   */
  public interface SearchCriterion {

    /**
     * @return description of this criterion, suitable for exposing in the UI
     */
    public String getDescription();

    /**
     * @return implementation-dependent String value specifying this criterion
     */
    public String getValue();
  }

  public abstract Builder toBuilder();

  public abstract int getFacetLimit();

  public abstract String getQuery();

  public abstract Optional<String> getCursor();

  public abstract boolean isSimple();

  public abstract boolean isRssSearch();

  public abstract boolean isJournalSearch();

  public abstract boolean isPartialSearch();

  public abstract ImmutableList<String> getFacetFields();

  public abstract int getFacetMinCount();

  public abstract int getStart();

  public abstract int getRows();

  public abstract Optional<SearchCriterion> getSortOrder();

  public abstract List<String> getJournalKeys();

  public abstract List<String> getArticleTypes();
  public abstract List<String> getArticleTypesToExclude();

  public abstract List<String> getSubjects();

  public abstract List<String> getAuthors();

  public abstract List<String> getSections();

  public abstract Optional<SearchCriterion> getDateRange();

  @Nullable public abstract String getStartDate();

  @Nullable public abstract String getEndDate();

  public abstract Optional<String> getStatsField();
  
  public static Builder builder() {
    return new AutoValue_ArticleSearchQuery.Builder()
      .setArticleTypes(ImmutableList.of())
      .setArticleTypesToExclude(ImmutableList.of())
      .setAuthors(ImmutableList.of())
      .setFacetFields(ImmutableList.of())
      .setFacetLimit(100)
      .setFacetMinCount(0)
      .setJournalKeys(ImmutableList.of())
      .setJournalSearch(false)
      .setQuery("*:*")
      .setPartialSearch(false)
      .setRows(1000)
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
     * @param isPartialSearch Flag the search to search partial documents. Only used when searching
     *                        For which section a keyword appears in.
     */
    public abstract Builder setPartialSearch(boolean partialSearch);

    /**
     * @param isRssSearch Flag the search to return only fields used by the RSS view
     */
    public abstract Builder setRssSearch(boolean rssSearch);

    /**
     * @param isJournalSearch Flag the search to return only fields used by the DoiToJournalResolutionService
     */
    public abstract Builder setJournalSearch(boolean journalSearch);

    /**
     * @param facet the facet to search for as it is stored in Solr. Setting this will also set the
     *              search itself as a "faceted" search.
     */
    public abstract Builder setFacetFields(ImmutableList<String> facet);

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
    public abstract Builder setSortOrder(SearchCriterion sortOrder);

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
    public abstract Builder setDateRange(@Nullable SearchCriterion dateRange);

    public abstract Builder setStartDate(String startDate);

    public abstract Builder setEndDate(String endDate);

    public abstract Builder setCursor(String cursor);

    public abstract Builder setStatsField(String statsField);
  }
}
