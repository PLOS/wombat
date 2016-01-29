package org.ambraproject.wombat.model;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultimap;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class TaxonomyGraph {

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

  public static TaxonomyGraph create(Collection<String> categoryPaths) {
    Set<String> roots = new TreeSet<>();
    SetMultimap<String, String> parentsToChildren = TreeMultimap.create();
    SetMultimap<String, String> childrenToParents = TreeMultimap.create();
    for (String categoryPath : categoryPaths) {
      List<String> categories = CATEGORY_SPLITTER.splitToList(categoryPath);
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

  public Set<String> getAllCategoryNames() {
    return Sets.union(roots, childrenToParents.keySet());
  }

  public CategoryView getView(String categoryName) {
    return (roots.contains(categoryName) || childrenToParents.containsKey(categoryName))
        ? new CategoryView(categoryName) : null;
  }

  public class CategoryView {
    private final String name;

    private CategoryView(String name) {
      this.name = Objects.requireNonNull(name);
    }

    public String getName() {
      return name;
    }

    public Map<String, CategoryView> getParents() {
      return Maps.asMap(childrenToParents.get(name), CategoryView::new);
    }

    public Map<String, CategoryView> getChildren() {
      return Maps.asMap(parentsToChildren.get(name), CategoryView::new);
    }
  }

}
