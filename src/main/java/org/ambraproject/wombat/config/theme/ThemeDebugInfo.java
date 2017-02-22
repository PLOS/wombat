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

package org.ambraproject.wombat.config.theme;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.MoreCollectors;
import com.google.common.collect.Multimaps;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

/**
 * A human-readable digest of theme information, for logging and debugging purposes only.
 */
public final class ThemeDebugInfo {
  private final String key;
  private final String description;
  private final ImmutableList<String> parents;
  private final ImmutableList<String> children;
  private final ImmutableList<String> inheritance;
  private final ImmutableList<String> sites;

  private ThemeDebugInfo(String key, String description,
                         List<String> parents, List<String> children, List<String> inheritance, List<String> sites) {
    this.key = Objects.requireNonNull(key);
    this.description = Objects.requireNonNull(description);
    this.parents = ImmutableList.copyOf(parents);
    this.children = ImmutableList.copyOf(children);
    this.inheritance = ImmutableList.copyOf(inheritance);
    this.sites = ImmutableList.copyOf(sites);
  }

  public String getKey() {
    return key;
  }

  public String getDescription() {
    return description;
  }

  public ImmutableList<String> getParents() {
    return parents;
  }

  public ImmutableList<String> getChildren() {
    return children;
  }

  public ImmutableList<String> getInheritance() {
    return inheritance;
  }

  public ImmutableList<String> getSites() {
    return sites;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ThemeDebugInfo themeInfo = (ThemeDebugInfo) o;
    return key.equals(themeInfo.key) && description.equals(themeInfo.description)
        && parents.equals(themeInfo.parents) && children.equals(themeInfo.children)
        && inheritance.equals(themeInfo.inheritance) && sites.equals(themeInfo.sites);
  }

  @Override
  public int hashCode() {
    return 31 * (31 * (31 * (31 * (31 * key.hashCode() + description.hashCode())
        + parents.hashCode()) + children.hashCode()) + inheritance.hashCode()) + sites.hashCode();
  }


  static Iterable<ThemeDebugInfo> describe(ThemeTree themeTree, SiteSet siteSet) {
    Objects.requireNonNull(themeTree);
    Objects.requireNonNull(siteSet);
    return () -> new ThemeInfoIterator(themeTree, siteSet);
  }

  /**
   * Iterate over all themes in the graph. Starting from the root, go in breadth-first order, but force each node to
   * appear only after all its parents.
   * <p>
   * The ordering is purely for human consumption from the {@link #describe} method. Because each theme only makes an
   * ordered declaration of its parents, not its children, the encounter order of each node's children is arbitrary.
   */
  private static class ThemeInfoIterator extends AbstractIterator<ThemeDebugInfo> {
    private final Queue<Theme> queue = new LinkedList<>();
    private final Set<Theme> yielded = new HashSet<>();
    private final ImmutableMultimap<Theme, Theme> childMap;
    private final ImmutableMultimap<Theme, Site> siteThemeMap;

    private ThemeInfoIterator(ThemeTree themeTree, SiteSet siteSet) {
      childMap = buildChildMap(themeTree);
      siteThemeMap = Multimaps.index(siteSet.getSites(), Site::getTheme);
      queue.add(findRoot(themeTree));
    }

    /**
     * @return the unique theme that has no parents (expected to be .Root)
     */
    private static Theme findRoot(ThemeTree themeTree) {
      return themeTree.getThemes().stream()
          .filter(t -> t.getParents().isEmpty())
          .collect(MoreCollectors.onlyElement());
    }

    /**
     * Build a map from each theme to its children.
     * <p>
     * (Contrast to {@link Theme.InheritanceChain#buildChildMap(Theme)}, which explores only part of the total theme
     * graph by starting at one node and exploring <em>up</em>. This method iterates over all themes.)
     */
    private static ImmutableSetMultimap<Theme, Theme> buildChildMap(ThemeTree themeTree) {
      ImmutableSetMultimap.Builder<Theme, Theme> childMap = ImmutableSetMultimap.builder();
      for (Theme child : themeTree.getThemes()) {
        for (Theme parent : child.getParents()) {
          childMap.put(parent, child);
        }
      }
      return childMap.build();
    }

    private Theme getNextTheme() {
      for (Iterator<Theme> iterator = queue.iterator(); iterator.hasNext(); ) {
        Theme theme = iterator.next();
        if (yielded.containsAll(theme.getParents())) {
          iterator.remove();
          if (yielded.add(theme)) {
            return theme;
          } // else, we already yielded it (because one node can be put into the queue more than once by different parents)
        } // else, it is not ready yet because we haven't yet encountered all its parents (leave in the queue for later)
      }
      if (!queue.isEmpty()) throw new AssertionError();
      return null;
    }

    @Override
    protected ThemeDebugInfo computeNext() {
      Theme theme = getNextTheme();
      if (theme == null) return endOfData();

      List<Theme> children = new ArrayList<>(childMap.get(theme));
      children.sort(Comparator
          .comparing((Theme t) -> !(t instanceof InternalTheme)) // prioritize internal themes first
          .thenComparing(Theme::getKey)); // then in alphabetical order
      queue.addAll(children);

      String key = theme.getKey();
      String description = theme.describeSource();
      List<String> parentKeys = theme.getParents().stream().map(Theme::getKey).collect(ImmutableList.toImmutableList());
      List<String> childKeys = children.stream().map(Theme::getKey).collect(ImmutableList.toImmutableList());
      List<String> inheritance = theme.getInheritanceChain().stream().map(Theme::getKey).collect(ImmutableList.toImmutableList());
      List<String> siteKeys = siteThemeMap.get(theme).stream().map(Site::getKey).collect(ImmutableList.toImmutableList());
      return new ThemeDebugInfo(key, description, parentKeys, childKeys, inheritance, siteKeys);
    }
  }

}
