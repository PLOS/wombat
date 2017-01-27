/*
 * Copyright (c) 2017 Public Library of Science
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package org.ambraproject.wombat.config;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.gson.GsonBuilder;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.theme.Theme;
import org.ambraproject.wombat.config.theme.ThemeTree;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Configuration for the webapp's runtime behavior as read from a YAML file.
 * <p/>
 * Because most configuration of application behavior should be behind the service layer, this class ought to be
 * concerned only with the minimal set of values that concern how this Spring app interacts with the service API.
 *
 * @see RootConfiguration#runtimeConfiguration
 */
public class YamlConfiguration implements RuntimeConfiguration {

  private final ConfigurationInput input;

  public YamlConfiguration(ConfigurationInput input) {
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
  public Optional<URL> getSolrServer() {
    return Optional.ofNullable(buildUrl(input.solrServer, null));
  }

  @Override
  public String getMailServer() {
    return input.mailServer;
  }

  @Override
  public ImmutableSet<String> getEnabledDevFeatures() {
    return ImmutableSet.copyOf(MoreObjects.firstNonNull(input.enableDevFeatures, ImmutableSet.<String>of()));
  }

  @Override
  public String getRootPagePath() {
    return input.rootPagePath;
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
      return input.cas.casUrl;
    }

    @Override
    public String getLoginUrl() {
      return input.cas.loginUrl;
    }

    @Override
    public String getLogoutUrl() {
      return input.cas.logoutUrl;
    }
  };

  @Override
  public Optional<CasConfiguration> getCasConfiguration() {
    return (input.cas == null) ? Optional.empty() : Optional.of(casConfiguration);
  }

  @Override
  public boolean areCommentsDisabled() {
    return (input.commentsDisabled != null) && input.commentsDisabled;
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

    /* Input-defining fields appear below.
     * SnakeYAML will reflectively inspect the names of these fields and use them as the input contract.
     * All such fields are immutable by convention. They should be set only by the YAML deserializer.
     * The intent of the @Deprecated annotation is to raise a warning in the IDE if a human refers
     * to them in code. (The reflective code in the library won't care at runtime of course.)

     * ---------------- Input fields (and boring boilerplate setters) are below this line ----------------
     */

    private String server;
    private String solrServer;
    private String mailServer;
    private String compiledAssetDir;
    private String rootPagePath;
    private List<String> enableDevFeatures;
    private List<Map<String, ?>> themes;
    private List<Map<String, ?>> sites;

    private CacheConfigurationInput cache;
    private HttpConnectionPoolConfigurationInput httpConnectionPool;
    private CasConfigurationInput cas;

    private Boolean commentsDisabled;

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
    public void setMailServer(String mailServer) {
      this.mailServer = mailServer;
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
    public void setRootPagePath(String rootPagePath) {
      this.rootPagePath = rootPagePath;
    }

    /**
     * @deprecated For access by reflective deserializer only
     */
    @Deprecated
    public void setEnableDevFeatures(List<String> enableDevFeatures) {
      this.enableDevFeatures = enableDevFeatures;
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

    /**
     * @deprecated For access by reflective deserializer only
     */
    public void setCommentsDisabled(Boolean commentsDisabled) {
      this.commentsDisabled = commentsDisabled;
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
    private String loginUrl;
    private String logoutUrl;

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

  }
}
