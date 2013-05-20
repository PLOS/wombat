package org.ambraproject.wombat.config;

import com.google.common.io.Closeables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.ambraproject.wombat.service.SoaService;
import org.ambraproject.wombat.service.SoaServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

@Configuration
public class SpringConfiguration {

  @Bean
  public Gson gson() {
    GsonBuilder builder = new GsonBuilder();
    builder.setPrettyPrinting();
    return builder.create();
  }

  @Bean
  public URL soa() throws IOException {
    File configPath = new File("/etc/ambra/soa_url.txt");
    if (!configPath.exists()) {
      throw new RuntimeException(configPath.getPath() + " not found");
    }

    URL soa = null;
    BufferedReader reader = null;
    boolean threw = true;
    try {
      reader = new BufferedReader(new FileReader(configPath));

      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty()) {
          continue;
        }
        if (soa != null) {
          throw new RuntimeException(configPath.getPath() + " contains more than one line of text");
        }
        try {
          soa = new URL(line);
        } catch (MalformedURLException e) {
          throw new RuntimeException(configPath.getPath() + " contains an invalid URL", e);
        }
      }
      if (soa == null) {
        throw new RuntimeException(configPath.getPath() + " is empty");
      }

      threw = false;
    } finally {
      Closeables.close(reader, threw);
    }

    return soa;
  }

  @Bean
  public SoaService soaService() {
    return new SoaServiceImpl();
  }

}
