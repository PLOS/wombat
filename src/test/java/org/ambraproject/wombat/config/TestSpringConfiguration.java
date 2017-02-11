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

import org.ambraproject.rhombat.cache.Cache;
import org.ambraproject.rhombat.cache.NullCache;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.theme.TestClasspathTheme;
import org.ambraproject.wombat.config.theme.Theme;
import org.ambraproject.wombat.config.theme.ThemeTree;
import org.ambraproject.wombat.service.AssetService;
import org.ambraproject.wombat.service.AssetServiceImpl;
import org.ambraproject.wombat.service.remote.CachedRemoteService;
import org.ambraproject.wombat.service.remote.JsonService;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.ambraproject.wombat.service.remote.SolrSearchApiImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.Reader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Defines spring beans needed by tests.
 */
@Configuration
public class TestSpringConfiguration {

  @Bean
  public RuntimeConfiguration runtimeConfiguration() {
    return new TestRuntimeConfiguration();
  }

  @Bean
  public ThemeTree themeTree(RuntimeConfiguration runtimeConfiguration)
      throws ThemeTree.ThemeConfigurationException {
    Set<Theme> themes = new HashSet<>();
    TestClasspathTheme rootTheme = new TestClasspathTheme("root", null);
    themes.add(rootTheme);
    TestClasspathTheme theme1 = new TestClasspathTheme("site1", Collections.singletonList(rootTheme));
    themes.add(theme1);
    TestClasspathTheme theme2 = new TestClasspathTheme("site2", Collections.singletonList(rootTheme));
    themes.add(theme2);
    return null;//runtimeConfiguration.getThemes(themes, rootTheme);
  }

  @Bean
  public SiteSet siteSet(RuntimeConfiguration runtimeConfiguration, ThemeTree themeTree) {
    return null;//runtimeConfiguration.getSites(themeTree);
  }

  @Bean
  public AssetService assetService() {
    return new AssetServiceImpl();
  }

  @Bean
  public Cache cache() {
    return new NullCache();
  }

  @Bean
  public JsonService jsonService() {

    // TODO: stub out if necessary for any test.
    return null;
  }

  @Bean
  public CachedRemoteService<Reader> cachedRemoteReader() {

    // TODO: stub out if necessary for any test.
    return null;
  }

  @Bean
  public ArticleApi articleApi() {

    // TODO: stub out if necessary for any test.
    return null;
  }

  @Bean
  public SolrSearchApiImpl getSearchService() {
    return new SolrSearchApiImpl();
  }
}
