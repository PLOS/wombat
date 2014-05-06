/*
 * Copyright (c) 2006-2014 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.wombat.config;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.ambraproject.rhombat.cache.Cache;
import org.ambraproject.rhombat.cache.MemcacheClient;
import org.ambraproject.rhombat.cache.NullCache;
import org.ambraproject.rhombat.gson.Iso8601DateAdapter;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.site.SiteTemplateLoader;
import org.ambraproject.wombat.config.theme.InternalTheme;
import org.ambraproject.wombat.config.theme.ThemeTree;
import org.ambraproject.wombat.controller.SiteResolver;
import org.ambraproject.wombat.freemarker.BuildInfoDirective;
import org.ambraproject.wombat.freemarker.CssLinkDirective;
import org.ambraproject.wombat.freemarker.Iso8601DateDirective;
import org.ambraproject.wombat.freemarker.JsDirective;
import org.ambraproject.wombat.freemarker.RandomIntegerDirective;
import org.ambraproject.wombat.freemarker.RenderCssLinksDirective;
import org.ambraproject.wombat.freemarker.RenderJsDirective;
import org.ambraproject.wombat.freemarker.ReplaceParametersDirective;
import org.ambraproject.wombat.freemarker.SiteLinkDirective;
import org.ambraproject.wombat.service.ArticleService;
import org.ambraproject.wombat.service.ArticleServiceImpl;
import org.ambraproject.wombat.service.ArticleTransformService;
import org.ambraproject.wombat.service.ArticleTransformServiceImpl;
import org.ambraproject.wombat.service.AssetService;
import org.ambraproject.wombat.service.AssetServiceImpl;
import org.ambraproject.wombat.service.BuildInfoService;
import org.ambraproject.wombat.service.BuildInfoServiceImpl;
import org.ambraproject.wombat.service.SearchService;
import org.ambraproject.wombat.service.SoaService;
import org.ambraproject.wombat.service.SoaServiceImpl;
import org.ambraproject.wombat.service.SolrSearchService;
import org.ambraproject.wombat.util.GitInfo;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;
import org.yaml.snakeyaml.Yaml;

import javax.servlet.ServletContext;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Date;

@Configuration
public class SpringConfiguration {

  @Bean
  public Gson gson() {
    GsonBuilder builder = new GsonBuilder();
    builder.setPrettyPrinting();
    builder.registerTypeAdapter(Date.class, new Iso8601DateAdapter());
    return builder.create();
  }

  @Bean
  public Yaml yaml() {
    return new Yaml();
  }

  @Bean
  public RuntimeConfiguration runtimeConfiguration(Yaml yaml) throws IOException {
    final File configPath = new File("/etc/ambra/wombat.yaml"); // TODO Descriptive file name
    if (!configPath.exists()) {
      throw new RuntimeConfigurationException(configPath.getPath() + " not found");
    }

    JsonConfiguration runtimeConfiguration;
    try (Reader reader = new BufferedReader(new FileReader(configPath))) {
      runtimeConfiguration = new JsonConfiguration(yaml.loadAs(reader, JsonConfiguration.UserFields.class));
    } catch (JsonSyntaxException e) {
      throw new RuntimeConfigurationException(configPath + " contains invalid JSON", e);
    }
    runtimeConfiguration.validate();
    return runtimeConfiguration;
  }

  @Bean
  public ThemeTree themeTree(ServletContext servletContext, RuntimeConfiguration runtimeConfiguration)
      throws ThemeTree.ThemeConfigurationException {
    String path = "/WEB-INF/themes/";
    InternalTheme root = new InternalTheme(".Root", null, servletContext, path + "root/");
    ImmutableList<InternalTheme> listOfRoot = ImmutableList.of(root);
    InternalTheme desktop = new InternalTheme(".Desktop", listOfRoot, servletContext, path + "desktop/");
    InternalTheme mobile = new InternalTheme(".Mobile", listOfRoot, servletContext, path + "mobile/");
    return runtimeConfiguration.getThemes(ImmutableSet.of(root, desktop, mobile), root);
  }

  @Bean
  public SiteSet siteSet(RuntimeConfiguration runtimeConfiguration,
                         ThemeTree themeTree) {
    return SiteSet.create(runtimeConfiguration.getThemesForSites(themeTree));
  }

  @Bean
  public SiteResolver siteResolver() {
    return new SiteResolver();
  }

  @Bean
  public SiteLinkDirective siteLinkDirective() {
    return new SiteLinkDirective();
  }

  @Bean
  public CssLinkDirective cssLinkDirective() {
    return new CssLinkDirective();
  }

  @Bean
  public RenderCssLinksDirective renderCssLinksDirective() {
    return new RenderCssLinksDirective();
  }

  @Bean
  JsDirective jsDirective() {
    return new JsDirective();
  }

  @Bean
  RenderJsDirective renderJsDirective() {
    return new RenderJsDirective();
  }

  @Bean
  public BuildInfoDirective buildInfoDirective() {
    return new BuildInfoDirective();
  }

  @Bean
  public FreeMarkerConfig freeMarkerConfig(ServletContext servletContext, SiteSet siteSet,
                                           SiteLinkDirective siteLinkDirective,
                                           CssLinkDirective cssLinkDirective,
                                           RenderCssLinksDirective renderCssLinksDirective,
                                           JsDirective jsDirective,
                                           RenderJsDirective renderJsDirective,
                                           BuildInfoDirective buildInfoDirective)
      throws IOException {
    SiteTemplateLoader loader = new SiteTemplateLoader(servletContext, siteSet);
    FreeMarkerConfigurer config = new FreeMarkerConfigurer();
    config.setPreTemplateLoaders(loader);

    // Freemarker custom directives used throughout the app.
    // TODO: should all of these be their own @Beans?  I'm only doing that now for
    // ones that have dependencies on spring-injection.
    ImmutableMap.Builder<String, Object> variables = ImmutableMap.builder();
    variables.put("formatJsonDate", new Iso8601DateDirective());
    variables.put("replaceParams", new ReplaceParametersDirective());
    variables.put("randomInteger", new RandomIntegerDirective());
    variables.put("siteLink", siteLinkDirective);
    variables.put("cssLink", cssLinkDirective);
    variables.put("renderCssLinks", renderCssLinksDirective);
    variables.put("js", jsDirective);
    variables.put("renderJs", renderJsDirective);
    variables.put("buildInfo", buildInfoDirective);
    config.setFreemarkerVariables(variables.build());
    return config;
  }

  @Bean
  public Charset charset() {
    return Charsets.UTF_8;
  }

  @Bean
  public FreeMarkerViewResolver viewResolver(Charset charset) {
    FreeMarkerViewResolver resolver = new FreeMarkerViewResolver();
    resolver.setContentType("text/html;charset=" + charset);
    resolver.setCache(true);
    resolver.setPrefix("");
    resolver.setSuffix(".ftl");
    return resolver;
  }

  @Bean
  public SoaService soaService() {
    return new SoaServiceImpl();
  }

  @Bean
  public ArticleService articleService() {
    return new ArticleServiceImpl();
  }

  @Bean
  public ArticleTransformService articleTransformService() {
    return new ArticleTransformServiceImpl();
  }

  @Bean
  public SearchService searchService() {
    return new SolrSearchService();
  }

  @Bean
  public Cache cache(RuntimeConfiguration runtimeConfiguration) throws IOException {
    if (!Strings.isNullOrEmpty(runtimeConfiguration.getMemcachedHost())) {

      // TODO: consider defining this in wombat.yaml instead.
      final int cacheTimeout = 60 * 60;
      MemcacheClient result = new MemcacheClient(runtimeConfiguration.getMemcachedHost(),
          runtimeConfiguration.getMemcachedPort(), runtimeConfiguration.getCacheAppPrefix(), cacheTimeout);
      result.connect();
      return result;
    } else {
      return new NullCache();
    }
  }

  @Bean
  public AssetService assetService() {
    return new AssetServiceImpl();
  }

  @Bean
  public HttpClientConnectionManager httpClientConnectionManager(RuntimeConfiguration runtimeConfiguration) {
    PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();

    Integer maxTotal = runtimeConfiguration.getConnectionPoolMaxTotal();
    if (maxTotal != null) manager.setMaxTotal(maxTotal);
    Integer defaultMaxPerRoute = runtimeConfiguration.getConnectionPoolDefaultMaxPerRoute();
    if (defaultMaxPerRoute != null) manager.setDefaultMaxPerRoute(defaultMaxPerRoute);

    return manager;
  }

  @Bean
  public BuildInfoService buildInfoService() {
    return new BuildInfoServiceImpl();
  }

  @Bean
  public GitInfo gitInfo(Environment environment) {
    GitInfo gitInfo = new GitInfo(environment);
    return gitInfo;
  }

}
