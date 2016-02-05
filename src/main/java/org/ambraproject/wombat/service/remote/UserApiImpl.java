package org.ambraproject.wombat.service.remote;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apache.xerces.impl.dv.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;

public class UserApiImpl extends AbstractRestfulJsonApi implements UserApi {

  @Autowired
  private ArticleApi articleApi;

  // Configuration data for sending requests to NED.
  // Lazily initialized. We can't create it until articleApi is wired.
  private static class NedConfiguration {
    private final URL server;
    private final ImmutableCollection<BasicHeader> authorizationHeader;

    private NedConfiguration(String server, String authorizationAppName, String authorizationPassword) {
      try {
        this.server = new URL(Objects.requireNonNull(server));
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }

      this.authorizationHeader = ((authorizationAppName != null) && (authorizationPassword != null))
          ? ImmutableList.of(createAuthorizationHeader(authorizationAppName, authorizationPassword))
          : ImmutableList.of();
    }
  }

  // There is probably a library for this. TODO
  private static BasicHeader createAuthorizationHeader(String authorizationAppName, String authorizationPassword) {
    String authorization = authorizationAppName + ":" + authorizationPassword;
    String encoded = Base64.encode(authorization.getBytes(Charsets.ISO_8859_1));
    return new BasicHeader("Authorization", "Basic " + encoded);
  }

  private NedConfiguration fetchNedConfiguration() {
    Map<String, ?> nedConfigurationData;
    try {
      nedConfigurationData = articleApi.requestObject("config/ned", Map.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return new NedConfiguration((String) nedConfigurationData.get("server"),
        (String) nedConfigurationData.get("authorizationAppName"),
        (String) nedConfigurationData.get("authorizationPassword"));
  }

  private transient NedConfiguration nedConfiguration;

  private NedConfiguration getNedConfiguration() {
    return (nedConfiguration != null) ? nedConfiguration : (nedConfiguration = fetchNedConfiguration());
  }

  @Override
  protected Iterable<? extends Header> getAdditionalHeaders() {
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
