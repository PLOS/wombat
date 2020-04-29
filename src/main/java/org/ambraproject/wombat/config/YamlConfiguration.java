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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.gson.GsonBuilder;

import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.theme.FilesystemThemeSource;
import org.ambraproject.wombat.config.theme.ThemeSource;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumSet;
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
  public ImmutableSet<String> getEnabledDevFeatures() {
    return ImmutableSet.copyOf(MoreObjects.firstNonNull(input.enableDevFeatures, ImmutableSet.<String>of()));
  }

  @Override
  public String getRootRedirect() {
    return input.rootRedirect;
  }

  /**
   * Future-proofing against the need for other ThemeSource types that may exist in the future (mainly, one that reads
   * from a remote source, probably by URL). Currently, the only supported type reads from the local filesystem.
   */
  private static enum ThemeSourceType {
    FILESYSTEM("filesystem") {
      @Override
      protected FilesystemThemeSource build(Map<String, ?> config) {
        String path = (String) config.get("path");
        if (path == null) throw new RuntimeException("Filesystem theme source must have path");
        return new FilesystemThemeSource(new File(path));
      }
    };

    private final String type;

    private ThemeSourceType(String type) {
      this.type = type;
    }

    private static final ImmutableMap<String, ThemeSourceType> BY_TYPE = Maps.uniqueIndex(
        EnumSet.allOf(ThemeSourceType.class), tst -> tst.type);

    protected abstract ThemeSource<?> build(Map<String, ?> config);
  }

  private static ThemeSource<?> parseThemeSource(Map<String, ?> map) {
    String typeStr = (String) map.get("type");
    if (typeStr == null) throw new RuntimeException("Theme source must have type");
    ThemeSourceType sourceType = ThemeSourceType.BY_TYPE.get(typeStr);
    if (sourceType == null) throw new RuntimeException("Unrecognized theme source type: " + typeStr);
    return sourceType.build(map);
  }

  @Override
  public ImmutableList<ThemeSource<?>> getThemeSources() {
    return input.themeSources.stream()
        .map(YamlConfiguration::parseThemeSource)
        .collect(ImmutableList.toImmutableList());
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

  private final SolrConfiguration solrConfiguration = new SolrConfiguration() {
    @Override
    public Optional<URL> getUrl() {
      return Optional.ofNullable(buildUrl(input.solr.url + getJournalsCollection() + "/select/", null));
    }

    @Override
    public Optional<URL> getUrl(Site site) {
      URL solrUrl;
      //todo: make type optional
      if (site.getType() != null && site.getType().equals("preprints")) {
         solrUrl = buildUrl(input.solr.url + getPreprintsCollection() + "/select/", null);
      } else {
        solrUrl = buildUrl(input.solr.url + getJournalsCollection() + "/select/", null);
      }
      return Optional.ofNullable(solrUrl);
    }

    @Override
    public String getJournalsCollection() {
      return input.solr.journalsCollection;
    }

    @Override
    public String getPreprintsCollection() {
      return input.solr.preprintsCollection;
    }
  };

  @Override
  public Optional<SolrConfiguration> getSolrConfiguration() {
    return (input.solr == null) ? Optional.empty() : Optional.of(solrConfiguration);
  }

  private final UserApiConfiguration userApiConfiguration = new UserApiConfiguration() {
    @Override
    public String getServerUrl() {
      return input.userApi.server;
    }

    @Override
    public String getAppName() {
      return input.userApi.authorizationAppName;
    }

    @Override
    public String getPassword() {
      return input.userApi.authorizationPassword;
    }
  };

  @Override
  public Optional<UserApiConfiguration> getUserApiConfiguration() {
    return (input.userApi == null) ? Optional.empty() : Optional.of(userApiConfiguration);
  }

  @Override
  public boolean areCommentsDisabled() {
    return (input.commentsDisabled != null) && input.commentsDisabled;
  }

  @Override
  public boolean showDebug() {
    if (input.showDebug == null) {
      return false;
    } else {
      return input.showDebug;
    }
  }

  @Override
  public String getCollectionsUrl() {
    if (input.collectionsUrl == null) {
      return "";
    } else {
      return input.collectionsUrl;
    }
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
    if (input.solr != null && !Strings.isNullOrEmpty(input.solr.url)) {
      try {
        new URL(input.solr.url);
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

    if (input.userApi == null) {
      throw new RuntimeConfigurationException("User API connection properties are required");
    }
    try {
      new URL(input.userApi.server);
    } catch (MalformedURLException e) {
      throw new RuntimeConfigurationException(
          "Provided User API server address is not a valid URL", e);
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
     *
     * ---------------- Input fields (and boring boilerplate setters) are below this line ----------------
     */

    private String server;
    private String compiledAssetDir;
    private String rootRedirect;
    private String collectionsUrl;
    private List<String> enableDevFeatures;
    private List<Map<String, ?>> themeSources;

    private CacheConfigurationInput cache;
    private HttpConnectionPoolConfigurationInput httpConnectionPool;
    private CasConfigurationInput cas;
    private SolrConfigurationInput solr;
    private UserApiConfigurationInput userApi;

    private Boolean commentsDisabled;
    private Boolean showDebug;

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
    public void setCompiledAssetDir(String compiledAssetDir) {
      this.compiledAssetDir = compiledAssetDir;
    }

    /**
     * @deprecated For access by reflective deserializer only
     */
    @Deprecated
    public void setRootRedirect(String rootRedirect) {
      this.rootRedirect = rootRedirect;
    }

    /**
     * @deprecated For access by reflective deserializer only
     */
    @Deprecated
    public void setShowDebug(boolean showDebug) {
      this.showDebug = showDebug;
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
    public void setThemeSources(List<Map<String, ?>> themeSources) {
      this.themeSources = themeSources;
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
    @Deprecated
    public void setSolr(SolrConfigurationInput solr) {
      this.solr = solr;
    }

    /**
     * @deprecated For access by reflective deserializer only
     */
    @Deprecated
    public void setUserApi(UserApiConfigurationInput userApi) {
      this.userApi = userApi;
    }

    /**
     * @deprecated For access by reflective deserializer only
     */
    public void setCommentsDisabled(Boolean commentsDisabled) {
      this.commentsDisabled = commentsDisabled;
    }

    /**
     * @deprecated For access by reflective deserializer only
     */
    public void setCollectionsUrl(String collectionsUrl) {
      this.collectionsUrl = collectionsUrl;
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

  public static class SolrConfigurationInput {
    private String url;
    private String journalsCollection;
    private String preprintsCollection;

    /**
     * @deprecated For access by reflective deserializer only
     */
    @Deprecated
    public void setUrl(String url) {
      this.url = url;
    }

    /**
     * @deprecated For access by reflective deserializer only
     */
    @Deprecated
    public void setJournalsCollection(String collection) {
      this.journalsCollection = collection;
    }

    /**
     * @deprecated For access by reflective deserializer only
     */
    @Deprecated
    public void setpreprintsCollection(String collection) {
      this.preprintsCollection = collection;
    }
  }

  public static class UserApiConfigurationInput {
    private String server;
    private String authorizationAppName;
    private String authorizationPassword;

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
    public void setAuthorizationAppName(String authorizationAppName) {
      this.authorizationAppName = authorizationAppName;
    }

    /**
     * @deprecated For access by reflective deserializer only
     */
    @Deprecated
    public void setAuthorizationPassword(String authorizationPassword) {
      this.authorizationPassword = authorizationPassword;
    }

  }
}
