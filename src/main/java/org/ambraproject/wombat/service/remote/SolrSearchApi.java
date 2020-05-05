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
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.google.auto.value.AutoValue;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.ambraproject.wombat.util.UriUtil;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Class to use to search a solr api. 
 */
public class SolrSearchApi {
  public static final Integer MAXIMUM_SOLR_RESULT_COUNT = 1000;

  private static final Logger log = LogManager.getLogger(SolrSearchApi.class);

  @Autowired
  private JsonService jsonService;

  @Autowired
  private CachedRemoteService<Reader> cachedRemoteReader;

  @Autowired
  private RuntimeConfiguration runtimeConfiguration;

  @AutoValue
  @JsonAdapter(Result.Deserializer.class)
  public static abstract class Result {
    public abstract Builder toBuilder();
    public abstract int getNumFound();
    public abstract int getStart();
    public abstract List<Map<String, Object>> getDocs();
    public abstract Optional<String> getNextCursorMark();
    public abstract Optional<FieldStatsResult<Date>> getPublicationDateStats();
    public abstract Optional<Map<String, Map<String,Integer>>> getFacets();
    public static Builder builder() {
      return new AutoValue_SolrSearchApi_Result.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
      public abstract Result build();

      public abstract Builder setNumFound(int numFound);
      public abstract Builder setStart(int start);
      public abstract Builder setDocs(List<Map<String, Object>> docs);
      public abstract Builder setNextCursorMark(String nextCursorMark);
      public abstract Builder setPublicationDateStats(FieldStatsResult<Date> publicationDateStats);
      public abstract Builder setFacets(Map<String, Map<String,Integer>> facets);
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

  /**
   * Queries Solr and returns the cooked results
   *
   * @param ArticleSearchQuery the query
   * @return results from Solr
   * @throws IOException
   */
  public Result search(ArticleSearchQuery query) throws IOException {
    List<NameValuePair> params = SolrQueryBuilder.buildParameters(query);
    URI uri = getSolrUri(params);
    log.debug("Solr request executing: " + uri);
    return jsonService.requestObject(cachedRemoteReader, new HttpGet(uri), Result.class);
  }

  private URI getSolrUri(List<NameValuePair> params) {
    try {
      URL solrServer = runtimeConfiguration.getSolrUrl();
      return new URL(solrServer, "select?" + UriUtil.formatParams(params)).toURI();
    } catch (MalformedURLException | URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
