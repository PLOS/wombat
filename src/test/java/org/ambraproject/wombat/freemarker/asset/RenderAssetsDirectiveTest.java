/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2013 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
