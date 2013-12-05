/*
 * $HeadURL$
 * $Id$
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

  /**
   * @deprecated should only be called reflectively by Gson
   */
  @Deprecated
  public JsonConfiguration() {
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
   * {@inheritDoc}
   */
  @Override
  public boolean trustUnsignedServer() {
    return (trustUnsignedServer == null) ? false : trustUnsignedServer;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean devModeAssets() {
    return (devModeAssets == null) ? false : devModeAssets;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getCompiledAssetDir() {
    return compiledAssetDir;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getMemcachedHost() {
    return memcachedHost;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getMemcachedPort() {
    return memcachedPort == null ? -1 : memcachedPort;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getCacheAppPrefix() {
    return cacheAppPrefix;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public URL getServer() {
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

  /**
   * {@inheritDoc}
   */
  @Override
  public ThemeTree getThemes(Theme internalDefault) throws ThemeTree.ThemeConfigurationException {
    return ThemeTree.parse(this.themes, internalDefault);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ImmutableMap<String, Theme> getThemesForSites(ThemeTree themeTree) {
    return themeTree.matchToSites(sites);
  }

  /**
   * {@inheritDoc}
   */
  @Override
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
