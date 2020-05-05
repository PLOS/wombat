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

import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import javax.servlet.http.HttpServletRequest;
import com.google.auto.value.AutoValue;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Interface to the article search service for the application.
 */
public interface SolrSearchApi {
  @AutoValue
  @JsonAdapter(Result.Deserializer.class)
  public static abstract class Result {
    public abstract int getNumFound();
    public abstract int getStart();
    public abstract List<Map<String, Object>> getDocs();
    public abstract Optional<String> getNextCursorMark();
    public abstract Optional<FieldStatsResult<Date>> getPublicationDateStats();
    public abstract Optional<Map<String, Map<String,Integer>>> getFacets();
    static Builder builder() {
      return new AutoValue_SolrSearchApi_Result.Builder();
    }

    @AutoValue.Builder
    abstract static class Builder {
      abstract Result build();

      abstract Builder setNumFound(int numFound);
      abstract Builder setStart(int start);
      abstract Builder setDocs(List<Map<String, Object>> docs);
      abstract Builder setNextCursorMark(String nextCursorMark);
      abstract Builder setPublicationDateStats(FieldStatsResult<Date> publicationDateStats);
      abstract Builder setFacets(Map<String, Map<String,Integer>> facets);
    }

    public class Deserializer implements JsonDeserializer<Result> {
      @Override
      public Result deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject responseData = json.getAsJsonObject().getAsJsonObject("response");
        Type docsType = new TypeToken<List<Map<String, Object>>>() {}.getType();
        Builder builder = Result.builder()
          .setNumFound(responseData.get("numFound").getAsInt())
          .setStart(responseData.get("start").getAsInt())
          .setDocs(context.deserialize(responseData.get("docs"), docsType));
        if (json.getAsJsonObject().has("facet_counts")) {
          Type facetType = new TypeToken<Map<String, Map<String, Integer>>>() {}.getType();
          builder.setFacets(context.deserialize(json.getAsJsonObject()
                                                .getAsJsonObject("facet_counts")
                                                .getAsJsonObject("facet_fields"),
                                                facetType));
        }

        if (json.getAsJsonObject().has("stats")) {
          FieldStatsResult<Date> publicationDateStats = 
            context.deserialize(json.getAsJsonObject()
                                .getAsJsonObject("stats")
                                .getAsJsonObject("stats_fields")
                                .getAsJsonObject("publication_date"),
                                FieldStatsResult.class);
          builder.setPublicationDateStats(publicationDateStats);
        }
        if (json.getAsJsonObject().has("nextCursorMark")) {
          builder.setNextCursorMark(json.getAsJsonObject().get("nextCursorMark").getAsString());
        }
        return builder.build();
      }
    }
  }

  @AutoValue
  @JsonAdapter(FieldStatsResult.Deserializer.class)
  public static abstract class FieldStatsResult<T> {
    public abstract T getMin();
    public abstract T getMax();
    static <T> Builder<T> builder() {
      return new AutoValue_SolrSearchApi_FieldStatsResult.Builder<T>();
    }
    
    @AutoValue.Builder
      abstract static class Builder<T> {
      abstract FieldStatsResult<T> build();
      abstract Builder<T> setMin(T min);
      abstract Builder<T> setMax(T max);
    }

    public class Deserializer implements JsonDeserializer<FieldStatsResult<T>> {
      @Override
      public FieldStatsResult<T> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject responseData = json.getAsJsonObject();
        return FieldStatsResult.<T>builder()
          .setMin(context.deserialize(responseData.get("min"), Date.class))
          .setMax(context.deserialize(responseData.get("max"), Date.class))
          .build();
      }
    }
  }

  public static final Integer MAXIMUM_SOLR_RESULT_COUNT = 1000;

  /**
   * Performs a search and returns the raw results.
   *
   * @return deserialized JSON returned by the search server
   * @throws IOException
   */
  public Map<String, ?> rawSearch(ArticleSearchQuery query) throws IOException;

  /**
   * Queries Solr and returns the cooked results
   *
   * @param ArticleSearchQuery the query
   * @return results from Solr
   * @throws IOException
   */
  public Result cookedSearch(ArticleSearchQuery query) throws IOException;

  /**
   * Attempts to retrieve information about an article based on the DOI.
   *
   * @param doi identifies the article
   * @param site
   * @return information about the article, if it exists; otherwise an empty result set
   * @throws IOException
   */
  public Map<?, ?> lookupArticleByDoi(String doi, Site site) throws IOException;

  public Map<?, ?> lookupArticlesByDois(List<String> dois, Site site) throws IOException;

  /**
   * Adds a new property, link, to each search result passed in.  The value of this property is the correct URL to the
   * article on this environment.  Calling this method is necessary since article URLs need to be specific to the site
   * of the journal the article is published in, not the site in which the search results are being viewed.
   *
   * @param searchResults deserialized search results JSON
   * @param request       current request
   * @param site          site of the current request (for the search results)
   * @param siteSet       site set of the current request
   * @return searchResults decorated with the new property
   * @throws IOException
   */
  public Map<?, ?> addArticleLinks(Map<?, ?> searchResults, HttpServletRequest request, Site site, SiteSet
      siteSet) throws IOException;

  /**
   * Returns a list of all subject categories associated with all papers ever published
   * for the given journal.
   *
   * @param journalKey name of the journal in question
   * @return List of category names
   */
  public List<String> getAllSubjects(String journalKey) throws IOException;

  /**
   * Returns the number of articles, for a given journal, associated with all the subject
   * categories in the taxonomy.
   *
   * @param journalKey specifies the journal
   * @throws IOException
   */
  public Map<String, Integer> getAllSubjectCounts(String journalKey) throws IOException;
}
