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

package org.ambraproject.wombat.config.theme;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;

public final class ThemeBuilder<T extends Theme> {

  @FunctionalInterface
  public static interface ConstructorFunction<T extends Theme> {
    public abstract T construct(String key, List<Theme> parentObjects);
  }

  private final String key;
  private final ImmutableList<String> parentKeys;
  private final ConstructorFunction<T> ctor;

  ThemeBuilder(String key, List<String> parentKeys, ConstructorFunction<T> ctor) {
    this.key = Objects.requireNonNull(key);
    this.parentKeys = ImmutableList.copyOf(parentKeys);
    this.ctor = Objects.requireNonNull(ctor);
  }

  public String getKey() {
    return key;
  }

  public ImmutableList<String> getParentKeys() {
    return parentKeys;
  }

  public T build(List<Theme> parentObjects) {
    parentObjects = ImmutableList.copyOf(parentObjects);
    int length = parentObjects.size();
    Preconditions.checkArgument(length == parentKeys.size());
    for (int i = 0; i < length; i++) {
      if (!parentObjects.get(i).getKey().equals(parentKeys.get(i))) {
        throw new IllegalArgumentException(String.format("Mismatched keys at index %d: %s; %s",
            i, parentObjects.get(i).getKey(), parentKeys.get(i)));
      }
    }

    return ctor.construct(key, parentObjects);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ThemeBuilder<?> that = (ThemeBuilder<?>) o;
    if (!key.equals(that.key)) return false;
    if (!parentKeys.equals(that.parentKeys)) return false;
    return ctor.equals(that.ctor);
  }

  @Override
  public int hashCode() {
    int result = key.hashCode();
    result = 31 * result + parentKeys.hashCode();
    result = 31 * result + ctor.hashCode();
    return result;
  }
}
