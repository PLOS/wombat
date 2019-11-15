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
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;

@AutoValue
public abstract class RelatedArticleType implements Comparable<RelatedArticleType> {
  public abstract int getSortOrder();
  public abstract String getDisplayName();
  public abstract String getName();
  public boolean isPertainsToRelation() {
    return !isHasRelation();
  }

  /**
   * Special css names that are not worth removing now. :(
   */  
  public abstract Optional<String> getSpecialCssName();

  public String getCssName() {
    return getSpecialCssName().orElse(getName());
  }

  /*
    * Return true if the related article here is a "has" type-relation
    * to the article it is attached to. In other words, if the base
    * article can be said to "has correction" of this article. Used
    * for display.
   */
  public abstract boolean isHasRelation();

  public abstract boolean isAmendment();

  public abstract boolean isFullBodyAmendment();

  public static Builder builder() {
    return new AutoValue_RelatedArticleType.Builder()
      .setHasRelation(false)
      .setAmendment(false)
      .setFullBodyAmendment(false);
  }

  public static RelatedArticleType get(String typeName) {
    RelatedArticleType type = TYPEMAP.get(typeName);
    if (type != null) {
      return type;
    } else {
      /* Must be a new type, do our best.*/
      return RelatedArticleType.builder()
        .setName(typeName)
        .setDisplayName(typeName)
        .setHasRelation(true)
        .setSortOrder(100)
        .build();
    }
  }

  private static ImmutableSet<RelatedArticleType> TYPESET =
    ImmutableSet
    .of(RelatedArticleType.builder()
        .setSortOrder(1)
        .setName("retraction-forward")
        .setSpecialCssName("retraction")
        .setDisplayName("Retraction")
        .setHasRelation(true)
        .setAmendment(true)
        .setFullBodyAmendment(true)
        .build(),
        RelatedArticleType.builder()
        .setSortOrder(2)
        .setName("retracted-article")
        .setDisplayName("Retraction")
        .build(),
        RelatedArticleType.builder()
        .setSortOrder(3)
        .setName("correction-forward")
        .setDisplayName("Correction")
        .setSpecialCssName("correction")
        .setHasRelation(true)
        .setAmendment(true)
        .build(),
        RelatedArticleType.builder()
        .setSortOrder(4)
        .setName("corrected-article")
        .setDisplayName("Correction")
        .build(),
        RelatedArticleType.builder()
        .setSortOrder(5)
        .setName("concern-forward")
        .setDisplayName("Expression of Concern")
        .setSpecialCssName("eoc")
        .setHasRelation(true)
        .setFullBodyAmendment(true)
        .setAmendment(true)
        .build(),
        RelatedArticleType.builder()
        .setSortOrder(6)
        .setName("object-of-concern")
        .setDisplayName("Expression of Concern")
        .build(),
        RelatedArticleType.builder()
        .setSortOrder(7)
        .setName("update-forward")
        .setDisplayName("Update Article")
        .setSpecialCssName("update")
        .setHasRelation(true)
        .setAmendment(true)
        .build(),
        RelatedArticleType.builder()
        .setSortOrder(8)
        .setName("updated-article")
        .setDisplayName("Update Article")
        .build(),
        RelatedArticleType.builder()
        .setSortOrder(9)
        .setName("companion")
        .setDisplayName("Companion")
        .setHasRelation(true)
        .build(),
        RelatedArticleType.builder()
        .setSortOrder(10)
        .setName("commentary")
        .setDisplayName("Commentary")
        .setHasRelation(true)
        .build(),
        RelatedArticleType.builder()
        .setSortOrder(11)
        .setName("commentary-article")
        .setDisplayName("Commentary")
        .build()
        );

  /* Map from typename to RelatedArticleType */
  private static Map<String, RelatedArticleType> TYPEMAP = TYPESET.stream()
      .collect(Collectors.toMap(RelatedArticleType::getName, Function.identity()));

  @Override
  public int compareTo(RelatedArticleType that) {
    return this.getSortOrder() - that.getSortOrder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract RelatedArticleType build();

    public abstract Builder setSortOrder(int sortOrder);
    public abstract Builder setDisplayName(String displayName);
    public abstract Builder setName(String name);
    public abstract Builder setHasRelation(boolean hasRelation);
    public abstract Builder setAmendment(boolean amendment);
    public abstract Builder setFullBodyAmendment(boolean fullBodyAmendment);
    public abstract Builder setSpecialCssName(String specialCssName);
  }
}
