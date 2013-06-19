package org.ambraproject.wombat.config;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Configuration for the webapp's runtime behavior. Because most configuration of application behavior should be behind
 * the service layer, this class ought to be concerned only with the minimal set of values that concern how this Spring
 * app interacts with the service API.
 *
 * @see SpringConfiguration#runtimeConfiguration
 */
public class RuntimeConfiguration {

  /**
   * @deprecated should only be called reflectively by Gson
   */
  @Deprecated
  public RuntimeConfiguration() {
  }

  // Fields are immutable by convention. They should be modified only during deserialization.
  private String server;
  private Boolean trustUnsignedServer;
  private String themeBuildPath;
  private List<Map<String, ?>> themes;
  private List<Map<String, ?>> journals;

  /**
   * Validate values after deserializing.
   *
   * @throws RuntimeConfigurationException if a value is invalid
   */
  public void validate() {
    if (Strings.isNullOrEmpty(server)) {
      throw new RuntimeConfigurationException("Server address required");
    }
    try {
      new URL(server);
    } catch (MalformedURLException e) {
      throw new RuntimeConfigurationException("Provided server address is not a valid URL", e);
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

  public File getThemeBuildPath() {
    return (themeBuildPath == null) ? Files.createTempDir() : new File(themeBuildPath);
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

  public ThemeTree getThemes() throws ThemeTree.ThemeConfigurationException {
    return ThemeTree.parse(this.themes);
  }

  public ImmutableMap<String, Theme> getThemesForJournals(ThemeTree themeTree) {
    return themeTree.matchToJournals(journals);
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
