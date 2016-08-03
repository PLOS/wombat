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
        .put(parentArticle.getVersionParameter())
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
