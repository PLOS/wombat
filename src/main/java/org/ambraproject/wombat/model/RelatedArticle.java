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
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;


@AutoValue
public abstract class RelatedArticle {
  @AutoValue
  public abstract static class ArticleMetadata {
    public abstract String getDoi();
    public abstract Optional<LocalDate> getPublicationDate();
    public abstract Optional<Integer> getRevisionNumber();
    public abstract Optional<String> getTitle();

    public boolean isPublished() {
      return this.getRevisionNumber().isPresent();
    }

    @AutoValue.Builder
    public abstract static class Builder {
      public abstract ArticleMetadata build();

      public abstract Builder setDoi(String doi);

      public abstract Builder setTitle(Optional<String> title);

      public abstract Builder setRevisionNumber(Optional<Integer> revisionNumber);

      public abstract Builder setPublicationDate(Optional<LocalDate> title);
    }

    public static Builder builder() {
      return new AutoValue_RelatedArticle_ArticleMetadata.Builder();
    }

    public static ArticleMetadata fromMap(Map<String, Object> map) {
      Optional<Integer> revisionNumber = Optional.ofNullable((Double) map.get("revisionNumber")).map(Double::intValue);
      Optional<LocalDate> publicationDate = Optional.ofNullable((String) map.get("publicationDate")).map(LocalDate::parse);

      return ArticleMetadata.builder()
        .setDoi((String) map.get("doi"))
        .setTitle(Optional.ofNullable((String) map.get("title")))
        .setRevisionNumber(revisionNumber)
        .setPublicationDate(publicationDate)
        .build();
    }
  }
    
  public abstract String getType();
  public abstract ArticleMetadata getSource();
  public abstract ArticleMetadata getTarget();

  public static Builder builder() {
    return new AutoValue_RelatedArticle.Builder();
  }

  public RelatedArticle invert() {
    return RelatedArticle.builder()
      .setSource(this.getTarget())
      .setTarget(this.getSource())
      .setType(RelatedArticle.invertedTypes.get(this.getType()))
      .build();
  }

  public static Map<String, String> invertedTypes = ImmutableMap.<String, String>builder()
    .put("commentary", "commentary-article")
    .put("commentary-article", "commentary")
    .put("companion", "companion")
    .put("corrected-article", "correction-forward")
    .put("correction-forward", "corrected-article")
    .put("retracted-article", "retraction-forward")
    .put("retraction-forward", "retracted-article")
    .put("object-of-concern", "concern-forward")
    .put("concern-forward", "object-of-concern")
    .build();

  public static RelatedArticle fromInboundMap(ArticleMetadata target, Map<String, Object> map) {
    ArticleMetadata source = ArticleMetadata.fromMap(map);

    return RelatedArticle.builder()
      .setTarget(target)
      .setSource(source)
      .setType((String) map.get("type"))
      .build();
  }

  public static RelatedArticle fromOutboundMap(ArticleMetadata source, Map<String, Object> map) {
    ArticleMetadata target = ArticleMetadata.fromMap(map);

    return RelatedArticle.builder()
      .setTarget(target)
      .setSource(source)
      .setType((String) map.get("type"))
      .build();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract RelatedArticle build();

    public abstract Builder setSource(ArticleMetadata source);
    public abstract Builder setTarget(ArticleMetadata target);
    public abstract Builder setType(String type);
  }
}
