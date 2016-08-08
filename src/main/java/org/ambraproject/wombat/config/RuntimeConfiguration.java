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

import com.google.common.collect.ImmutableSet;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.theme.Theme;
import org.ambraproject.wombat.config.theme.ThemeTree;

import java.net.URL;
import java.util.Collection;


/**
 * Interface that represents configurable values that are only known at server startup time.
 */
public interface RuntimeConfiguration {

  /**
   * @return the directory in which to write and serve compiled assets (.js and .css), or {@code null} to not compile
   * assets due to being in dev mode
   */
  String getCompiledAssetDir();

  interface CacheConfiguration {
    /**
     * @return the value to pass to Ehcache to set the size of its local heap
     */
    String getMaxBytesLocalHeap();
  }

  CacheConfiguration getCacheConfiguration();

  interface HttpConnectionPoolConfiguration {
    /**
     * @see org.apache.http.pool.ConnPoolControl
     */
    Integer getMaxTotal();

    /**
     * @see org.apache.http.pool.ConnPoolControl
     */
    Integer getDefaultMaxPerRoute();
  }

  HttpConnectionPoolConfiguration getHttpConnectionPoolConfiguration();

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

  /**
   * Get the host name of the server to which to send SMTP messages;
   */
  String getMailServer();

  /**
   * Get the path of an HTML document to display on the root page
   */
  String getRootPagePath();
  /**
   * @return the set of enabled dev features, configured in wombat.yaml.
   */
  ImmutableSet<String> getEnabledDevFeatures();

  /**
   * Parse the user-defined themes.
   *
   * @param internalThemes constant themes provided by the webapp
   * @param rootTheme      the default parent theme to be applied to any user-defined theme without an explicit parent
   * @return the set of all available themes
   * @throws org.ambraproject.wombat.config.theme.ThemeTree.ThemeConfigurationException
   * @throws java.lang.IllegalArgumentException                                         if {@code internalThemes} does
   *                                                                                    not contain {@code rootTheme}
   */
  ThemeTree getThemes(Collection<? extends Theme> internalThemes, Theme rootTheme) throws ThemeTree.ThemeConfigurationException;

  /**
   * Produce a map from site keys to each site's theme.
   *
   * @param themeTree the set of available themes
   * @return map from site keys to each site's theme
   */
  SiteSet getSites(ThemeTree themeTree);

  interface CasConfiguration {
    String getCasUrl();

    String getLoginUrl();

    String getLogoutUrl();
  }

  CasConfiguration getCasConfiguration();

}
