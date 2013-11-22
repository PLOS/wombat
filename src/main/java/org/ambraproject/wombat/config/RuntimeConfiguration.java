package org.ambraproject.wombat.config;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.gson.GsonBuilder;
import org.ambraproject.wombat.controller.ControllerHook;
import org.ambraproject.wombat.service.SearchService;
import org.ambraproject.wombat.service.SoaService;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
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

  @Autowired
  private SoaService soaService;

  @Autowired
  private SearchService searchService;

  /**
   * @deprecated should only be called reflectively by Gson
   */
  @Deprecated
  public RuntimeConfiguration() {
  }

  // Fields are immutable by convention. They should be modified only during deserialization.
  private String server;
  private String solrServer;
  private String memcachedHost;
  private Integer memcachedPort;
  private String cacheAppPrefix;
  private Boolean trustUnsignedServer;
  private Boolean devModeAssets;
  private String compiledAssetDir;
  private List<Map<String, ?>> themes;
  private List<Map<String, ?>> sites;
  private Map<String, Class<? extends ControllerHook>> homePageHooks;

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
    if (!Strings.isNullOrEmpty(solrServer)) {
      try {
        new URL(solrServer);
      } catch (MalformedURLException e) {
        throw new RuntimeConfigurationException("Provided solr server address is not a valid URL", e);
      }
    }
    Map<String, Class<? extends ControllerHook>> temp = new HashMap<>();
    for (Map<String, ?> siteMap : sites) {
      String site = (String) siteMap.get("key");
      String className = (String) siteMap.get("homePageHook");
      if (className != null) {
        Class<? extends ControllerHook> klass = null;
        try {
          klass = (Class<? extends ControllerHook>) Class.forName(className);
        } catch (ClassCastException cce) {
          throw new RuntimeConfigurationException(String.format(
              "homePageHook %s for site %s does not extend ControllerHook", klass.getCanonicalName(), site));
        } catch (ClassNotFoundException cnfe) {
          throw new RuntimeConfigurationException(String.format(
              "Could not load class %s for homePageHook for site %s", className, site));
        }
        temp.put(site, klass);
      }
    }
    homePageHooks = ImmutableMap.copyOf(temp);
    if (!Strings.isNullOrEmpty(memcachedHost) && memcachedPort == null) {
      throw new RuntimeConfigurationException("No memcachedPort specified");
    }
    if (!Strings.isNullOrEmpty(memcachedHost) && Strings.isNullOrEmpty(cacheAppPrefix)) {
      throw new RuntimeConfigurationException("If memcachedHost is specified, cacheAppPrefix must be as well");
    }
    if ((devModeAssets == null || !devModeAssets) && Strings.isNullOrEmpty(compiledAssetDir)) {
      throw new RuntimeConfigurationException("If devModeAssets is false, compiledAssetDir must be specified");
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
   * @return true if we are running in "dev mode" for .js and .css, and compilation/minification
   *     should not happen for these files
   */
  public boolean devModeAssets() {
    return (devModeAssets == null) ? false : devModeAssets;
  }

  /**
   * @return the directory in which to write and serve compiled assets (.js and .css).  Not relevant
   *     if devModeAssets is true.
   */
  public String getCompiledAssetDir() {
    return compiledAssetDir;
  }

  /**
   * @return the memcached host, or null if it is not present in the config
   */
  public String getMemcachedHost() {
    return memcachedHost;
  }

  /**
   * @return the memcached port, or -1 if it is not present in the config
   */
  public int getMemcachedPort() {
    return memcachedPort == null ? -1 : memcachedPort;
  }

  /**
   * @return the cacheAppPrefix value, or null if it is not defined in the config.  This should
   *     be a String that is shared by all wombat app servers, defining a namespace for them.
   */
  public String getCacheAppPrefix() {
    return cacheAppPrefix;
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

  /**
   * Get the URL of the solr search server.
   *
   * @return the URL
   */
  public URL getSolrServer() {
    String server = solrServer;
    if (Strings.isNullOrEmpty(server)) {
      server = "http://localhost:8983/solr/select/";
    }
    try {
      return new URL(server);
    } catch (MalformedURLException e) {
      throw new IllegalStateException("Invalid URL should have been caught at validation", e);
    }
  }

  public ThemeTree getThemes(Theme internalDefault) throws ThemeTree.ThemeConfigurationException {
    return ThemeTree.parse(this.themes, internalDefault);
  }

  public ImmutableMap<String, Theme> getThemesForSites(ThemeTree themeTree) {
    return themeTree.matchToSites(sites);
  }

  /**
   * Returns the {@link ControllerHook} that adds additional model data needed to render the
   * home page for a given site, or null if one is not needed.
   *
   * @param site string identifying the site
   * @return ControllerHook instance, or null
   */
  public ControllerHook getHomePageHook(String site) {
    Class<? extends ControllerHook> klass = homePageHooks.get(site);
    if (klass == null) {
      return null; // No special hook is specified for this site
    }

    ControllerHook result;
    try {
      result = klass.newInstance();
    } catch (InstantiationException ie) {
      throw new RuntimeConfigurationException("Cound not instantiate " + klass.getName(), ie);
    } catch (IllegalAccessException iae) {
      throw new RuntimeConfigurationException("Cound not instantiate " + klass.getName(), iae);
    }

    // Since we create this ControllerHook here, through reflection, we can't use spring
    // to autowire any of its fields.  Instead, they have to be injected here.
    result.setSoaService(soaService);
    result.setSearchService(searchService);
    return result;
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
