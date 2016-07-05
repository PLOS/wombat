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

import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.theme.TestClasspathTheme;
import org.ambraproject.wombat.config.theme.Theme;
import org.ambraproject.wombat.config.theme.ThemeTree;
import org.ambraproject.wombat.model.TaxonomyCountTable;
import org.ambraproject.wombat.model.TaxonomyGraph;
import org.ambraproject.wombat.service.AssetService;
import org.ambraproject.wombat.service.AssetServiceImpl;
import org.ambraproject.wombat.service.remote.CachedRemoteService;
import org.ambraproject.wombat.service.remote.JsonService;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.ambraproject.wombat.service.remote.SolrSearchApiImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.integration.CompletionListener;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
    return runtimeConfiguration.getThemes(themes, rootTheme);
  }

  @Bean
  public SiteSet siteSet(RuntimeConfiguration runtimeConfiguration, ThemeTree themeTree) {
    return runtimeConfiguration.getSites(themeTree);
  }

  @Bean
  public AssetService assetService() {
    return new AssetServiceImpl();
  }

  private static class NullCache<T, V> implements Cache<T, V> {

    @Override
    public V get(T t) {
      return null;
    }

    @Override
    public Map<T, V> getAll(Set<? extends T> set) {
      return null;
    }

    @Override
    public boolean containsKey(T t) {
      return false;
    }

    @Override
    public void loadAll(Set<? extends T> set, boolean b, CompletionListener completionListener) {

    }

    @Override
    public void put(T t, V v) {

    }

    @Override
    public V getAndPut(T t, V v) {
      return null;
    }

    @Override
    public void putAll(Map<? extends T, ? extends V> map) {

    }

    @Override
    public boolean putIfAbsent(T t, V v) {
      return false;
    }

    @Override
    public boolean remove(T t) {
      return false;
    }

    @Override
    public boolean remove(T t, V v) {
      return false;
    }

    @Override
    public V getAndRemove(T t) {
      return null;
    }

    @Override
    public boolean replace(T t, V v, V v1) {
      return false;
    }

    @Override
    public boolean replace(T t, V v) {
      return false;
    }

    @Override
    public V getAndReplace(T t, V v) {
      return null;
    }

    @Override
    public void removeAll(Set<? extends T> set) {

    }

    @Override
    public void removeAll() {

    }

    @Override
    public void clear() {

    }

    @Override
    public <T1> T1 invoke(T t, EntryProcessor<T, V, T1> entryProcessor, Object... objects) throws EntryProcessorException {
      return null;
    }

    @Override
    public <T1> Map<T, EntryProcessorResult<T1>> invokeAll(Set<? extends T> set, EntryProcessor<T, V, T1> entryProcessor, Object... objects) {
      return null;
    }

    @Override
    public String getName() {
      return null;
    }

    @Override
    public CacheManager getCacheManager() {
      return null;
    }

    @Override
    public void close() {

    }

    @Override
    public boolean isClosed() {
      return false;
    }

    @Override
    public <T1> T1 unwrap(Class<T1> aClass) {
      return null;
    }

    @Override
    public void registerCacheEntryListener(CacheEntryListenerConfiguration<T, V> cacheEntryListenerConfiguration) {

    }

    @Override
    public void deregisterCacheEntryListener(CacheEntryListenerConfiguration<T, V> cacheEntryListenerConfiguration) {

    }

    @Override
    public Iterator<Entry<T, V>> iterator() {
      return null;
    }

    @Override
    public <C extends javax.cache.configuration.Configuration<T, V>> C getConfiguration(Class<C> aClass) {
      return null;
    }
  }
  @Bean
  public Cache<String, String> assetFilenameCache() throws IOException {
    return new NullCache<String, String>();
  }

  @Bean
  public Cache<String, Object> assetContentCache() throws IOException {
    return new NullCache<String, Object>();
  }

  @Bean
  public Cache<String, TaxonomyGraph> taxonomyGraphCache() throws IOException {
    return new NullCache<String, TaxonomyGraph>();
  }

  @Bean
  public Cache<String, TaxonomyCountTable> taxonomyCountTableCache() throws IOException {
    return new NullCache<String, TaxonomyCountTable>();
  }

  @Bean
  public Cache<String, List> recentArticleCache() throws IOException {
    return new NullCache<String, List>();
  }

  @Bean
  public Cache<String, Object> remoteServiceCache() throws IOException {
    return new NullCache<String, Object>();
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
