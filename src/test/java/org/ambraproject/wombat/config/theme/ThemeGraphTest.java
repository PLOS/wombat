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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import org.ambraproject.wombat.config.TestSpringConfiguration;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.util.MockSiteUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration(classes = TestSpringConfiguration.class)
public class ThemeGraphTest extends AbstractJUnit4SpringContextTests {

  @Autowired
  private SiteSet siteSet;

  /**
   * Create a dummy theme.
   */
  private static ThemeBuilder<TestClasspathTheme> theme(String key, String... parentKeys) {
    return new ThemeBuilder<>(key, ImmutableList.copyOf(parentKeys), TestClasspathTheme::new);
  }

  private static final ImmutableList<ThemeBuilder<?>> THEME_GRAPH_CASE = ImmutableList.of(
      theme("root1"),
      theme("root2"),
      theme("child1-1", "root1"),
      theme("child1-2", "root1"),
      theme("child1-2-1", "child1-2"),
      theme("child2-1", "root2"),

      // Should be able to declare children before their parents
      theme("child3", "root3"),
      theme("root3"),

      theme("multichild1-1", "child1-1", "child1-2"),
      theme("multichild1-2", "root1", "child1-1", "child1-2"), // redundant parent should change nothing
      theme("multichild2", "child1-1", "child1-2", "child2-1")
  );

  private static final ImmutableList<ThemeBuilder<?>> THEME_GRAPH_CYCLE_CASE = ImmutableList.of(
      theme("node1", "node2"), theme("node2", "node1"));

  private static void assertChainIs(ThemeGraph themeGraph, String themeKey, String... expectedParentKeys) {
    Theme themeUnderTest = themeGraph.getTheme(themeKey);
    ImmutableList<Theme> chain = themeUnderTest.getInheritanceChain();
    assertEquals(1 + expectedParentKeys.length, chain.size());
    assertEquals(themeUnderTest, chain.get(0));
    for (int i = 0; i < expectedParentKeys.length; i++) {
      assertEquals(themeGraph.getTheme(expectedParentKeys[i]), chain.get(i + 1));
    }
  }

  @Test
  public void testParse() throws ThemeGraph.ThemeConfigurationException {
    TestClasspathTheme testClasspathTheme = new TestClasspathTheme();
    String classpathThemeKey = testClasspathTheme.getKey();
    ThemeGraph themeGraph = ThemeGraph.create(testClasspathTheme, ImmutableList.of(testClasspathTheme), THEME_GRAPH_CASE);
    assertEquals(THEME_GRAPH_CASE.size() + 1, themeGraph.getThemes().size());

    assertChainIs(themeGraph, classpathThemeKey);
    assertChainIs(themeGraph, "root1", classpathThemeKey);
    assertChainIs(themeGraph, "root2", classpathThemeKey);
    assertChainIs(themeGraph, "child1-1", "root1", classpathThemeKey);
    assertChainIs(themeGraph, "child1-2", "root1", classpathThemeKey);
    assertChainIs(themeGraph, "child1-2-1", "child1-2", "root1", classpathThemeKey);
    assertChainIs(themeGraph, "child2-1", "root2", classpathThemeKey);
    assertChainIs(themeGraph, "root3", classpathThemeKey);
    assertChainIs(themeGraph, "child3", "root3", classpathThemeKey);

    assertChainIs(themeGraph, "multichild1-1", "child1-1", "child1-2", "root1", classpathThemeKey);
    assertChainIs(themeGraph, "multichild1-2", "child1-1", "child1-2", "root1", classpathThemeKey);
    assertChainIs(themeGraph, "multichild2", "child1-1", "child1-2", "root1", "child2-1", "root2", classpathThemeKey);
  }

  @Test(expected = ThemeGraph.ThemeConfigurationException.class)
  public void testParseCycle() throws ThemeGraph.ThemeConfigurationException {
    TestClasspathTheme testClasspathTheme = new TestClasspathTheme();
    ThemeGraph.create(testClasspathTheme, ImmutableList.of(testClasspathTheme), THEME_GRAPH_CYCLE_CASE);
  }

  @Test
  public void testInheritPropertyFromRoot() throws IOException {
    Site site = MockSiteUtil.getByUniqueJournalKey(siteSet, "journal1Key");
    Map<String, Object> journal = site.getTheme().getConfigMap("homepage");
    Object inheritedValue = journal.get("defaultSelection");
    assertEquals(inheritedValue, null); // expected to match src/main/webapp/WEB-INF/themes/root/config/homepage.yaml
  }

  @Test
  public void testCanOverridePropertyFromRoot() throws IOException {
    Site site = MockSiteUtil.getByUniqueJournalKey(siteSet, "journal2Key");
    Map<String, Object> journal = site.getTheme().getConfigMap("homepage");
    Object inheritedValue = journal.get("defaultSelection");
    assertEquals(inheritedValue, "popular"); // expected to match src/test/resources/test_themes/site2/config/homepage.json
  }

}
