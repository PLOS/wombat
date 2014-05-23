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
import org.ambraproject.wombat.config.theme.Theme;
import org.ambraproject.wombat.config.theme.ThemeTree;
import org.ambraproject.wombat.service.SearchService;
import org.ambraproject.wombat.service.SoaService;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
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
    private Map<String, ?> httpConnectionPool;
    private String cacheAppPrefix;
    private Boolean devModeAssets;
    private String compiledAssetDir;
    private List<Map<String, ?>> themes;
    private List<Map<String, ?>> sites;
    private String casServiceUrl;
    private String casUrl;
    private String casLoginUrl;
    private String casLogoutUrl;

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

    public void setHttpConnectionPool(Map<String, ?> httpConnectionPool) {
      this.httpConnectionPool = httpConnectionPool;
    }

    public void setCacheAppPrefix(String cacheAppPrefix) {
      this.cacheAppPrefix = cacheAppPrefix;
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

    public void setCasServiceUrl(String casServiceUrl) {
      this.casServiceUrl = casServiceUrl;
    }

    public void setCasUrl(String casUrl) {
      this.casUrl = casUrl;
    }

    public void setCasLoginUrl(String casLoginUrl) {
      this.casLoginUrl = casLoginUrl;
    }

    public void setCasLogoutUrl(String casLogoutUrl) {
      this.casLogoutUrl = casLogoutUrl;
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

  @Override
  public Integer getConnectionPoolMaxTotal() {
    return uf.httpConnectionPool == null ? null : (Integer) uf.httpConnectionPool.get("maxTotal");
  }

  @Override
  public Integer getConnectionPoolDefaultMaxPerRoute() {
    return uf.httpConnectionPool == null ? null : (Integer) uf.httpConnectionPool.get("defaultMaxPerRoute");
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
  public ThemeTree getThemes(Collection<? extends Theme> internalThemes, Theme rootTheme)
      throws ThemeTree.ThemeConfigurationException {
    return ThemeTree.parse(uf.themes, internalThemes, rootTheme);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ImmutableMap<String, Theme> getThemesForSites(ThemeTree themeTree) {
    return themeTree.matchToSites(uf.sites);
  }

  @Override
  public String getCasServiceUrl() {
    return uf.casServiceUrl;
  }

  @Override
  public String getCasUrl() {
    return uf.casUrl;
  }

  @Override
  public String getCasLoginUrl() {
    return uf.casLoginUrl;
  }

  @Override
  public String getCasLogoutUrl() {
    return uf.casLogoutUrl;
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
