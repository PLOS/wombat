/*
 * Copyright (c) 2006-2013 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
public class JsonConfiguration implements RuntimeConfiguration {

  @Autowired
  private SoaService soaService;

  @Autowired
  private SearchService searchService;

  private final UserFields uf;

  public JsonConfiguration(UserFields uf) {
    this.uf = uf;
  }

  public static class UserFields {
    /**
     * @deprecated should only be called reflectively by a deserialization library
     */
    @Deprecated
    public UserFields() {
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

    public void setServer(String server) {
      this.server = server;
    }

    public void setSolrServer(String solrServer) {
      this.solrServer = solrServer;
    }

    public void setMemcachedHost(String memcachedHost) {
      this.memcachedHost = memcachedHost;
    }

    public void setMemcachedPort(Integer memcachedPort) {
      this.memcachedPort = memcachedPort;
    }

    public void setCacheAppPrefix(String cacheAppPrefix) {
      this.cacheAppPrefix = cacheAppPrefix;
    }

    public void setTrustUnsignedServer(Boolean trustUnsignedServer) {
      this.trustUnsignedServer = trustUnsignedServer;
    }

    public void setDevModeAssets(Boolean devModeAssets) {
      this.devModeAssets = devModeAssets;
    }

    public void setCompiledAssetDir(String compiledAssetDir) {
      this.compiledAssetDir = compiledAssetDir;
    }

    public void setThemes(List<Map<String, ?>> themes) {
      this.themes = themes;
    }

    public void setSites(List<Map<String, ?>> sites) {
      this.sites = sites;
    }

    public void setHomePageHooks(Map<String, Class<? extends ControllerHook>> homePageHooks) {
      this.homePageHooks = homePageHooks;
    }
  }

  /**
   * Validate values after deserializing.
   *
   * @throws RuntimeConfigurationException if a value is invalid
   */
  public void validate() {
    if (Strings.isNullOrEmpty(uf.server)) {
      throw new RuntimeConfigurationException("Server address required");
    }
    try {
      new URL(uf.server);
    } catch (MalformedURLException e) {
      throw new RuntimeConfigurationException("Provided server address is not a valid URL", e);
    }
    if (!Strings.isNullOrEmpty(uf.solrServer)) {
      try {
        new URL(uf.solrServer);
      } catch (MalformedURLException e) {
        throw new RuntimeConfigurationException("Provided solr server address is not a valid URL", e);
      }
    }
    Map<String, Class<? extends ControllerHook>> temp = new HashMap<>();
    for (Map<String, ?> siteMap : uf.sites) {
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
    uf.homePageHooks = ImmutableMap.copyOf(temp);
    if (!Strings.isNullOrEmpty(uf.memcachedHost) && uf.memcachedPort == null) {
      throw new RuntimeConfigurationException("No memcachedPort specified");
    }
    if (!Strings.isNullOrEmpty(uf.memcachedHost) && Strings.isNullOrEmpty(uf.cacheAppPrefix)) {
      throw new RuntimeConfigurationException("If memcachedHost is specified, cacheAppPrefix must be as well");
    }
    if ((uf.devModeAssets == null || !uf.devModeAssets) && Strings.isNullOrEmpty(uf.compiledAssetDir)) {
      throw new RuntimeConfigurationException("If devModeAssets is false, compiledAssetDir must be specified");
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean trustUnsignedServer() {
    return (uf.trustUnsignedServer == null) ? false : uf.trustUnsignedServer;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean devModeAssets() {
    return (uf.devModeAssets == null) ? false : uf.devModeAssets;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getCompiledAssetDir() {
    return uf.compiledAssetDir;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getMemcachedHost() {
    return uf.memcachedHost;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getMemcachedPort() {
    return uf.memcachedPort == null ? -1 : uf.memcachedPort;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getCacheAppPrefix() {
    return uf.cacheAppPrefix;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public URL getServer() {
    try {
      return new URL(uf.server);
    } catch (MalformedURLException e) {
      throw new IllegalStateException("Invalid URL should have been caught at validation", e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public URL getSolrServer() {
    String server = uf.solrServer;
    if (Strings.isNullOrEmpty(server)) {
      server = "http://localhost:8983/solr/select/";
    }
    try {
      return new URL(server);
    } catch (MalformedURLException e) {
      throw new IllegalStateException("Invalid URL should have been caught at validation", e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ThemeTree getThemes(Theme internalDefault) throws ThemeTree.ThemeConfigurationException {
    return ThemeTree.parse(uf.themes, internalDefault);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ImmutableMap<String, Theme> getThemesForSites(ThemeTree themeTree) {
    return themeTree.matchToSites(uf.sites);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ControllerHook getHomePageHook(String site) {
    Class<? extends ControllerHook> klass = uf.homePageHooks.get(site);
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
