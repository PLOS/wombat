/*
 * Copyright (c) 2006-2013 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.wombat.model;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * CategoryView to hold category structure and information
 */
public class CategoryView implements Serializable {

  /**
   * Name of the root node of the taxonomy.  This is not exposed in the UI anywhere, but makes dealing
   * with the taxonomy easier.
   */
  public static final String ROOT_NODE_NAME = "ROOT";

  private Map<String, CategoryView> parents;
  private Map<String, CategoryView> children;
  private final String name;

  public CategoryView(String name) {
    this.name = name;

    parents = new ConcurrentSkipListMap<>();
    children = new ConcurrentSkipListMap<>();
  }

  public Map<String, CategoryView> getParents() {
    return parents;
  }

  public Map<String, CategoryView> getChildren() {
    return children;
  }

  public String getName() {
    return name;
  }

  /**
   * @return true iff this category is the root of the entire taxonomy tree
   */
  public boolean isRoot() {
    return ROOT_NODE_NAME.equals(name);
  }

  @Override
  public String toString() {
    return name;
  }

  public CategoryView getChild(String key) {
    return children.get(key);
  }

  public CategoryView getParent(String key) {
    return parents.get(key);
  }

  public void addParent(CategoryView categoryView) {
    parents.put(categoryView.getName(), categoryView);
    categoryView.children.put(this.name, this);
  }

  public void addChild(CategoryView categoryView) {
    children.put(categoryView.getName(), categoryView);
    categoryView.parents.put(this.name, this);
  }

  @Override
  public boolean equals(Object that) {
    if (that == null || !(that instanceof CategoryView)) {
      return false;
    }
    return name.equals(((CategoryView) (that)).name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }
}
