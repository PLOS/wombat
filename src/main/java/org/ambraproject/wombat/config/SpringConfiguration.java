package org.ambraproject.wombat.config;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Closeables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.ambraproject.rhombat.cache.Cache;
import org.ambraproject.rhombat.cache.MemcacheClient;
import org.ambraproject.rhombat.cache.NullCache;
import org.ambraproject.rhombat.gson.Iso8601DateAdapter;
import org.ambraproject.wombat.service.ArticleTransformService;
import org.ambraproject.wombat.service.ArticleTransformServiceImpl;
import org.ambraproject.wombat.service.SearchService;
import org.ambraproject.wombat.service.SoaService;
import org.ambraproject.wombat.service.SoaServiceImpl;
import org.ambraproject.wombat.service.SolrSearchService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import javax.servlet.ServletContext;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Date;

@Configuration
public class SpringConfiguration {

  private static final int DEFAULT_CACHE_TIMEOUT = 60 * 60;

  @Bean
  public Gson gson() {
    GsonBuilder builder = new GsonBuilder();
    builder.setPrettyPrinting();
    builder.registerTypeAdapter(Date.class, new Iso8601DateAdapter());
    return builder.create();
  }

  @Bean
  public RuntimeConfiguration runtimeConfiguration(Gson gson) throws IOException {
    final File configPath = new File("/etc/ambra/wombat.json"); // TODO Descriptive file name
    if (!configPath.exists()) {
      throw new RuntimeConfigurationException(configPath.getPath() + " not found");
    }

    RuntimeConfiguration runtimeConfiguration;
    Reader reader = null;
    boolean threw = true;
    try {
      reader = new BufferedReader(new FileReader(configPath));
      runtimeConfiguration = gson.fromJson(reader, RuntimeConfiguration.class);
      threw = false;
    } catch (JsonSyntaxException e) {
      throw new RuntimeConfigurationException(configPath + " contains invalid JSON", e);
    } finally {
      Closeables.close(reader, threw);
    }
    runtimeConfiguration.validate();
    return runtimeConfiguration;
  }

  @Bean
  public ThemeTree themeTree(ServletContext servletContext, RuntimeConfiguration runtimeConfiguration)
      throws ThemeTree.ThemeConfigurationException {
    String internalViewPath = "/WEB-INF/views/";
    Theme internalDefaultTheme = new InternalTheme("", null, servletContext, internalViewPath);
    return runtimeConfiguration.getThemes(internalDefaultTheme);
  }

  @Bean
  public SiteSet siteSet(RuntimeConfiguration runtimeConfiguration,
                         ThemeTree themeTree) {
    return SiteSet.create(runtimeConfiguration.getThemesForSites(themeTree));
  }

  @Bean
  public FreeMarkerConfig freeMarkerConfig(SiteSet siteSet) throws IOException {
    SiteTemplateLoader loader = new SiteTemplateLoader(siteSet);
    FreeMarkerConfigurer config = new FreeMarkerConfigurer();
    config.setPreTemplateLoaders(loader);
    return config;
  }

  @Bean
  public Charset charset() {
    return Charsets.UTF_8;
  }

  @Bean
  public FreeMarkerViewResolver viewResolver(Charset charset) {
    FreeMarkerViewResolver resolver = new FreeMarkerViewResolver();
    resolver.setContentType("text/html;charset=" + charset);
    resolver.setCache(true);
    resolver.setPrefix("");
    resolver.setSuffix(".ftl");
    return resolver;
  }

  @Bean
  public SoaService soaService() {
    return new SoaServiceImpl();
  }

  @Bean
  public ArticleTransformService articleTransformService() {
    return new ArticleTransformServiceImpl();
  }

  @Bean
  public SearchService searchService() {
    return new SolrSearchService();
  }

  @Bean
  public Cache cache(RuntimeConfiguration runtimeConfiguration) throws IOException {
    if (!Strings.isNullOrEmpty(runtimeConfiguration.getMemcachedHost())) {
      MemcacheClient result = new MemcacheClient(runtimeConfiguration.getMemcachedHost(),
          runtimeConfiguration.getMemcachedPort(), runtimeConfiguration.getCacheAppPrefix(), DEFAULT_CACHE_TIMEOUT);
      result.connect();
      return result;
    } else {
      return new NullCache();
    }
  }
}
