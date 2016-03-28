package org.ambraproject.wombat.model;

import com.google.common.base.Functions;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
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

  // All lookups are case-insensitive, so all data structures sorted with case-insensitive comparisons.
  // There is no ImmutableSortedSetMultimap, so use unmodifiable wrappers instead.
  private final SortedSetMultimap<String, String> parentsToChildren;
  private final SortedSetMultimap<String, String> childrenToParents;
  private final ImmutableSortedSet<String> roots;
  private final ImmutableSortedMap<String, String> canonicalNames;

  private TaxonomyGraph(Set<String> roots,
                        SortedSetMultimap<String, String> parentsToChildren,
                        SortedSetMultimap<String, String> childrenToParents,
                        Set<String> canonicalNames) {
    this.roots = ImmutableSortedSet.copyOf(String.CASE_INSENSITIVE_ORDER, roots);
    this.parentsToChildren = Multimaps.unmodifiableSortedSetMultimap(parentsToChildren);
    this.childrenToParents = Multimaps.unmodifiableSortedSetMultimap(childrenToParents);
    this.canonicalNames = ImmutableSortedMap.copyOf(Maps.asMap(canonicalNames, Functions.identity()),
        String.CASE_INSENSITIVE_ORDER);
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
    Set<String> roots = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    Set<String> names = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    SortedSetMultimap<String, String> parentsToChildren = caseInsensitiveSetMultimap();
    SortedSetMultimap<String, String> childrenToParents = caseInsensitiveSetMultimap();
    for (String categoryPath : categoryPaths) {
      List<String> categories = parseTerms(categoryPath);
      String root = categories.get(0);
      roots.add(root);
      names.add(root);
      for (int i = 1; i < categories.size(); i++) {
        String parent = categories.get(i - 1);
        String child = categories.get(i);
        parentsToChildren.put(parent, child);
        childrenToParents.put(child, parent);
        names.add(child);
      }
    }
    return new TaxonomyGraph(roots, parentsToChildren, childrenToParents, names);
  }

  public ImmutableSet<String> getRootCategoryNames() {
    return roots;
  }

  public Collection<CategoryView> getRootCategoryViews() {
    return Collections2.transform(getRootCategoryNames(), CategoryView::new);
  }

  public Set<String> getAllCategoryNames() {
    return Sets.union(roots, childrenToParents.keySet());
  }

  /**
   * @param categoryName the name of a category (not a full path; case-insensitive)
   * @return a view of the named category, or {@code null} if no such category is in this graph
   */
  public CategoryView getView(String categoryName) {
    return (roots.contains(categoryName) || childrenToParents.containsKey(categoryName))
        ? new CategoryView(categoryName) : null;
  }

  /**
   * @param subject the name of a category
   * @return the same name with its canonical capitalization, or {@code null} if the named category does not exist
   */
  public String getName(String subject) {
    return canonicalNames.get(subject);
  }

  /**
   * A view of a category, with access to its parents and children.
   */
  public class CategoryView {
    private final String name;

    private CategoryView(String name) {
      this.name = Objects.requireNonNull(name);
    }

    /**
     * @return the category's name
     */
    public String getName() {
      return name;
    }

    /**
     * Create a lazy-loading map view of a group of other categories. Other CategoryView will be constructed only
     * on-demand, meaning that we do not have to walk the graph and construct CategoryView objects for nodes that are
     * not read.
     * <p>
     * Uses the same definition of equality as the delegate set (i.e., case-insensitivity).
     */
    private Map<String, CategoryView> buildLazyMap(SortedSet<String> categoryNames) {
      return Maps.asMap(categoryNames, CategoryView::new);
    }

    /**
     * @return views of the category's parents
     */
    public Map<String, CategoryView> getParents() {
      return buildLazyMap(childrenToParents.get(name));
    }

    /**
     * @return views of the category's children
     */
    public Map<String, CategoryView> getChildren() {
      return buildLazyMap(parentsToChildren.get(name));
    }
  }

}
