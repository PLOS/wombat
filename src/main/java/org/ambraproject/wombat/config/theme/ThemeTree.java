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
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.ambraproject.wombat.config.site.SiteSet;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
   * Dump a human-friendly description of all themes, starting at the root, for logging and debugging purposes only.
   */
  public Iterable<ThemeDebugInfo> describe(SiteSet siteSet) {
    return ThemeDebugInfo.describe(this, siteSet);
  }

}
