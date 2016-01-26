package org.ambraproject.wombat.service.remote;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class NedServiceImpl extends AbstractRestfulJsonService implements NedService {

  @Autowired
  private SoaService soaService;

  private static class NedConfiguration {
    private final URL server;
    private final String authorizationAppName;
    private final String authorizationPassword;

    private NedConfiguration(Map<String, Object> nedConfigurationData) {
      try {
        this.server = new URL(nedConfigurationData.get("server").toString());
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
      this.authorizationAppName = nedConfigurationData.get("authorizationAppName").toString();
      this.authorizationPassword = nedConfigurationData.get("authorizationPassword").toString();
    }
  }

  private NedConfiguration fetchNedConfiguration() {
    try {
      return new NedConfiguration(soaService.requestObject("config/ned", Map.class));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private transient NedConfiguration nedConfiguration;

  private NedConfiguration getNedConfiguration() {
    return (nedConfiguration != null) ? nedConfiguration : (nedConfiguration = fetchNedConfiguration());
  }

  @Override
  protected URL getServerUrl() {
    NedConfiguration nedConfiguration1 = getNedConfiguration();
    return nedConfiguration1.server;
  }

  @Override
  protected String getCachePrefix() {
    return "ned";
  }
}
