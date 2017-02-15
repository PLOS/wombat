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

import com.google.common.base.Joiner;
import freemarker.cache.TemplateLoader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

/**
 * Implementation of {@link org.ambraproject.wombat.config.theme.Theme} suitable for tests.  All resources are loaded
 * from the classpath.
 */
public class TestClasspathTheme extends Theme {

  private static final String TEST_RESOURCE_DIR = "test_themes";

  /**
   * Constructor intended for simple tests where a theme has no parent (or is the parent).
   */
  public TestClasspathTheme() {
    super("test", null);
  }

  public TestClasspathTheme(String key, List<? extends Theme> parents) {
    super(key, parents);
  }

  @Override
  public TemplateLoader getTemplateLoader() throws IOException {
    throw new IllegalStateException("Not yet implemented for testing");
  }

  @Override
  protected InputStream fetchStaticResource(String path) throws IOException {

    // In order to use the "live" config files for the root theme, we treat it
    // differently here.
    if ("root".equals(getKey())) {
      String fullPath = Joiner.on(File.separator).join("src", "main", "webapp", "WEB-INF", "themes", "root", path);
      File file = new File(fullPath);
      return file.exists() ? new BufferedInputStream(new FileInputStream(fullPath)) : null;
    } else {
      String fullPath = Joiner.on(File.separator).join(TEST_RESOURCE_DIR, getKey(), path);
      return Thread.currentThread().getContextClassLoader().getResourceAsStream(fullPath);
    }
  }

  @Override
  public Collection<String> fetchStaticResourcePaths(String root) throws IOException {
    throw new IllegalStateException("Not yet implemented for testing");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected ResourceAttributes fetchResourceAttributes(String path) throws IOException {
    throw new IllegalStateException("Not yet implemented for testing");
  }

  @Override
  public String describeSource() {
    return getClass().getName();
  }
}
