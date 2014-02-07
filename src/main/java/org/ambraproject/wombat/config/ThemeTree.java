package org.ambraproject.wombat.config;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

/**
 * Internal representation of the set of all themes.
 */
public class ThemeTree {

  private ImmutableMap<String, Theme> themes;

  @VisibleForTesting
  public ThemeTree(Map<String, Theme> themes) {
    this.themes = ImmutableMap.copyOf(themes);
  }

  public Theme getTheme(String key) {
    return themes.get(key);
  }


  /**
   * Mutable representation of a theme.
   */
  private static class Mutable {
    private String key;
    private String location;
    private String parentKey;
    private Mutable parent;
    private Collection<Mutable> children = Lists.newArrayList();
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
   * @return
   * @throws ThemeConfigurationException
   */
  static ThemeTree parse(List<Map<String, ?>> themeConfigJson, Collection<? extends Theme> internalThemes, Theme rootTheme)
      throws ThemeConfigurationException {
    Preconditions.checkArgument(internalThemes.contains(rootTheme));
    Map<String, ? extends Theme> internalThemeMap = Maps.uniqueIndex(internalThemes, Theme.GET_KEY);

    Map<String, Mutable> mutables = Maps.newHashMapWithExpectedSize(internalThemeMap.size() + themeConfigJson.size());
    List<String> keyOrder = Lists.newArrayListWithCapacity(themeConfigJson.size());
    keyOrder.addAll(internalThemeMap.keySet());

    // Make a pass over the JSON, creating mutable objects and mapping them by their keys
    for (Map<String, ?> themeJsonObj : themeConfigJson) {
      Mutable node = buildFromJson(themeJsonObj);
      mutables.put(node.key, node);
      keyOrder.add(node.key);
    }

    // Make a pass over the created mutables, linking them to their parent mutables
    for (Mutable node : mutables.values()) {
      if (node.parentKey == null || internalThemeMap.containsKey(node.parentKey)) {
        continue; // Its parent is internal
      }
      Mutable parent = mutables.get(node.parentKey);
      if (parent == null) {
        String message = String.format("Parent \"%s\" not found for theme \"%s\"",
            node.parentKey, node.key);
        throw new ThemeConfigurationException(message);
      }
      parent.children.add(node);
      node.parent = parent;
    }

    SortedMap<String, Theme> created = Maps.newTreeMap(Ordering.explicit(keyOrder));
    created.putAll(internalThemeMap);

    // Create the root nodes, then recursively create their children
    for (Mutable node : mutables.values()) {
      if (node.parent == null) {
        Theme parent = (node.parentKey == null) ? rootTheme : internalThemeMap.get(node.parentKey);
        createImmutableNodes(node, parent, created);
      }
    }

    // Cycles in the inheritance graph don't belong to any tree, so they wouldn't have gotten picked up
    if (created.size() < mutables.size()) {
      Set<String> cyclicKeys = Sets.difference(mutables.keySet(), created.keySet());
      throw new ThemeConfigurationException("Theme parents have one or more cycles: " + cyclicKeys);
    }

    return new ThemeTree(created);
  }

  private static Mutable buildFromJson(Map<String, ?> themeJsonObj) {
    Mutable m = new Mutable();
    m.key = (String) themeJsonObj.get("key");
    m.location = (String) themeJsonObj.get("path");
    m.parentKey = (String) themeJsonObj.get("parent");
    return m;
  }

  private static void createImmutableNodes(Mutable toCreate, Theme parent, Map<String, Theme> container)
      throws ThemeConfigurationException {
    File themeLocation = new File(toCreate.location);
    Theme node;
    try {
      node = new FileTheme(toCreate.key, parent, themeLocation); // TODO Support other theme types
    } catch (IOException e) {
      throw new ThemeConfigurationException("Could not access: " + themeLocation, e);
    }
    Theme previous = container.put(node.getKey(), node);
    if (previous != null) {
      // Expected not to be possible on any user input.
      // It should (hopefully) interrupt an infinite recursion bug.
      throw new RuntimeException("Key collision: " + node.getKey());
    }
    for (Mutable child : toCreate.children) {
      createImmutableNodes(child, node, container);
    }
  }

  ImmutableMap<String, Theme> matchToSites(List<Map<String, ?>> siteConfigJson) {
    Map<String, Theme> siteMap = Maps.newLinkedHashMap();
    for (Map<String, ?> siteObj : siteConfigJson) {
      String key = (String) siteObj.get("key");
      String themeName = (String) siteObj.get("theme");
      siteMap.put(key, themes.get(themeName));
    }
    return ImmutableMap.copyOf(siteMap);
  }

}
