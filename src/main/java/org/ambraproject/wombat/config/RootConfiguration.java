package org.ambraproject.wombat.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
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
  public ServiceCacheSet serviceCacheSet() throws IOException {
    return new CacheManagerWrapper();
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
  public CachedRemoteService<InputStream> cachedRemoteStreamer(HttpClientConnectionManager httpClientConnectionManager)
      throws IOException {
    return new CachedRemoteService<>(new StreamService(httpClientConnectionManager));
  }

  @Bean
  public CachedRemoteService<Reader> cachedRemoteReader(HttpClientConnectionManager httpClientConnectionManager)
      throws IOException {
    return new CachedRemoteService<>(new ReaderService(httpClientConnectionManager));
  }
}
