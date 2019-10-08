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

package org.ambraproject.wombat.model;
import java.time.LocalDate;
import java.util.Map;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class RelatedArticle {
  public abstract String getDoi();
  public abstract LocalDate getPublicationDate();
  public abstract String getTitle();

  public static Builder builder() {
    return new AutoValue_RelatedArticle.Builder();
  }

  public static RelatedArticle fromMap(Map<String, String> map) {
    return RelatedArticle.builder()
      .setDoi(map.get("doi"))
      .setTitle(map.get("title"))
      .setPublicationDate(LocalDate.parse(map.get("publicationDate")))
      .build();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract RelatedArticle build();

    public abstract Builder setDoi(String doi);
    public abstract Builder setTitle(String title);
    public abstract Builder setPublicationDate(LocalDate title);
  }
}
