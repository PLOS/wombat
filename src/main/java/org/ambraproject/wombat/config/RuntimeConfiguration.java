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

import com.google.common.collect.ImmutableMap;
import org.ambraproject.wombat.controller.ControllerHook;

import java.net.URL;
import java.util.Collection;

/**
 * Interface that represents configurable values that are only known at server startup time.
 */
public interface RuntimeConfiguration {

  /**
   * Check whether this webapp is configured to naively trust servers without SSL certificates. This should be {@code
   * true} only when debugging or connecting to a private testing server. Defaults to {@code false}.
   *
   * @return {@code false} if default SSL authentication should be preserved
   */
  boolean trustUnsignedServer();

  /**
   * @return true if we are running in "dev mode" for .js and .css, and compilation/minification
   *     should not happen for these files
   */
  boolean devModeAssets();

  /**
   * @return the directory in which to write and serve compiled assets (.js and .css).  Not relevant
   *     if devModeAssets is true.
   */
  String getCompiledAssetDir();

  /**
   * @return the memcached host, or null if it is not present in the config
   */
  String getMemcachedHost();

  /**
   * @return the memcached port, or -1 if it is not present in the config
   */
  int getMemcachedPort();

  /**
   * @return the cacheAppPrefix value, or null if it is not defined in the config.  This should
   *     be a String that is shared by all wombat app servers, defining a namespace for them.
   */
  String getCacheAppPrefix();

  /**
   * Get the URL of the SOA server.
   *
   * @return the URL
   */
  URL getServer();

  /**
   * Get the URL of the solr search server.
   *
   * @return the URL
   */
  URL getSolrServer();

  ThemeTree getThemes(Collection<? extends Theme> internalThemes, Theme rootTheme) throws ThemeTree.ThemeConfigurationException;

  ImmutableMap<String, Theme> getThemesForSites(ThemeTree themeTree);

  /**
   * Returns the {@link ControllerHook} that adds additional model data needed to render the
   * home page for a given site, or null if one is not needed.
   *
   * @param site string identifying the site
   * @return ControllerHook instance, or null
   */
  ControllerHook getHomePageHook(String site);
}
