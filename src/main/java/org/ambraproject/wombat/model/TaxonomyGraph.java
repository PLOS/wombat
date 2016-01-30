package org.ambraproject.wombat.model;

import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultimap;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * A representation of the graph of all taxonomy terms. Each term is a graph node that may have multiple parents and
 * children.
 */
public class TaxonomyGraph implements Serializable {

  private final ImmutableSet<String> roots;
  private final ImmutableSetMultimap<String, String> parentsToChildren;
  private final ImmutableSetMultimap<String, String> childrenToParents;

  private TaxonomyGraph(Collection<String> roots,
                        SetMultimap<String, String> parentsToChildren,
                        SetMultimap<String, String> childrenToParents) {
    this.roots = ImmutableSet.copyOf(roots);
    this.parentsToChildren = ImmutableSetMultimap.copyOf(parentsToChildren);
    this.childrenToParents = ImmutableSetMultimap.copyOf(childrenToParents);
  }

  private static final Splitter CATEGORY_SPLITTER = Splitter.on('/').omitEmptyStrings();

  public static List<String> parseTerms(String categoryPath) {
    return CATEGORY_SPLITTER.splitToList(categoryPath);
  }

  /**
   * @param categoryPaths a list of all slash-delimited category paths in the taxonomy
   * @return a parsed graph representation of the taxonomy
   */
  public static TaxonomyGraph create(Collection<String> categoryPaths) {
    Set<String> roots = new TreeSet<>();
    SetMultimap<String, String> parentsToChildren = TreeMultimap.create();
    SetMultimap<String, String> childrenToParents = TreeMultimap.create();
    for (String categoryPath : categoryPaths) {
      List<String> categories = parseTerms(categoryPath);
      roots.add(categories.get(0));
      for (int i = 1; i < categories.size(); i++) {
        String parent = categories.get(i - 1);
        String child = categories.get(i);
        parentsToChildren.put(parent, child);
        childrenToParents.put(child, parent);
      }
    }
    return new TaxonomyGraph(roots, parentsToChildren, childrenToParents);
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
   * @param categoryName the name of a category (not a full path)
   * @return a view of the named category, or {@code null} if no such category is in this graph
   */
  public CategoryView getView(String categoryName) {
    return (roots.contains(categoryName) || childrenToParents.containsKey(categoryName))
        ? new CategoryView(categoryName) : null;
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
     */
    private Map<String, CategoryView> buildLazyMap(ImmutableSet<String> categoryNames) {
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
