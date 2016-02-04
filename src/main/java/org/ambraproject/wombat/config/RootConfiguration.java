package org.ambraproject.wombat.config;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.ambraproject.rhombat.cache.Cache;
import org.ambraproject.rhombat.cache.MemcacheClient;
import org.ambraproject.rhombat.cache.NullCache;
import org.ambraproject.rhombat.gson.Iso8601DateAdapter;
import org.ambraproject.wombat.service.remote.CachedRemoteService;
import org.ambraproject.wombat.service.remote.JsonService;
import org.ambraproject.wombat.service.remote.NedService;
import org.ambraproject.wombat.service.remote.NedServiceImpl;
import org.ambraproject.wombat.service.remote.ReaderService;
import org.ambraproject.wombat.service.remote.SolrSearchService;
import org.ambraproject.wombat.service.remote.SoaService;
import org.ambraproject.wombat.service.remote.SoaServiceImpl;
import org.ambraproject.wombat.service.remote.SolrSearchServiceImpl;
import org.ambraproject.wombat.service.remote.StreamService;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
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
  public SolrSearchService searchService() {
    return new SolrSearchServiceImpl();
  }

  @Bean
  public NedService nedService() {
    return new NedServiceImpl();
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
