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

package org.ambraproject.wombat.service;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;

import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.ambraproject.wombat.config.TestSpringConfiguration;
import org.ambraproject.wombat.config.site.SiteSet;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration
public class AssetServiceTest extends AbstractJUnit4SpringContextTests {
  @Import(TestSpringConfiguration.class)
  @Configuration
  static class ContextConfiguration {
    @Bean
    AssetService assetService() {
      return new AssetServiceImpl();
    }
  }
  
  private static final String DATA_PATH = Joiner.on(File.separator).join("src", "test", "resources", "test_themes",
      "site1", AssetService.AssetUrls.RESOURCE_NAMESPACE, "js") + File.separator;

  @Autowired
  AssetService assetService;
  
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
        assertEquals("magicValue", expected);
      }
    }

    // Now we compile the same javascript files, execute them, and check the value returned.
    List<String> jsFiles = new ArrayList<>();
    jsFiles.add("resource/js/test1.js");
    jsFiles.add("resource/js/test2.js");
    String compiledJsPath = assetService.getCompiledAssetLink(AssetService.AssetType.JS, jsFiles,
        siteSet.getSite("site1"));
    String[] fields = compiledJsPath.split("/");
    String basename = fields[fields.length - 1];

    Context jsContext = Context.enter();
    jsContext.setOptimizationLevel(-1);
    Scriptable jsScope = jsContext.initStandardObjects();
    File file = new File(runtimeConfiguration.getCompiledAssetDir() + File.separator + basename);
    try (FileReader fr = new FileReader(file)) {
      Object actual = jsContext.evaluateReader(jsScope, fr, basename, 1, null);
      assertEquals(expected, actual);
    }
  }

  /**
   * Tests that invalid javascript cannot be compiled.
   */
  @Test(expected = AssetCompilationException.class)
  public void testJsError() throws Exception {
    List<String> jsFiles = new ArrayList<>();
    jsFiles.add("resource/js/error.js");
    assetService.getCompiledAssetLink(AssetService.AssetType.JS, jsFiles, siteSet.getSite("site1"));
  }
}
