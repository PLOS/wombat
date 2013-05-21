package org.ambraproject.wombat.config;

import com.google.common.base.Strings;
import com.google.gson.GsonBuilder;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Configuration for how this webapp connects, as a client, to the SOA.
 *
 * @see SpringConfiguration#soaConfiguration
 */
public class SoaConfiguration {

  /**
   * @deprecated should only be called reflectively by Gson
   */
  @Deprecated
  public SoaConfiguration() {
  }

  // Fields are immutable by convention. They should be modified only during deserialization.
  private String server;
  private Boolean trustUnsignedServer;

  /**
   * Validate values after deserializing.
   *
   * @throws ConfigurationException if a value is invalid
   */
  public void validate() {
    if (Strings.isNullOrEmpty(server)) {
      throw new ConfigurationException("Server address required");
    }
    try {
      new URL(server);
    } catch (MalformedURLException e) {
      throw new ConfigurationException("Provided server address is not a valid URL", e);
    }
  }

  /**
   * Check whether this webapp is configured to naively trust servers without SSL certificates. This should be {@code
   * true} only when debugging or connecting to a private testing server. Defaults to {@code false}.
   *
   * @return {@code false} if default SSL authentication should be preserved
   */
  public boolean trustUnsignedServer() {
    return (trustUnsignedServer == null) ? false : trustUnsignedServer;
  }

  /**
   * Get the URL of the SOA server.
   *
   * @return the URL
   */
  public URL getServer() {
    try {
      return new URL(server);
    } catch (MalformedURLException e) {
      throw new IllegalStateException("Invalid URL should have been caught at validation", e);
    }
  }

  /*
   * For debugger-friendliness only. If there is a need to serialize back to JSON in production, it would be more
   * efficient to use the Gson bean.
   */
  @Override
  public String toString() {
    return new GsonBuilder().create().toJson(this);
  }

}
