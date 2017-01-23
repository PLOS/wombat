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

package org.ambraproject.wombat.identity;

import com.google.common.collect.ImmutableMap;
import org.ambraproject.wombat.controller.DoiVersionArgumentResolver;

import java.util.Objects;

public final class AssetPointer {

  private final String assetDoi;
  private final ArticlePointer parentArticle;

  public AssetPointer(String assetDoi, ArticlePointer parentArticle) {
    this.assetDoi = Objects.requireNonNull(assetDoi);
    this.parentArticle = Objects.requireNonNull(parentArticle);
  }

  public String getAssetDoi() {
    return assetDoi;
  }

  public ArticlePointer getParentArticle() {
    return parentArticle;
  }

  public ImmutableMap<String, String> asParameterMap() {
    return ImmutableMap.<String, String>builder()
        .put(DoiVersionArgumentResolver.ID_PARAMETER, assetDoi)
        .putAll(parentArticle.getVersionParameter())
        .build();
  }

  @Override
  public boolean equals(Object o) {
    return this == o || o != null && getClass() == o.getClass()
        && assetDoi.equals(((AssetPointer) o).assetDoi)
        && parentArticle.equals(((AssetPointer) o).parentArticle);
  }

  @Override
  public int hashCode() {
    return 31 * parentArticle.hashCode() + assetDoi.hashCode();
  }

}
