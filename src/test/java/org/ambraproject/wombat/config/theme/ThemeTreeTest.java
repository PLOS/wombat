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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.ambraproject.wombat.config.TestSpringConfiguration;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.util.MockSiteUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;

import static org.testng.Assert.assertEquals;

@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = TestSpringConfiguration.class)
public class ThemeTreeTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private SiteSet siteSet;

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

  @Test
  public void testInheritPropertyFromRoot() throws IOException {
    Site site = MockSiteUtil.getByUniqueJournalKey(siteSet, "journal1Key");
    Map<String, Object> journal = site.getTheme().getConfigMap("homepage");
    Object inheritedValue = journal.get("defaultSelection");
    assertEquals(inheritedValue, "recent"); // expected to match src/main/webapp/WEB-INF/themes/root/config/homepage.yaml
  }

  @Test
  public void testCanOverridePropertyFromRoot() throws IOException {
    Site site = MockSiteUtil.getByUniqueJournalKey(siteSet, "journal2Key");
    Map<String, Object> journal = site.getTheme().getConfigMap("homepage");
    Object inheritedValue = journal.get("defaultSelection");
    assertEquals(inheritedValue, "popular"); // expected to match src/test/resources/test_themes/site2/config/homepage.json
  }

}
