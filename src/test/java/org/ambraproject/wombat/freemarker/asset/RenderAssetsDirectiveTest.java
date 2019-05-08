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

package org.ambraproject.wombat.freemarker.asset;

import org.ambraproject.wombat.config.TestSpringConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
@ContextConfiguration(classes = TestSpringConfiguration.class)
public class RenderAssetsDirectiveTest extends AbstractJUnit4SpringContextTests {

  /*
   * Convenience method.
   */
  private static AssetNode node(String path, String... dependencies) {
    return new AssetNode(path, Arrays.asList(dependencies));
  }
  
  @Parameters
  public static Collection<Object[]> assetNodes() {
    return Arrays
      .asList(new Object[][]{
        {new AssetNode[]{}, new String[]{}},
        {new AssetNode[]{node("a"), node("b"), node("c")}, new String[]{"a", "b", "c"}},
        {new AssetNode[]{node("a"), node("b", "c"), node("c"), node("d")}, new String[]{"a", "c", "b", "d"}},
        {
            new AssetNode[]{
                node("2", "11"),
                node("3"),
                node("5"),
                node("7"),
                node("8", "3", "7"),
                node("9", "8", "11"),
                node("10", "3", "11"),
                node("11", "5", "7"),
            },
            new String[]{"3", "5", "7", "8", "11", "2", "9", "10"}
        },
        });
  }

  @Parameter(0)
  public AssetNode[] nodes;

  @Parameter(1)
  public String[] expectedPaths;

  @Test
  public void testSortNodes() throws Exception {
    List<String> results = RenderAssetsDirective.sortNodes(Arrays.asList(nodes));
    assertEquals(Arrays.asList(expectedPaths), results);
  }

  @Test(expected = RuntimeException.class)
  public void testSortNodesSelfDependency() throws Exception {
    RenderAssetsDirective.sortNodes(Arrays.asList(node("a", "a")));
  }

  @Test(expected = RuntimeException.class)
  public void testSortNodesMissingDep() throws Exception {
    RenderAssetsDirective.sortNodes(Arrays.asList(node("a", "nonexistent")));
  }

  @Test(expected = RuntimeException.class)
  public void testSortNodesCyclic() throws Exception {
    RenderAssetsDirective.sortNodes(Arrays.asList(node("a", "b"), node("b", "a")));
  }

  @Test(expected = RuntimeException.class)
  public void testSortNodesMissingDepDeep() throws Exception {
    RenderAssetsDirective.sortNodes(Arrays.asList(node("a"), node("b", "c"), node("c", "b"), node("d")));
  }
}
