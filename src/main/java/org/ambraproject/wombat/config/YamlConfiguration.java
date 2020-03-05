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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import com.google.common.base.Strings;
import com.google.gson.GsonBuilder;

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

  @Override
  public String getThemePath() {
    return input.themePath;
  }
  
  @Override
  public String getMemcachedServer() {
    return input.memcachedServer;
  }

  @Override
  public String getCasUrl() {
    return input.casUrl;
  }

  @Override
  public URL getSolrUrl() {
    return buildUrl(input.solrUrl, null);
  }

  @Override
  public URI getUserApiUrl() {
    try {
      return new URI(input.userApiUrl);
    } catch (URISyntaxException ex) {
      throw new IllegalStateException("Invalid URL should have been caught at validation", ex);
    }
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
    if (!Strings.isNullOrEmpty(input.solrUrl)) {
      try {
        new URL(input.solrUrl);
      } catch (MalformedURLException e) {
        throw new RuntimeConfigurationException("Provided solr server address is not a valid URL", e);
      }
    }

    try {
      new URL(input.userApiUrl);
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
    private String themePath;
    private String memcachedServer;
    private String casUrl;
    private String solrUrl;
    private String userApiUrl;

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
    public void setThemePath(String themePath) {
      this.themePath = themePath;
    }

    /**
     * @deprecated For access by reflective deserializer only
     */
    @Deprecated
    public void setMemcachedServer(String memcachedServer) {
      this.memcachedServer = memcachedServer;
    }

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
    public void setUserApiUrl(String userApiUrl) {
      this.userApiUrl = userApiUrl;
    }
  }
}
