package org.ambraproject.wombat.freemarker.asset;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import java.util.Collection;

/**
 * A link to an asset, optionally with pointers to other assets on which it depends.
 */
class AssetNode {

  private final String path;
  private final ImmutableSet<String> dependencies;

  AssetNode(String path, Collection<String> dependencies) {
    this.path = Preconditions.checkNotNull(path);
    this.dependencies = (dependencies == null) ? ImmutableSet.<String>of() : ImmutableSet.copyOf(dependencies);
  }

  public String getPath() {
    return path;
  }

  public ImmutableSet<String> getDependencies() {
    return dependencies;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AssetNode assetNode = (AssetNode) o;

    if (!path.equals(assetNode.path)) return false;
    if (!dependencies.equals(assetNode.dependencies)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = path.hashCode();
    result = 31 * result + dependencies.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "AssetNode{" +
        "path='" + path + '\'' +
        ", dependencies=" + dependencies +
        '}';
  }
}
