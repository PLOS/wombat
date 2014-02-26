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

package org.ambraproject.wombat.config;

import freemarker.cache.TemplateLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * Implementation of {@link Theme} suitable for tests.  All resources are loaded from the classpath.
 */
public class TestClasspathTheme extends Theme {

  public TestClasspathTheme() {
    super("test", null);
  }

  @Override
  public TemplateLoader getTemplateLoader() throws IOException {
    throw new IllegalStateException("Not yet implemented for testing");
  }

  @Override
  protected InputStream fetchStaticResource(String path) throws IOException {

    // Huge hack: the "real" themes include a file that specifies the theme
    // key.  Instead of including a test version, we just look for the filename.
    if (path.equals("config/" + Site.JOURNAL_KEY_PATH + ".json")) {
      String dummyJson = String.format("{\"%s\": \"%s\"}", Site.CONFIG_KEY_FOR_JOURNAL, "default");
      return new ByteArrayInputStream(dummyJson.getBytes());
    } else {
      return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
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
}
