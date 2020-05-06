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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import javax.annotation.Nullable;
import javax.xml.bind.DatatypeConverter;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

@AutoValue
public abstract class ArticleSearchQuery {
  /**
   * Specifies the article fields in the solr schema that we want returned in the results.
   */
  public static final List<String> ARTICLE_FIELDS = ImmutableList.of("id", "eissn",
      "publication_date", "title", "title_display", "journal_name", "author_display",
      "article_type", "counter_total_all", "alm_scopusCiteCount", 
      "alm_mendeleyCount", "alm_twitterCount", "alm_facebookCount", "retraction",
      "expression_of_concern", "striking_image", "figure_table_caption", "journal_key");
  public static final List<String> RSS_FIELDS = ImmutableList.of("id", "publication_date",
      "title", "title_display", "journal_name", "author_display", "abstract",
      "abstract_primary_display");

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

  /**
   * Enumerates sort orders that we want to expose in the UI.
   */
  public static enum SolrSortOrder implements ArticleSearchQuery.SearchCriterion {

    // The order here determines the order in the UI.
    RELEVANCE("Relevance", "score desc,publication_date desc"),
    DATE_NEWEST_FIRST("Date, newest first", "publication_date desc"),
    DATE_OLDEST_FIRST("Date, oldest first", "publication_date asc"),
    MOST_VIEWS_30_DAYS("Most views, last 30 days", "counter_total_month desc"),
    MOST_VIEWS_ALL_TIME("Most views, all time", "counter_total_all desc"),
    MOST_CITED("Most cited, all time", "alm_scopusCiteCount desc"),
    MOST_BOOKMARKED("Most bookmarked", "alm_mendeleyCount desc"),
    MOST_SHARED("Most shared in social media", "sum(alm_twitterCount, alm_facebookCount) desc");

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
  public static enum SolrEnumeratedDateRange implements ArticleSearchQuery.SearchCriterion {

    ALL_TIME("All time", -1),
    LAST_YEAR("Last year", 365),

    // Clearly these are approximations given the different lengths of months.
    LAST_6_MONTHS("Last 6 months", 182),
    LAST_3_MONTHS("Last 3 months", 91);

    private String description;

    private int daysAgo;

    SolrEnumeratedDateRange(String description, int daysAgo) {
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

  public static class SolrExplicitDateRange implements ArticleSearchQuery.SearchCriterion {

    private String description;
    private Calendar startDate;
    private Calendar endDate;

    public SolrExplicitDateRange(String description, String startDate, String endDate) {
      this.description = description;

      Calendar startCal = Calendar.getInstance();
      Calendar endCal = Calendar.getInstance();
      startCal.setTimeZone(TimeZone.getTimeZone("UTC"));
      endCal.setTimeZone(TimeZone.getTimeZone("UTC"));

      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
      // getValue() method uses DatatypeConverter.printDateTime to convert the calendar object to a string.
      // However, this method uses the local time zone. Setting the time zone for the Calendar object doesn't
      // enforce UTC in the result of the printDateTime method but setting it in the simpleDateFormat does.
      simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      try {
        startCal.setTime(simpleDateFormat.parse(startDate));
        endCal.setTime(simpleDateFormat.parse(endDate));
      } catch (ParseException e) {
        throw new RuntimeException(e);
      }

      this.startDate = startCal;
      this.endDate = endCal;
    }

    @Override
    public String getDescription() {
      return description;
    }

    /**
     * @return a String representing part of the "fq" param to pass to solr that will restrict the date range
     * appropriately.  For example, "[2013-02-14T21:00:29.942Z TO 2013-08-15T21:00:29.942Z]". The String must be escaped
     * appropriately before being included in the URL.  The final http param passed to solr should look like
     * "fq=publication_date:[2013-02-14T21:00:29.942Z+TO+2013-08-15T21:00:29.942Z]".
     */
    @Override
    public String getValue() {
      return String.format("[%s TO %s]", DatatypeConverter.printDateTime(startDate),
          DatatypeConverter.printDateTime(endDate));
    }

  }

  public abstract Builder toBuilder();

  public abstract int getFacetLimit();

  public abstract String getQuery();

  public abstract Optional<String> getCursor();

  public abstract boolean isSimple();

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

  public abstract Optional<List<String>> getFields();
  
  public static Builder builder() {
    return new AutoValue_ArticleSearchQuery.Builder()
      .setArticleTypes(ImmutableList.of())
      .setArticleTypesToExclude(ImmutableList.of())
      .setAuthors(ImmutableList.of())
      .setFacetFields(ImmutableList.of())
      .setFacetLimit(100)
      .setFacetMinCount(1)
      .setJournalKeys(ImmutableList.of())
      .setQuery("*:*")
      .setPartialSearch(false)
      .setRows(1000)
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

    public abstract Builder setFields(List<String> fields);
  }
}
