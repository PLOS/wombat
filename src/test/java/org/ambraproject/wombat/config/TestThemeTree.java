package org.ambraproject.wombat.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class TestThemeTree {

  /**
   * Create a dummy theme.
   */
  private static ImmutableMap<String, Object> theme(String key, String... parentKeys) {
    ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
    builder.put("path", ".");
    builder.put("key", key);
    if (parentKeys != null) {
      builder.put("parent", ImmutableList.copyOf(parentKeys));
    }
    return builder.build();
  }

  private static final ImmutableList<ImmutableMap<String, Object>> THEME_TREE_CASE = ImmutableList.of(
      theme("root1", null),
      theme("root2", null),
      theme("child1-1", "root1"),
      theme("child1-2", "root1"),
      theme("child1-2-1", "child1-2"),
      theme("child2-1", "root2"),

      // Should be able to declare children before their parents
      theme("child3", "root3"),
      theme("root3", null),

      theme("multichild1-1", "child1-1", "child1-2"),
      theme("multichild1-2", "root1", "child1-1", "child1-2"), // redundant parent should change nothing
      theme("multichild2", "child1-1", "child1-2", "child2-1")
  );

  private static final ImmutableList<ImmutableMap<String, Object>> THEME_TREE_CYCLE_CASE = ImmutableList.of(
      theme("node1", "node2"), theme("node2", "node1"));

  private static ImmutableList<Theme> getChain(ThemeTree themeTree, String themeKey) {
    return ImmutableList.copyOf(themeTree.getTheme(themeKey).getChain());
  }

  private static void assertChainIs(ThemeTree themeTree, String themeKey, String... expectedParentKeys) {
    ImmutableList<Theme> chain = getChain(themeTree, themeKey);
    assertEquals(chain.size(), 1 + expectedParentKeys.length);
    assertEquals(chain.get(0), themeTree.getTheme(themeKey));
    for (int i = 0; i < expectedParentKeys.length; i++) {
      assertEquals(chain.get(i + 1), themeTree.getTheme(expectedParentKeys[i]));
    }
  }

  @Test
  public void testParse() throws ThemeTree.ThemeConfigurationException {
    TestClasspathTheme testClasspathTheme = new TestClasspathTheme();
    String classpathThemeKey = testClasspathTheme.getKey();
    ThemeTree themeTree = ThemeTree.parse(THEME_TREE_CASE, ImmutableList.of(testClasspathTheme), testClasspathTheme);
    assertEquals(themeTree.getThemes().size(), THEME_TREE_CASE.size() + 1);

    assertChainIs(themeTree, classpathThemeKey);
    assertChainIs(themeTree, "root1", classpathThemeKey);
    assertChainIs(themeTree, "root2", classpathThemeKey);
    assertChainIs(themeTree, "child1-1", "root1", classpathThemeKey);
    assertChainIs(themeTree, "child1-2", "root1", classpathThemeKey);
    assertChainIs(themeTree, "child1-2-1", "child1-2", "root1", classpathThemeKey);
    assertChainIs(themeTree, "child2-1", "root2", classpathThemeKey);
    assertChainIs(themeTree, "root3", classpathThemeKey);
    assertChainIs(themeTree, "child3", "root3", classpathThemeKey);

    assertChainIs(themeTree, "multichild1-1", "child1-1", "child1-2", "root1", classpathThemeKey);
    assertChainIs(themeTree, "multichild1-2", "child1-1", "child1-2", "root1", classpathThemeKey);
    assertChainIs(themeTree, "multichild2", "child1-1", "child1-2", "root1", "child2-1", "root2", classpathThemeKey);
  }

  @Test(expectedExceptions = ThemeTree.ThemeConfigurationException.class)
  public void testParseCycle() throws ThemeTree.ThemeConfigurationException {
    TestClasspathTheme testClasspathTheme = new TestClasspathTheme();
    ThemeTree.parse(THEME_TREE_CYCLE_CASE, ImmutableList.of(testClasspathTheme), testClasspathTheme);
  }

}
