package org.ambraproject.wombat.config;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.ambraproject.rhombat.cache.Cache;
import org.ambraproject.rhombat.cache.MemcacheClient;
import org.ambraproject.rhombat.cache.NullCache;
import org.ambraproject.rhombat.gson.Iso8601DateAdapter;
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
import org.ambraproject.wombat.util.JodaTimeLocalDateAdapter;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.context.ApplicationContext;
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

  private static final String CONFIG_DIR_PROPERTY_NAME = "wombat.configDir";
  private static final String CONFIG_DIR_ENVIRONMENT_NAME = "WOMBAT_CONFIG_DIR";

  private static File getConfigDirectory(ApplicationContext applicationContext) {
    String property = System.getProperty(CONFIG_DIR_PROPERTY_NAME);
    if (!Strings.isNullOrEmpty(property)) {
      return new File(property);
    }

    String environmentVar = System.getenv(CONFIG_DIR_ENVIRONMENT_NAME);
    if (!Strings.isNullOrEmpty(environmentVar)) {
      return new File(environmentVar);
    }

    String applicationName = applicationContext.getApplicationName();
    if (!Strings.isNullOrEmpty(applicationName)) {
      return new File("/etc", applicationName);
    }

    throw new RuntimeException("Config directory not found. " +
        "(If application name is empty, " + CONFIG_DIR_PROPERTY_NAME + " or "
        + CONFIG_DIR_ENVIRONMENT_NAME + " must be defined.)");
  }

  @Bean
  public RuntimeConfiguration runtimeConfiguration(ApplicationContext applicationContext,
                                                   Yaml yaml)
      throws IOException {
    File configDirectory = getConfigDirectory(applicationContext);
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
    GsonBuilder builder = new GsonBuilder();
    builder.setPrettyPrinting();
    builder.registerTypeAdapter(Date.class, new Iso8601DateAdapter());
    builder.registerTypeAdapter(org.joda.time.LocalDate.class, JodaTimeLocalDateAdapter.INSTANCE);
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
