/*
 * Copyright (c) 2006-2013 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.wombat.service;

import com.google.common.base.Joiner;
import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.ambraproject.wombat.config.TestSpringConfiguration;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.controller.StaticResourceController;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = TestSpringConfiguration.class)
public class AssetServiceTest extends AbstractTestNGSpringContextTests {

  private static final String DATA_PATH = Joiner.on(File.separator).join("src", "test", "resources",
      StaticResourceController.RESOURCE_NAMESPACE, "js") + File.separator;

  @Autowired
  private AssetService assetService;

  @Autowired
  private RuntimeConfiguration runtimeConfiguration;

  @Autowired
  private SiteSet siteSet;

  @Test
  public void testCompiledJs() throws Exception {

    // First, we use a javascript runtime to execute some uncompiled javascript, to
    // get the expected value.
    Object expected;
    {
      Context jsContext = Context.enter();
      jsContext.setOptimizationLevel(-1);
      Scriptable jsScope = jsContext.initStandardObjects();
      try (FileReader reader1 = new FileReader(new File(DATA_PATH + "test1.js"));
           FileReader reader2 = new FileReader(new File(DATA_PATH + "test2.js"))) {
        jsContext.evaluateReader(jsScope, reader1, "test1.js", 1, null);
        expected = jsContext.evaluateReader(jsScope, reader2, "test2.js", 1, null);
        assertEquals(expected, "magicValue");
      }
    }

    // Now we compile the same javascript files, execute them, and check the value returned.
    List<String> jsFiles = new ArrayList<>();
    jsFiles.add("resource/js/test1.js");
    jsFiles.add("resource/js/test2.js");
    String compiledJsPath = assetService.getCompiledAssetLink(AssetService.AssetType.JS, jsFiles,
        siteSet.getSite("default"));
    String[] fields = compiledJsPath.split("/");
    String basename = fields[fields.length - 1];

    Context jsContext = Context.enter();
    jsContext.setOptimizationLevel(-1);
    Scriptable jsScope = jsContext.initStandardObjects();
    File file = new File(runtimeConfiguration.getCompiledAssetDir() + File.separator + basename);
    try (FileReader fr = new FileReader(file)) {
      Object actual = jsContext.evaluateReader(jsScope, fr, basename, 1, null);
      assertEquals(actual, expected);
    }
  }

  /**
   * Tests that invalid javascript cannot be compiled.
   */
  @Test(expectedExceptions = {AssetCompilationException.class})
  public void testJsError() throws Exception {
    List<String> jsFiles = new ArrayList<>();
    jsFiles.add("resource/js/error.js");
    assetService.getCompiledAssetLink(AssetService.AssetType.JS, jsFiles, siteSet.getSite("default"));
  }
}
