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
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.testng.Assert.assertEquals;

@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = TestSpringConfiguration.class)
public class RenderAssetsDirectiveTest extends AbstractTestNGSpringContextTests {

  /*
   * Convenience method.
   */
  private static AssetNode node(String path, String... dependencies) {
    return new AssetNode(path, Arrays.asList(dependencies));
  }

  @DataProvider
  public Object[][] assetNodes() {
    return new Object[][]{
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
    };
  }

  @Test(dataProvider = "assetNodes")
  public void testSortNodes(AssetNode[] nodes, String[] expectedPaths) throws Exception {
    assertEquals(RenderAssetsDirective.sortNodes(Arrays.asList(nodes)), Arrays.asList(expectedPaths));
  }

  @DataProvider
  public Object[][] cyclicNodes() {
    return new Object[][]{
        {new AssetNode[]{node("a", "a")}},
        {new AssetNode[]{node("a", "nonexistent")}},
        {new AssetNode[]{node("a", "b"), node("b", "a")}},
        {new AssetNode[]{node("a"), node("b", "c"), node("c", "b"), node("d")}},
    };
  }

  @Test(dataProvider = "cyclicNodes", expectedExceptions = RuntimeException.class)
  public void testSortNodesWithCycle(AssetNode[] nodes) throws Exception {
    RenderAssetsDirective.sortNodes(Arrays.asList(nodes));
  }

}
