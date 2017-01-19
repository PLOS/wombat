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

package org.ambraproject.wombat.freemarker.asset;

import com.google.common.base.Preconditions;

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
    this.dependencies = (dependencies == null) ? new HashSet<String>(0) : new HashSet<>(dependencies);
  }

  /**
   * @return the path of the asset link
   */
  public String getPath() {
    return path;
  }

  /**
   * Return the set of dependencies on other nodes. Dependencies are represented by strings that match the other nodes'
   * {@link #getPath()} values.
   * <p/>
   * The returned {@code Set} supports addition and removal.
   *
   * @return the set of paths of nodes on which this node is dependent
   */
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
