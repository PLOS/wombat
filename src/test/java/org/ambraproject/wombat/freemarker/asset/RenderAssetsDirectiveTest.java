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
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = TestSpringConfiguration.class)
public class RenderAssetsDirectiveTest extends AbstractTestNGSpringContextTests {

  @Test
  public void testGetPathDepths() throws Exception {
    {
      List<String> paths = new ArrayList<>();
      paths.add("../foo");
      RenderAssetsDirective.GetPathDepthsResult actual = RenderAssetsDirective.getPathDepths(paths);
      assertEquals(actual.depth, 1);
      List<String> expectedPaths = new ArrayList<>();
      expectedPaths.add("foo");
      assertEquals(actual.depthlessPaths, expectedPaths);
    }
    {
      List<String> paths = new ArrayList<>();
      paths.add("../../../foo");
      paths.add("../../../bar");
      paths.add("../../../blaz");
      RenderAssetsDirective.GetPathDepthsResult actual = RenderAssetsDirective.getPathDepths(paths);
      assertEquals(actual.depth, 3);
      List<String> expectedPaths = new ArrayList<>();
      expectedPaths.add("foo");
      expectedPaths.add("bar");
      expectedPaths.add("blaz");
      assertEquals(actual.depthlessPaths, expectedPaths);
    }
  }

  @Test(expectedExceptions = {IllegalArgumentException.class})
  public void testGetPathDepthsError() {
    List<String> paths = new ArrayList<>();
    paths.add("../../foo");
    paths.add("../bar");
    RenderAssetsDirective.getPathDepths(paths);
  }

  @Test
  public void testGetDepthPrefix() throws Exception {
    assertEquals(RenderAssetsDirective.getDepthPrefix(0), "");
    assertEquals(RenderAssetsDirective.getDepthPrefix(1), "../");
    assertEquals(RenderAssetsDirective.getDepthPrefix(2), "../../");
    assertEquals(RenderAssetsDirective.getDepthPrefix(3), "../../../");
  }
}
