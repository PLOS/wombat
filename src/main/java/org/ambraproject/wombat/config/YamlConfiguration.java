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
  public URL getRhinoServerUrl() {
    return buildUrl(input.rhinoServerUrl, null);
  }

  @Override
  public String getRootPagePath() {
    return input.rootPagePath;
  }

  @Override
  public String getEnvironment() {
    return input.environment;
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

  @Override
  public String getMemcachedHost() {
    return input.memcachedHost;
  }

  @Override
  public int getMemcachedPort() {
    return input.memcachedPort == null ? -1 : input.memcachedPort;
  }

  @Override
  public String getCasUrl() {
    return input.casUrl;
  }

  private final SolrConfiguration solrConfiguration = new SolrConfiguration() {
    @Override
    public Optional<URL> getUrl() {
      return Optional.ofNullable(buildUrl(input.solr.url + getJournalsCollection() + "/select/", null));
    }

    @Override
    public String getJournalsCollection() {
      return input.solr.journalsCollection;
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

  /**
   * Validate values after deserializing.
   *
   * @throws RuntimeConfigurationException if a value is invalid
   */
  public void validate() {
    if (Strings.isNullOrEmpty(input.rhinoServerUrl)) {
      throw new RuntimeConfigurationException("Server address required");
    }
    try {
      new URL(input.rhinoServerUrl);
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
    if (!Strings.isNullOrEmpty(input.memcachedHost) && input.memcachedPort == null) {
      throw new RuntimeConfigurationException("No memcachedPort specified");
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

    private String rhinoServerUrl;
    private String rootPagePath;
    private String environment;
    private List<Map<String, ?>> themeSources;

    private SolrConfigurationInput solr;
    private UserApiConfigurationInput userApi;
    private String memcachedHost;
    private String casUrl;
    private Integer memcachedPort;

    /**
     * @deprecated For access by reflective deserializer only
     */
    @Deprecated
    public void setServer(String rhinoServerUrl) {
      this.rhinoServerUrl = rhinoServerUrl;
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
    public void setEnvironment(String environment) {
      this.environment = environment;
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
  }

  private String casUrl;

  /**
   * @deprecated For access by reflective deserializer only
   */
  @Deprecated
  public void setCasUrl(String casUrl) {
    this.casUrl = casUrl;
  }

  public static class SolrConfigurationInput {
    private String url;
    private String journalsCollection;

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
