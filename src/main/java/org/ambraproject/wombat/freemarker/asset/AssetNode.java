package org.ambraproject.wombat.freemarker.asset;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A link to an asset, optionally with pointers to other assets on which it depends.
 */
class AssetNode {

  private final String path;
  private final Set<String> dependencies;

  AssetNode(String path, Collection<String> dependencies) {
    this.path = Preconditions.checkNotNull(path);
    this.dependencies = (dependencies == null || dependencies.isEmpty())
        ? ImmutableSet.<String>of() // We expect nothing to be added, so the immutable flyweight should be safe
        : new HashSet<>(dependencies); // Must make this mutable to support removal
  }

  public String getPath() {
    return path;
  }

  public Set<String> getDependencies() {
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
