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
import javax.annotation.Nullable;
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
        Integer revisionNumber = jsonObject.get("revisionNumber").getAsBigInteger().intValue();
        LocalDate publicationDate = LocalDate.parse(jsonObject.get("publicationDate").getAsString());
        Optional<String> specificUse = Optional.ofNullable(jsonObject.get("specificUse")).map(JsonElement::getAsString);
        RelatedArticleType relatedArticleType =
          RelatedArticleType.get(jsonObject.get("type").getAsString(), specificUse.orElse(null));
        return RelatedArticle.builder()
          .setDoi(jsonObject.get("doi").getAsString())
          .setTitle(jsonObject.get("title").getAsString())
          .setRevisionNumber(revisionNumber)
          .setPublicationDate(publicationDate)
          .setType(relatedArticleType)
          .setJournal(context.deserialize(jsonObject.get("journal"), RelatedArticleJournal.class))
          .build();
    }
}
  public abstract String getDoi();
  public abstract LocalDate getPublicationDate();
  public abstract Integer getRevisionNumber();
  public abstract String getTitle();
  public abstract RelatedArticleType getType();
  public abstract RelatedArticleJournal getJournal();

  public boolean isPublished() {
    return this.getRevisionNumber() != null;
  }

  public static Builder builder() {
    return new AutoValue_RelatedArticle.Builder();
  }

  @AutoValue.Builder
    public abstract static class Builder {
    public abstract RelatedArticle build();

    public abstract Builder setType(RelatedArticleType type);
    public abstract Builder setDoi(String doi);
    public abstract Builder setTitle(String title);
    public abstract Builder setRevisionNumber(Integer revisionNumber);
    public abstract Builder setPublicationDate(LocalDate date);
    public abstract Builder setJournal(RelatedArticleJournal journal);
  }
}
