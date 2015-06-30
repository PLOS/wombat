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
import org.ambraproject.rhombat.cache.Cache;
import org.ambraproject.rhombat.cache.NullCache;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.theme.TestClasspathTheme;
import org.ambraproject.wombat.config.theme.ThemeTree;
import org.ambraproject.wombat.service.AssetService;
import org.ambraproject.wombat.service.AssetServiceImpl;
import org.ambraproject.wombat.service.remote.CachedRemoteService;
import org.ambraproject.wombat.service.remote.JsonService;
import org.ambraproject.wombat.service.remote.SearchService;
import org.ambraproject.wombat.service.remote.SolrSearchService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.Reader;

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
    TestClasspathTheme testClasspathTheme = new TestClasspathTheme();
    return runtimeConfiguration.getThemes(ImmutableSet.of(testClasspathTheme), testClasspathTheme);
  }

  @Bean
  public SiteSet siteSet(RuntimeConfiguration runtimeConfiguration, ThemeTree themeTree) {
    return runtimeConfiguration.getSites(themeTree);
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
  public SolrSearchService getSearchService() {
    return new SolrSearchService();
  }
}
