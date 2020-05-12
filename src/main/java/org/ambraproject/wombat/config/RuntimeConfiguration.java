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
import java.util.Date;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.UtcDateTypeAdapter;
import org.ambraproject.wombat.util.JodaTimeLocalDateAdapter;


/**
 * Interface that represents configurable values that are only known at server startup time.
 */
public interface RuntimeConfiguration {

  public static Gson makeGson() {
    GsonBuilder builder = new GsonBuilder();
    builder.setPrettyPrinting();
    builder.registerTypeAdapter(Date.class, new UtcDateTypeAdapter());
    builder.registerTypeAdapter(org.joda.time.LocalDate.class, JodaTimeLocalDateAdapter.INSTANCE);
    return builder.create();
  }

  /**
   * @return the directory in which to write and serve compiled assets (.js and .css), or {@code null} to not compile
   * assets due to being in dev mode
   */
  String getCompiledAssetDir();

  interface CacheConfiguration {
    /**
     * @return the memcached host, or null if it is not present in the config
     */
    String getMemcachedHost();

    /**
     * @return the memcached port, or -1 if it is not present in the config
     */
    int getMemcachedPort();

    /**
     * @return the cacheAppPrefix value, or null if it is not defined in the config.  This should be a String that is
     * shared by all wombat app servers, defining a namespace for them.
     */
    String getCacheAppPrefix();
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
   * Get the path of an HTML document to display on the root page
   */
  String getRootRedirect();

  /**
   * Show debugging information?
   */
  boolean showDebug();

  /**
   * @return the set of enabled dev features, configured in wombat.yaml.
   */
  ImmutableSet<String> getEnabledDevFeatures();

  ImmutableList<ThemeSource<?>> getThemeSources();

  interface CasConfiguration {
    String getCasUrl();

    String getLoginUrl();

    String getLogoutUrl();
  }

  interface SolrConfiguration {
    Optional<URL> getUrl();

    Optional<URL> getUrl(Site site);

    String getJournalsCollection();

    String getPreprintsCollection();
  }

  interface UserApiConfiguration {
    String getServerUrl();

    String getAppName();

    String getPassword();
  }

  Optional<CasConfiguration> getCasConfiguration();

  Optional<SolrConfiguration> getSolrConfiguration();

  Optional<UserApiConfiguration> getUserApiConfiguration();

  boolean areCommentsDisabled();

  String getCollectionsUrl();
  
}
