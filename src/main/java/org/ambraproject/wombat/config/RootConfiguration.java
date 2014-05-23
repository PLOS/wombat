package org.ambraproject.wombat.config;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.ambraproject.rhombat.cache.Cache;
import org.ambraproject.rhombat.cache.MemcacheClient;
import org.ambraproject.rhombat.cache.NullCache;
import org.ambraproject.rhombat.gson.Iso8601DateAdapter;
import org.ambraproject.wombat.service.SearchService;
import org.ambraproject.wombat.service.SoaService;
import org.ambraproject.wombat.service.SoaServiceImpl;
import org.ambraproject.wombat.service.SolrSearchService;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Date;

@Configuration
public class RootConfiguration {

  @Bean
  public Yaml yaml() {
    return new Yaml();
  }

  @Bean
  public RuntimeConfiguration runtimeConfiguration(Yaml yaml) throws IOException {
    final File configPath = new File("/etc/ambra/wombat.yaml"); // TODO Descriptive file name
    if (!configPath.exists()) {
      throw new RuntimeConfigurationException(configPath.getPath() + " not found");
    }

    JsonConfiguration runtimeConfiguration;
    try (Reader reader = new BufferedReader(new FileReader(configPath))) {
      runtimeConfiguration = new JsonConfiguration(yaml.loadAs(reader, JsonConfiguration.UserFields.class));
    } catch (JsonSyntaxException e) {
      throw new RuntimeConfigurationException(configPath + " contains invalid JSON", e);
    }
    runtimeConfiguration.validate();
    return runtimeConfiguration;
  }

  @Bean
  public SoaService soaService() {
    return new SoaServiceImpl();
  }

  @Bean
  public Gson gson() {
    GsonBuilder builder = new GsonBuilder();
    builder.setPrettyPrinting();
    builder.registerTypeAdapter(Date.class, new Iso8601DateAdapter());
    return builder.create();
  }

  @Bean
  public HttpClientConnectionManager httpClientConnectionManager(RuntimeConfiguration runtimeConfiguration) {
    PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();

    Integer maxTotal = runtimeConfiguration.getConnectionPoolMaxTotal();
    if (maxTotal != null) manager.setMaxTotal(maxTotal);
    Integer defaultMaxPerRoute = runtimeConfiguration.getConnectionPoolDefaultMaxPerRoute();
    if (defaultMaxPerRoute != null) manager.setDefaultMaxPerRoute(defaultMaxPerRoute);

    return manager;
  }

  @Bean
  public Cache cache(RuntimeConfiguration runtimeConfiguration) throws IOException {
    if (!Strings.isNullOrEmpty(runtimeConfiguration.getMemcachedHost())) {

      // TODO: consider defining this in wombat.yaml instead.
      final int cacheTimeout = 60 * 60;
      MemcacheClient result = new MemcacheClient(runtimeConfiguration.getMemcachedHost(),
          runtimeConfiguration.getMemcachedPort(), runtimeConfiguration.getCacheAppPrefix(), cacheTimeout);
      result.connect();
      return result;
    } else {
      return new NullCache();
    }
  }

  @Bean
  public SearchService searchService() {
    return new SolrSearchService();
  }

}
