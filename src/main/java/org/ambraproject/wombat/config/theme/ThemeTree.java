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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

/**
 * Internal representation of the set of all themes.
 * <p/>
 * Note that, because a theme can have more than one parent, this is not actually a tree and should be renamed to
 * something like {@code ThemeGraph} or {@code ThemeDag}. However, to avoid causing conflicts between source branches,
 * we are putting off renaming it for later.
 */
public class ThemeTree {

  private ImmutableBiMap<String, Theme> themes;

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


  /**
   * Mutable representation of a theme.
   */
  private static class Mutable {
    private String key;
    private String location;
    private Object parentKeys;

    public List<String> getParentKeys() {
      if (parentKeys == null) return ImmutableList.of();
      if (parentKeys instanceof String) return ImmutableList.of((String) parentKeys);

      Collection<String> parentKeyStrings = (Collection<String>) parentKeys;
      List<String> checked = Collections.checkedList(Lists.<String>newArrayListWithCapacity(parentKeyStrings.size()),
          String.class);
      checked.addAll(parentKeyStrings);
      return checked;
    }
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
   *
   * @param themeConfigJson
   * @param internalThemes
   * @return
   * @throws ThemeConfigurationException
   */
  public static ThemeTree parse(List<? extends Map<String, ?>> themeConfigJson, Collection<? extends Theme> internalThemes, Theme rootTheme)
      throws ThemeConfigurationException {
    Preconditions.checkArgument(internalThemes.contains(rootTheme));
    Map<String, ? extends Theme> internalThemeMap = Maps.uniqueIndex(internalThemes, Theme.GET_KEY);
    Map<String, Mutable> mutables = Maps.newLinkedHashMap();

    // Make a pass over the JSON, creating mutable objects and mapping them by their keys
    for (Map<String, ?> themeJsonObj : themeConfigJson) {
      Mutable node = buildFromJson(themeJsonObj);
      mutables.put(node.key, node);
    }
    Ordering<String> keyOrder = Ordering.explicit(ImmutableList.copyOf(
        Iterables.concat(internalThemeMap.keySet(), mutables.keySet())));

    SortedMap<String, Theme> created = Maps.newTreeMap(keyOrder);
    created.putAll(internalThemeMap);

    // Make repeated passes looking for nodes whose parents have all been created
    int sizeLastPass = 0;
    while (!mutables.isEmpty()) {
      for (Iterator<Mutable> iterator = mutables.values().iterator(); iterator.hasNext(); ) {
        Mutable node = iterator.next();

        // Search for this node's parents in the map of already-created, immutable themes
        List<String> parentKeys = node.getParentKeys();
        List<Theme> parentThemes = Lists.newArrayListWithCapacity(parentKeys.size());
        for (String parentKey : parentKeys) {
          Theme parentTheme = created.get(parentKey);
          if (parentTheme != null) {
            parentThemes.add(parentTheme);
          } else if (!mutables.containsKey(parentKey)) {
            throw new ThemeConfigurationException("Unrecognized theme key: " + parentKey);
          } else {
            // At least one parent has not been created yet
            parentThemes = null;
            break;
          }
        }

        if (parentThemes != null) {
          // All parents were found
          Theme immutableNode = createImmutableNode(node, parentThemes, rootTheme);
          created.put(immutableNode.getKey(), immutableNode);
          iterator.remove(); // Remove this node from the to-be-created pool
        } // Else, leave this node in the pool and look for more matches
      }

      // Check that the to-be-created pool shrank by at least 1, to prevent infinite looping
      if (created.size() <= sizeLastPass) {
        throw new ThemeConfigurationException("A parentage cycle exists within: " + mutables.keySet());
      }
      sizeLastPass = created.size();
    }

    return new ThemeTree(created);
  }

  private static Mutable buildFromJson(Map<String, ?> themeJsonObj) {
    Mutable m = new Mutable();
    m.key = (String) themeJsonObj.get("key");
    m.location = (String) themeJsonObj.get("path");
    m.parentKeys = themeJsonObj.get("parent");
    return m;
  }

  private static Theme createImmutableNode(Mutable toCreate, List<Theme> parents, Theme defaultTheme) throws ThemeConfigurationException {
    if (parents.isEmpty()) {
      parents = ImmutableList.of(defaultTheme);
    }
    File themeLocation = new File(toCreate.location);
    Theme node;
    try {
      node = new FileTheme(toCreate.key, parents, themeLocation); // TODO Support other theme types
    } catch (IOException e) {
      throw new ThemeConfigurationException("Could not access: " + themeLocation, e);
    }
    return node;
  }

}
