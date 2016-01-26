package org.ambraproject.wombat.service.remote;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
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

    private final ImmutableMultimap<String, String> authorizationHeader;

    private NedConfiguration(Map<String, Object> nedConfigurationData) {
      try {
        this.server = new URL(nedConfigurationData.get("server").toString());
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
      this.authorizationAppName = nedConfigurationData.get("authorizationAppName").toString();
      this.authorizationPassword = nedConfigurationData.get("authorizationPassword").toString();

      if (authorizationAppName != null && authorizationPassword != null) {
        String authorizationHeaderValue = ""; // TODO
        this.authorizationHeader = ImmutableMultimap.of("Authorization", authorizationHeaderValue);
      } else {
        this.authorizationHeader = ImmutableMultimap.of();
      }
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
  protected Multimap<String, String> getAdditionalHeaders() {
    return getNedConfiguration().authorizationHeader;
  }

  @Override
  protected URL getServerUrl() {
    return getNedConfiguration().server;
  }

  @Override
  protected String getCachePrefix() {
    return "ned";
  }
}
