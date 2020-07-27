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

import java.util.List;
import java.util.Map;
import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;

@AutoValue
public abstract class SearchFilterItem {
  public abstract String getDisplayName();

  public abstract int getNumberOfHits();

  public abstract String getFilterParamName();

  public abstract String getFilterValue();

  public static Builder builder() {
    return new AutoValue_SearchFilterItem.Builder();
  }

  @Memoized
  public String getFilterValueLowerCase() {
    return this.getFilterValue().toLowerCase();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract SearchFilterItem build();
    public abstract Builder setDisplayName(String displayName);
    public abstract Builder setNumberOfHits(int numberOfHits);
    public abstract Builder setFilterParamName(String filterParamName);
    public abstract Builder setFilterValue(String filterValue);
  }
}
