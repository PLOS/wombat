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

package org.ambraproject.wombat.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.theme.ThemeSource;

import java.net.URL;
import java.util.Optional;


/**
 * Interface that represents configurable values that are only known at server startup time.
 */
public interface RuntimeConfiguration {
  /**
   * @return the memcached host, or null if it is not present in the config
   */
  String getMemcachedHost();

  /**
   * @return the memcached port, or -1 if it is not present in the config
   */
  int getMemcachedPort();

  /**
   * Get the URL of the rhino server.
   *
   * @return the URL
   */
  URL getRhinoServerUrl();

  /**
   * Get the path of an HTML document to display on the root page
   */
  String getRootPagePath();

  /**
   * Get the name of the runtime environment ("prod", "dev", etc.)
   */
  String getEnvironment();

  ImmutableList<ThemeSource<?>> getThemeSources();

  String getCasUrl();

  interface SolrConfiguration {
    Optional<URL> getUrl();

    String getJournalsCollection();
  }

  interface UserApiConfiguration {
    String getServerUrl();

    String getAppName();

    String getPassword();
  }

  Optional<SolrConfiguration> getSolrConfiguration();

  Optional<UserApiConfiguration> getUserApiConfiguration();
}
