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

import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Maps;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A representation of the graph of all taxonomy terms. Each term is a graph node that may have multiple parents and
 * children.
 */
public class TaxonomyGraph implements Serializable {

  private final ImmutableSortedMap<String, CategoryInfo> categories;
  private final ImmutableList<CategoryInfo> roots;

  private TaxonomyGraph(ImmutableSortedMap<String, CategoryInfo> categories) {
    this.categories = Objects.requireNonNull(categories);
    this.roots = ImmutableList.copyOf(Collections2.filter(this.categories.values(),
        category -> category.parentNames.isEmpty()));
  }

  private static final Splitter CATEGORY_SPLITTER = Splitter.on('/').omitEmptyStrings();

  public static List<String> parseTerms(String categoryPath) {
    return CATEGORY_SPLITTER.splitToList(categoryPath);
  }

  private static SortedSetMultimap<String, String> caseInsensitiveSetMultimap() {
    return TreeMultimap.create(String.CASE_INSENSITIVE_ORDER, String.CASE_INSENSITIVE_ORDER);
  }

  /**
   * @param categoryPaths a list of all slash-delimited category paths in the taxonomy
   * @return a parsed graph representation of the taxonomy
   */
  public static TaxonomyGraph create(Collection<String> categoryPaths) {
    Set<String> names = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    SortedSetMultimap<String, String> parentsToChildren = caseInsensitiveSetMultimap();
    SortedSetMultimap<String, String> childrenToParents = caseInsensitiveSetMultimap();
    for (String categoryPath : categoryPaths) {
      List<String> categories = parseTerms(categoryPath);
      for (int i = 0; i < categories.size(); i++) {
        String node = categories.get(i);
        names.add(node);
        if (i > 0) {
          String parent = categories.get(i - 1);
          parentsToChildren.put(parent, node);
          childrenToParents.put(node, parent);
        }
      }
    }

    ImmutableSortedMap.Builder<String, CategoryInfo> categoryMap = ImmutableSortedMap.orderedBy(String.CASE_INSENSITIVE_ORDER);
    for (String name : names) {
      categoryMap.put(name, new CategoryInfo(name, childrenToParents.get(name), parentsToChildren.get(name)));
    }

    return new TaxonomyGraph(categoryMap.build());
  }

  public Collection<String> getRootCategoryNames() {
    return Collections2.transform(roots, CategoryInfo::getName);
  }

  public Collection<CategoryView> getRootCategoryViews() {
    return Collections2.transform(roots, CategoryView::new);
  }

  public Set<String> getAllCategoryNames() {
    return categories.keySet();
  }

  /**
   * @param categoryName the name of a category (not a full path; case-insensitive)
   * @return a view of the named category, or {@code null} if no such category is in this graph
   */
  public CategoryView getView(String categoryName) {
    CategoryInfo info = categories.get(categoryName);
    return (info == null) ? null : new CategoryView(info);
  }

  /**
   * @param categoryName the name of a category
   * @return the same name with its canonical capitalization, or {@code null} if the named category does not exist
   */
  public String getName(String categoryName) {
    CategoryInfo info = categories.get(categoryName);
    return (info == null) ? null : info.getName();
  }

  /**
   * Serializable storage of data for a category.
   */
  private static class CategoryInfo implements Serializable {
    private final String name;
    private final ImmutableSortedSet<String> parentNames;
    private final ImmutableSortedSet<String> childNames;

    private CategoryInfo(String name, Set<String> parentNames, Set<String> childNames) {
      this.name = name;
      this.parentNames = ImmutableSortedSet.copyOf(String.CASE_INSENSITIVE_ORDER, parentNames);
      this.childNames = ImmutableSortedSet.copyOf(String.CASE_INSENSITIVE_ORDER, childNames);
    }

    private String getName() {
      return name;
    }
  }

  /**
   * A non-serializable, lazy-loading view of a category, with access to its parents and children, for the purpose of
   * passing up to the front end.
   */
  public class CategoryView {
    private final CategoryInfo categoryInfo;

    private CategoryView(CategoryInfo categoryInfo) {
      this.categoryInfo = Objects.requireNonNull(categoryInfo);
    }

    /**
     * @return the category's name
     */
    public String getName() {
      return categoryInfo.name;
    }

    /**
     * Create a lazy-loading map view of a group of other categories. Other CategoryView will be constructed only
     * on-demand, meaning that we do not have to walk the graph and construct CategoryView objects for nodes that are
     * not read.
     * <p>
     * Uses the same definition of equality as the delegate set (i.e., case-insensitivity).
     */
    private Map<String, CategoryView> buildLazyMap(SortedSet<String> categoryNames) {
      return Maps.asMap(categoryNames, TaxonomyGraph.this::getView);
    }

    /**
     * @return views of the category's parents
     */
    public Map<String, CategoryView> getParents() {
      return buildLazyMap(categoryInfo.parentNames);
    }

    /**
     * @return views of the category's children
     */
    public Map<String, CategoryView> getChildren() {
      return buildLazyMap(categoryInfo.childNames);
    }
  }

}
