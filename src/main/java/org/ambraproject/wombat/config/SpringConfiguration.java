package org.ambraproject.wombat.config;

import com.google.common.io.Closeables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.ambraproject.wombat.service.SoaService;
import org.ambraproject.wombat.service.SoaServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
  public SoaConfiguration soaConfiguration(Gson gson) throws IOException {
    final File configPath = new File("/etc/ambra/soa.json");
    if (!configPath.exists()) {
      throw new ConfigurationException(configPath.getPath() + " not found");
    }

    SoaConfiguration soaConfiguration;
    Reader reader = null;
    boolean threw = true;
    try {
      reader = new BufferedReader(new FileReader(configPath));
      soaConfiguration = gson.fromJson(reader, SoaConfiguration.class);
      threw = false;
    } catch (JsonSyntaxException e) {
      throw new ConfigurationException(configPath + " contains invalid JSON");
    } finally {
      Closeables.close(reader, threw);
    }
    soaConfiguration.validate();
    return soaConfiguration;
  }

  @Bean
  public SoaService soaService() {
    return new SoaServiceImpl();
  }

}
