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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.MoreCollectors;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Internal representation of the set of all themes.
 * <p/>
 * Note that, because a theme can have more than one parent, this is not actually a tree and should be renamed to
 * something like {@code ThemeGraph} or {@code ThemeDag}. However, to avoid causing conflicts between source branches,
 * we are putting off renaming it for later.
 */
public class ThemeTree {

  private final ImmutableBiMap<String, Theme> themes;

  @VisibleForTesting
  public ThemeTree(Map<String, Theme> themes) {
    this.themes = ImmutableBiMap.copyOf(themes);
  }

  public Theme getTheme(String key) {
    return themes.get(key);
  }

  @VisibleForTesting
  public ImmutableSet<Theme> getThemes() {
    return themes.values();
  }


  public static class ThemeConfigurationException extends Exception {
    private ThemeConfigurationException(String message) {
      super(message);
    }

    public ThemeConfigurationException(String message, Throwable cause) {
      super(message, cause);
    }
  }


  /**
   * Factory method for the tree.
   */
  public static ThemeTree create(Theme rootTheme,
                                 Collection<? extends Theme> internalThemes,
                                 Collection<? extends ThemeBuilder<?>> externalThemes)
      throws ThemeConfigurationException {
    Preconditions.checkArgument(internalThemes.contains(rootTheme));

    Map<String, ThemeBuilder<?>> themeBuilderMap = externalThemes.stream()
        .collect(Collectors.toMap(ThemeBuilder::getKey, Function.identity()));

    Map<String, Theme> created = new TreeMap<>(); // will eventually contain all themes
    created.putAll(Maps.uniqueIndex(internalThemes, Theme::getKey)); // initialize with internal themes

    // Make repeated passes looking for nodes whose parents have all been created
    int sizeLastPass = 0;
    while (!themeBuilderMap.isEmpty()) {
      for (Iterator<ThemeBuilder<?>> iterator = themeBuilderMap.values().iterator(); iterator.hasNext(); ) {
        ThemeBuilder<?> node = iterator.next();

        // Search for this node's parents in the map of already-created, immutable themes
        List<String> parentKeys = node.getParentKeys();
        List<Theme> parentThemes = Lists.newArrayListWithCapacity(parentKeys.size());
        for (String parentKey : parentKeys) {
          Theme parentTheme = created.get(parentKey);
          if (parentTheme != null) {
            parentThemes.add(parentTheme);
          } else if (!themeBuilderMap.containsKey(parentKey)) {
            throw new ThemeConfigurationException("Unrecognized theme key: " + parentKey);
          } else {
            // At least one parent has not been created yet
            parentThemes = null;
            break;
          }
        }

        if (parentThemes != null) {
          // All parents were found
          Theme immutableNode = node.build(rootTheme, parentThemes);
          created.put(immutableNode.getKey(), immutableNode);
          iterator.remove(); // Remove this node from the to-be-created pool
        } // Else, leave this node in the pool and look for more matches
      }

      // Check that the to-be-created pool shrank by at least 1, to prevent infinite looping
      if (created.size() <= sizeLastPass) {
        throw new ThemeConfigurationException("A parentage cycle exists within: " + themeBuilderMap.keySet());
      }
      sizeLastPass = created.size();
    }

    return new ThemeTree(created);
  }


  /**
   * A human-readable digest of theme information, for logging and debugging purposes only.
   */
  public static final class ThemeInfo {
    private final String key;
    private final String description;
    private final ImmutableList<String> parents;
    private final ImmutableList<String> children;
    private final ImmutableList<String> inheritance;
    private final ImmutableList<String> sites;

    private ThemeInfo(String key, String description,
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
      ThemeInfo themeInfo = (ThemeInfo) o;
      return key.equals(themeInfo.key) && description.equals(themeInfo.description)
          && parents.equals(themeInfo.parents) && children.equals(themeInfo.children)
          && inheritance.equals(themeInfo.inheritance) && sites.equals(themeInfo.sites);
    }

    @Override
    public int hashCode() {
      return 31 * (31 * (31 * (31 * (31 * key.hashCode() + description.hashCode())
          + parents.hashCode()) + children.hashCode()) + inheritance.hashCode()) + sites.hashCode();
    }
  }

  /**
   * Iterate over all themes in the tree, in breadth-first order starting from the root.
   * <p>
   * The ordering is purely for human consumption from the {@link #describe} method. Because each theme only makes an
   * ordered declaration of its parents, not its children, the encounter order of each node's children is arbitrary.
   */
  private class ThemeInfoIterator extends AbstractIterator<ThemeInfo> {
    private final Deque<Theme> queue = new ArrayDeque<>();
    private final Set<Theme> yielded = Sets.newHashSetWithExpectedSize(themes.size());
    private final ImmutableMultimap<Theme, Theme> childMap = buildChildMap();
    private final ImmutableMultimap<Theme, Site> siteThemeMap;

    private ThemeInfoIterator(SiteSet siteSet) {
      siteThemeMap = Multimaps.index(siteSet.getSites(), Site::getTheme);
      queue.add(findRoot());
    }

    /**
     * @return the unique theme that has no parents (expected to be .Root)
     */
    private Theme findRoot() {
      return themes.values().stream()
          .filter(t -> t.getParents().isEmpty())
          .collect(MoreCollectors.onlyElement());
    }

    /**
     * Build a map from each theme to its children.
     * <p>
     * (Contrast to {@link Theme.InheritanceChain#buildChildMap(Theme)}, which explores only part of the total theme
     * graph by starting at one node and exploring <em>up</em>. This method starts iterates over all themes.)
     */
    private ImmutableSetMultimap<Theme, Theme> buildChildMap() {
      ImmutableSetMultimap.Builder<Theme, Theme> childMap = ImmutableSetMultimap.builder();
      for (Theme child : themes.values()) {
        for (Theme parent : child.getParents()) {
          childMap.put(parent, child);
        }
      }
      return childMap.build();
    }

    @Override
    protected ThemeInfo computeNext() {
      Theme theme;
      do {
        if (queue.isEmpty()) return endOfData();
        theme = queue.remove();
      } while (yielded.contains(theme));
      yielded.add(theme);

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
      return new ThemeInfo(key, description, parentKeys, childKeys, inheritance, siteKeys);
    }
  }

  /**
   * Dump a human-friendly description of all themes, starting at the root, for logging and debugging purposes only.
   */
  public Iterable<ThemeInfo> describe(SiteSet siteSet) {
    Objects.requireNonNull(siteSet);
    return () -> new ThemeInfoIterator(siteSet);
  }

}
