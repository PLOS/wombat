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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import org.ambraproject.wombat.cache.Cache;
import org.ambraproject.wombat.cache.MemcacheClient;
import org.ambraproject.wombat.cache.NullCache;
import org.ambraproject.wombat.config.yaml.IgnoreMissingPropertyConstructor;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.ambraproject.wombat.service.remote.ArticleApiImpl;
import org.ambraproject.wombat.service.remote.CachedRemoteService;
import org.ambraproject.wombat.service.remote.JsonService;
import org.ambraproject.wombat.service.remote.ReaderService;
import org.ambraproject.wombat.service.remote.SolrSearchApi;
import org.ambraproject.wombat.service.remote.SolrSearchApiImpl;
import org.ambraproject.wombat.service.remote.StreamService;
import org.ambraproject.wombat.service.remote.UserApi;
import org.ambraproject.wombat.service.remote.UserApiImpl;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

@Configuration
public class RootConfiguration {

  @Value("${rootConfiguration.ignoreMissingProperty:false}")
  private boolean ignoreMissingProperty;

  @Bean
  public Yaml yaml() {
    final Yaml yaml;
    if (ignoreMissingProperty) {
      final Constructor contructor = new IgnoreMissingPropertyConstructor();
      yaml = new Yaml(contructor);
    } else {
      yaml = new Yaml();
    }
    return yaml;
  }

  private static final String CONFIG_DIR_PROPERTY_NAME = "wombat.configDir";

  private static File getConfigDirectory() {
    String property = System.getProperty(CONFIG_DIR_PROPERTY_NAME);
    if (!Strings.isNullOrEmpty(property)) {
      return new File(property);
    } else {
      throw new RuntimeException("Config directory not found. " + CONFIG_DIR_PROPERTY_NAME + " must be defined.");
    }
  }

  @Bean
  public RuntimeConfiguration runtimeConfiguration(Yaml yaml)
      throws IOException {
    File configDirectory = getConfigDirectory();
    File configPath = new File(configDirectory, "wombat.yaml");
    if (!configPath.exists()) {
      throw new RuntimeConfigurationException(configPath.getPath() + " not found");
    }

    YamlConfiguration runtimeConfiguration;
    try (Reader reader = new BufferedReader(new FileReader(configPath))) {
      runtimeConfiguration = new YamlConfiguration(yaml.loadAs(reader, YamlConfiguration.ConfigurationInput.class));
    } catch (JsonSyntaxException e) {
      throw new RuntimeConfigurationException(configPath + " contains invalid JSON", e);
    }
    runtimeConfiguration.validate();
    return runtimeConfiguration;
  }

  @Bean
  public ArticleApi articleApi() {
    return new ArticleApiImpl();
  }

  @Bean
  public Gson gson() {
    return RuntimeConfiguration.makeGson();
  }

  @Bean
  public HttpClientConnectionManager httpClientConnectionManager(RuntimeConfiguration runtimeConfiguration) {
    PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();

    final RuntimeConfiguration.HttpConnectionPoolConfiguration httpConnectionPoolConfiguration = runtimeConfiguration.getHttpConnectionPoolConfiguration();
    Integer maxTotal = httpConnectionPoolConfiguration.getMaxTotal();
    if (maxTotal != null) manager.setMaxTotal(maxTotal);
    Integer defaultMaxPerRoute = httpConnectionPoolConfiguration.getDefaultMaxPerRoute();
    if (defaultMaxPerRoute != null) manager.setDefaultMaxPerRoute(defaultMaxPerRoute);

    return manager;
  }

  @Bean
  public Cache cache(RuntimeConfiguration runtimeConfiguration) throws IOException {
    final RuntimeConfiguration.CacheConfiguration cacheConfiguration = runtimeConfiguration.getCacheConfiguration();
    if (!Strings.isNullOrEmpty(cacheConfiguration.getMemcachedHost())) {

      // TODO: consider defining this in wombat.yaml instead.
      final int cacheTimeout = 60 * 60;
      MemcacheClient result = new MemcacheClient(cacheConfiguration.getMemcachedHost(),
          cacheConfiguration.getMemcachedPort(), cacheConfiguration.getCacheAppPrefix(), cacheTimeout);
      result.connect();
      return result;
    } else {
      return new NullCache();
    }
  }

  @Bean
  public SolrSearchApi searchService() {
    return new SolrSearchApiImpl();
  }

  @Bean
  public UserApi userApi() {
    return new UserApiImpl();
  }

  @Bean
  public JsonService jsonService() {
    return new JsonService();
  }

  @Bean
  public CachedRemoteService<InputStream> cachedRemoteStreamer(HttpClientConnectionManager httpClientConnectionManager,
                                                               Cache cache) {
    return new CachedRemoteService<>(new StreamService(httpClientConnectionManager), cache);
  }

  @Bean
  public CachedRemoteService<Reader> cachedRemoteReader(HttpClientConnectionManager httpClientConnectionManager,
                                                        Cache cache) {
    return new CachedRemoteService<>(new ReaderService(httpClientConnectionManager), cache);
  }
}
