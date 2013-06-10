package org.ambraproject.wombat.config;

import com.google.common.io.Closeables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.ambraproject.wombat.service.SoaService;
import org.ambraproject.wombat.service.SoaServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

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
      throw new RuntimeConfigurationException(configPath + " contains invalid JSON");
    } finally {
      Closeables.close(reader, threw);
    }
    runtimeConfiguration.validate();
    return runtimeConfiguration;
  }

  @Bean
  public FreeMarkerConfig freeMarkerConfig() {
    FreeMarkerConfigurer config = new FreeMarkerConfigurer();
    config.setTemplateLoaderPath("/WEB-INF/views/");
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

}
