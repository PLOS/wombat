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

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.gson.GsonBuilder;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.theme.Theme;
import org.ambraproject.wombat.config.theme.ThemeTree;
import org.ambraproject.wombat.service.remote.SearchService;
import org.ambraproject.wombat.service.remote.SoaService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
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

  private final ConfigurationInput input;

  public JsonConfiguration(ConfigurationInput input) {
    this.input = input;
  }

  private static URL buildUrl(String address, String defaultValue) {
    if (Strings.isNullOrEmpty(address)) {
      if (defaultValue == null) {
        return null;
      } else {
        address = defaultValue;
      }
    }
    try {
      return new URL(address);
    } catch (MalformedURLException e) {
      throw new IllegalStateException("Invalid URL should have been caught at validation", e);
    }
  }


  @Override
  public String getCompiledAssetDir() {
    return input.compiledAssetDir;
  }

  @Override
  public URL getServer() {
    return buildUrl(input.server, null);
  }

  @Override
  public URL getSolrServer() {
    return buildUrl(input.solrServer, "http://localhost:8983/solr/select/");
  }

  @Override
  public ThemeTree getThemes(Collection<? extends Theme> internalThemes, Theme rootTheme) throws ThemeTree.ThemeConfigurationException {
    return ThemeTree.parse(input.themes, internalThemes, rootTheme);
  }

  @Override
  public SiteSet getSites(ThemeTree themeTree) {
    return SiteSet.create(input.sites, themeTree);
  }

  private final CacheConfiguration cacheConfiguration = new CacheConfiguration() {
    @Override
    public String getMemcachedHost() {
      return (input.cache == null) ? null : input.cache.memcachedHost;
    }

    @Override
    public int getMemcachedPort() {
      return (input.cache == null || input.cache.memcachedPort == null) ? -1
          : input.cache.memcachedPort;
    }

    @Override
    public String getCacheAppPrefix() {
      return (input.cache == null) ? null : input.cache.cacheAppPrefix;
    }
  };

  @Override
  public CacheConfiguration getCacheConfiguration() {
    return cacheConfiguration;
  }

  private final HttpConnectionPoolConfiguration httpConnectionPoolConfiguration = new HttpConnectionPoolConfiguration() {
    @Override
    public Integer getMaxTotal() {
      return (input.httpConnectionPool == null) ? null : input.httpConnectionPool.maxTotal;
    }

    @Override
    public Integer getDefaultMaxPerRoute() {
      return (input.httpConnectionPool == null) ? null : input.httpConnectionPool.defaultMaxPerRoute;
    }
  };

  @Override
  public HttpConnectionPoolConfiguration getHttpConnectionPoolConfiguration() {
    return httpConnectionPoolConfiguration;
  }

  private final CasConfiguration casConfiguration = new CasConfiguration() {
    @Override
    public String getCasUrl() {
      return (input.cas == null) ? null : input.cas.casUrl;
    }

    @Override
    public String getServiceUrl() {
      return (input.cas == null) ? null : input.cas.serviceUrl;
    }

    @Override
    public String getLoginUrl() {
      return (input.cas == null) ? null : input.cas.loginUrl;
    }

    @Override
    public String getLogoutUrl() {
      if (input.cas == null || Strings.isNullOrEmpty(input.cas.logoutUrl)) {
        return null;
      }

      if (Strings.isNullOrEmpty(input.cas.logoutServiceUrl)) {
        return input.cas.logoutUrl;
      } else {
        try {
          return input.cas.logoutUrl + "?service=" + URLEncoder.encode(input.cas.logoutServiceUrl, Charsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
          throw new RuntimeException(e);
        }
      }
    }

    @Override
    public String getLogoutServiceUrl() {
      return (input.cas == null) ? null : input.cas.logoutServiceUrl;
    }
  };

  @Override
  public CasConfiguration getCasConfiguration() {
    return casConfiguration;
  }


  /**
   * Validate values after deserializing.
   *
   * @throws RuntimeConfigurationException if a value is invalid
   */
  public void validate() {
    if (Strings.isNullOrEmpty(input.server)) {
      throw new RuntimeConfigurationException("Server address required");
    }
    try {
      new URL(input.server);
    } catch (MalformedURLException e) {
      throw new RuntimeConfigurationException("Provided server address is not a valid URL", e);
    }
    if (!Strings.isNullOrEmpty(input.solrServer)) {
      try {
        new URL(input.solrServer);
      } catch (MalformedURLException e) {
        throw new RuntimeConfigurationException("Provided solr server address is not a valid URL", e);
      }
    }
    if (input.cache != null) {
      if (!Strings.isNullOrEmpty(input.cache.memcachedHost) && input.cache.memcachedPort == null) {
        throw new RuntimeConfigurationException("No memcachedPort specified");
      }
      if (!Strings.isNullOrEmpty(input.cache.memcachedHost) && Strings.isNullOrEmpty(input.cache.cacheAppPrefix)) {
        throw new RuntimeConfigurationException("If memcachedHost is specified, cacheAppPrefix must be as well");
      }
    }
  }


  public static class ConfigurationInput {
    /**
     * @deprecated should only be called reflectively by a deserialization library
     */
    @Deprecated
    public ConfigurationInput() {
    }

    /*
     * For debugger-friendliness only. If there is a need to serialize back to JSON in production, it would be more
     * efficient to use the Gson bean.
     */
    @Override
    public String toString() {
      return new GsonBuilder().create().toJson(this);
    }


    // Input-defining fields appear below.
    // SnakeYAML will reflectively inspect the names of these fields and use them as the input contract.
    // All such fields are immutable by convention. They should be set only by the YAML deserializer.

    // ---------------- Input fields (and boring boilerplate setters) are below this line ----------------

    private String server;
    private String solrServer;
    private String compiledAssetDir;
    private List<Map<String, ?>> themes;
    private List<Map<String, ?>> sites;

    private CacheConfigurationInput cache;
    private HttpConnectionPoolConfigurationInput httpConnectionPool;
    private CasConfigurationInput cas;

    /**
     * @deprecated For access by reflective deserializer only
     */
    @Deprecated
    public void setServer(String server) {
      this.server = server;
    }

    /**
     * @deprecated For access by reflective deserializer only
     */
    @Deprecated
    public void setSolrServer(String solrServer) {
      this.solrServer = solrServer;
    }

    /**
     * @deprecated For access by reflective deserializer only
     */
    @Deprecated
    public void setCompiledAssetDir(String compiledAssetDir) {
      this.compiledAssetDir = compiledAssetDir;
    }

    /**
     * @deprecated For access by reflective deserializer only
     */
    @Deprecated
    public void setThemes(List<Map<String, ?>> themes) {
      this.themes = themes;
    }

    /**
     * @deprecated For access by reflective deserializer only
     */
    @Deprecated
    public void setSites(List<Map<String, ?>> sites) {
      this.sites = sites;
    }

    /**
     * @deprecated For access by reflective deserializer only
     */
    @Deprecated
    public void setCache(CacheConfigurationInput cache) {
      this.cache = cache;
    }

    /**
     * @deprecated For access by reflective deserializer only
     */
    @Deprecated
    public void setHttpConnectionPool(HttpConnectionPoolConfigurationInput httpConnectionPool) {
      this.httpConnectionPool = httpConnectionPool;
    }

    /**
     * @deprecated For access by reflective deserializer only
     */
    @Deprecated
    public void setCas(CasConfigurationInput cas) {
      this.cas = cas;
    }
  }

  public static class CacheConfigurationInput {
    private String memcachedHost;
    private Integer memcachedPort;
    private String cacheAppPrefix;

    /**
     * @deprecated For access by reflective deserializer only
     */
    @Deprecated
    public void setMemcachedHost(String memcachedHost) {
      this.memcachedHost = memcachedHost;
    }

    /**
     * @deprecated For access by reflective deserializer only
     */
    @Deprecated
    public void setMemcachedPort(Integer memcachedPort) {
      this.memcachedPort = memcachedPort;
    }

    /**
     * @deprecated For access by reflective deserializer only
     */
    @Deprecated
    public void setCacheAppPrefix(String cacheAppPrefix) {
      this.cacheAppPrefix = cacheAppPrefix;
    }
  }

  public static class HttpConnectionPoolConfigurationInput {
    private Integer maxTotal;
    private Integer defaultMaxPerRoute;

    /**
     * @deprecated For access by reflective deserializer only
     */
    @Deprecated
    public void setMaxTotal(Integer maxTotal) {
      this.maxTotal = maxTotal;
    }

    /**
     * @deprecated For access by reflective deserializer only
     */
    @Deprecated
    public void setDefaultMaxPerRoute(Integer defaultMaxPerRoute) {
      this.defaultMaxPerRoute = defaultMaxPerRoute;
    }
  }

  public static class CasConfigurationInput {
    private String casUrl;
    private String serviceUrl;
    private String loginUrl;
    private String logoutUrl;
    private String logoutServiceUrl;

    /**
     * @deprecated For access by reflective deserializer only
     */
    @Deprecated
    public void setCasUrl(String casUrl) {
      this.casUrl = casUrl;
    }

    /**
     * @deprecated For access by reflective deserializer only
     */
    @Deprecated
    public void setServiceUrl(String serviceUrl) {
      this.serviceUrl = serviceUrl;
    }

    /**
     * @deprecated For access by reflective deserializer only
     */
    @Deprecated
    public void setLoginUrl(String loginUrl) {
      this.loginUrl = loginUrl;
    }

    /**
     * @deprecated For access by reflective deserializer only
     */
    @Deprecated
    public void setLogoutUrl(String logoutUrl) {
      this.logoutUrl = logoutUrl;
    }

    /**
     * @deprecated For access by reflective deserializer only
     */
    @Deprecated
    public void setLogoutServiceUrl(String logoutServiceUrl) {
      this.logoutServiceUrl = logoutServiceUrl;
    }
  }
}
