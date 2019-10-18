/*
 * Copyright (c) 2019 Public Library of Science
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

package org.ambraproject.wombat.model;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.Optional;

import com.google.auto.value.AutoValue;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.JsonAdapter;

@AutoValue
@JsonAdapter(RelatedArticle.Deserializer.class)
public abstract class RelatedArticle {
  public class Deserializer implements JsonDeserializer<RelatedArticle> {  
    @Override
    public RelatedArticle deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        Optional<Integer> revisionNumber = Optional.ofNullable(jsonObject.get("revisionNumber").getAsBigInteger().intValue());
        Optional<LocalDate> publicationDate = Optional.ofNullable(jsonObject.get("publicationDate").getAsString()).map(LocalDate::parse);

        return RelatedArticle.builder()
          .setDoi(jsonObject.get("doi").getAsString())
          .setTitle(Optional.ofNullable(jsonObject.get("title").getAsString()))
          .setRevisionNumber(revisionNumber)
          .setPublicationDate(publicationDate)
          .setType(jsonObject.get("type").getAsString())
          .build();
    }
}
  public abstract String getDoi();
  public abstract Optional<LocalDate> getPublicationDate();
  public abstract Optional<Integer> getRevisionNumber();
  public abstract Optional<String> getTitle();
  public abstract String getType();

  public boolean isPublished() {
    return this.getRevisionNumber().isPresent();
  }
    
  public static Builder builder() {
    return new AutoValue_RelatedArticle.Builder();
  }

  @AutoValue.Builder
    public abstract static class Builder {
    public abstract RelatedArticle build();

    public abstract Builder setType(String type);
    public abstract Builder setDoi(String doi);
    public abstract Builder setTitle(Optional<String> title);
    public abstract Builder setRevisionNumber(Optional<Integer> revisionNumber);
    public abstract Builder setPublicationDate(Optional<LocalDate> title);
  }
}
