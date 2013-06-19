package org.ambraproject.wombat.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

/**
 * Internal representation of the set of all themes. Should be exposed to the controller layer only.
 */
class ThemeTree {

  private ImmutableMap<String, Theme> themes;

  private ThemeTree(Map<String, Theme> themes) {
    this.themes = ImmutableMap.copyOf(themes);
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
  static ThemeTree parse(List<Map<String, ?>> themeConfigJson) throws ThemeConfigurationException {
    Map<String, Mutable> mutables = Maps.newHashMapWithExpectedSize(themeConfigJson.size());

    // Make a pass over the JSON, creating mutable objects and mapping them by their keys
    for (Map<String, ?> themeJsonObj : themeConfigJson) {
      Mutable node = buildFromJson(mutables, themeJsonObj);
      mutables.put(node.key, node);
    }

    // Make a pass over the created mutables, linking them to their parent mutables
    for (Mutable node : mutables.values()) {
      if (node.parentKey == null) {
        continue; // It's a root node
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

    // Create the root nodes, then recursively create their children
    SortedMap<String, Theme> created = Maps.newTreeMap();
    for (Mutable node : mutables.values()) {
      if (node.parent == null) {
        createImmutableNodes(node, null, created);
      }
    }

    // Cycles in the inheritance graph don't belong to any tree, so they wouldn't have gotten picked up
    if (created.size() < mutables.size()) {
      Set<String> cyclicKeys = Sets.difference(mutables.keySet(), created.keySet());
      throw new ThemeConfigurationException("Theme parents have one or more cycles: " + cyclicKeys);
    }

    return new ThemeTree(created);
  }

  private static Mutable buildFromJson(Map<String, Mutable> mutables, Map<String, ?> themeJsonObj) {
    String key = (String) themeJsonObj.get("key");
    Mutable m = new Mutable();
    m.key = key;
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

  ImmutableMap<String, Theme> matchToJournals(List<Map<String, ?>> journalConfigJson) {
    SortedMap<String, Theme> journalMap = Maps.newTreeMap();
    for (Map<String, ?> journalObj : journalConfigJson) {
      String key = (String) journalObj.get("key");
      String themeName = (String) journalObj.get("theme");
      journalMap.put(key, themes.get(themeName));
    }
    return ImmutableMap.copyOf(journalMap);
  }

}
