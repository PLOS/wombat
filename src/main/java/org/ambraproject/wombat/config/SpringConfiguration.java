package org.ambraproject.wombat.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Closeables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.ambraproject.wombat.service.ArticleService;
import org.ambraproject.wombat.service.ArticleServiceImpl;
import org.ambraproject.wombat.service.ArticleTransformService;
import org.ambraproject.wombat.service.ArticleTransformServiceImpl;
import org.ambraproject.wombat.service.SoaService;
import org.ambraproject.wombat.service.SoaServiceImpl;
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

@Configuration
public class SpringConfiguration {

  @Bean
  public Gson gson() {
    GsonBuilder builder = new GsonBuilder();
    builder.setPrettyPrinting();
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
  public ImmutableMap<String, Theme> themesForJournals(RuntimeConfiguration runtimeConfiguration,
                                                       ThemeTree themeTree) {
    return runtimeConfiguration.getThemesForJournals(themeTree);
  }

  @Bean
  public FreeMarkerConfig freeMarkerConfig(ImmutableMap<String, Theme> themesForJournals) throws IOException {
    JournalTemplateLoader loader = new JournalTemplateLoader(themesForJournals);
    FreeMarkerConfigurer config = new FreeMarkerConfigurer();
    config.setPreTemplateLoaders(loader);
    return config;
  }

  @Bean
  public FreeMarkerViewResolver viewResolver() {
    FreeMarkerViewResolver resolver = new FreeMarkerViewResolver();
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
  public ArticleService articleService() {
    return new ArticleServiceImpl();
  }
}
