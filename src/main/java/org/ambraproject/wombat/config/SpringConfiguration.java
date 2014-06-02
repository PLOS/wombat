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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.site.SiteTemplateLoader;
import org.ambraproject.wombat.config.theme.InternalTheme;
import org.ambraproject.wombat.config.theme.ThemeTree;
import org.ambraproject.wombat.controller.SiteResolver;
import org.ambraproject.wombat.freemarker.AppLinkDirective;
import org.ambraproject.wombat.freemarker.BuildInfoDirective;
import org.ambraproject.wombat.freemarker.CssLinkDirective;
import org.ambraproject.wombat.freemarker.FetchHtmlDirective;
import org.ambraproject.wombat.freemarker.Iso8601DateDirective;
import org.ambraproject.wombat.freemarker.JsDirective;
import org.ambraproject.wombat.freemarker.RandomIntegerDirective;
import org.ambraproject.wombat.freemarker.RenderCssLinksDirective;
import org.ambraproject.wombat.freemarker.RenderJsDirective;
import org.ambraproject.wombat.freemarker.ReplaceParametersDirective;
import org.ambraproject.wombat.freemarker.SiteLinkDirective;
import org.ambraproject.wombat.freemarker.ThemeConfigDirective;
import org.ambraproject.wombat.service.ArticleService;
import org.ambraproject.wombat.service.ArticleServiceImpl;
import org.ambraproject.wombat.service.ArticleTransformService;
import org.ambraproject.wombat.service.ArticleTransformServiceImpl;
import org.ambraproject.wombat.service.AssetService;
import org.ambraproject.wombat.service.AssetServiceImpl;
import org.ambraproject.wombat.service.BuildInfoService;
import org.ambraproject.wombat.service.BuildInfoServiceImpl;
import org.ambraproject.wombat.service.RecentArticleService;
import org.ambraproject.wombat.service.RecentArticleServiceImpl;
import org.ambraproject.wombat.service.remote.StoredHomepageService;
import org.ambraproject.wombat.util.GitInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.nio.charset.Charset;

@Configuration
public class SpringConfiguration {

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
    return runtimeConfiguration.getSites(themeTree);
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
  public FetchHtmlDirective fetchHtmlDirective() {
    return new FetchHtmlDirective();
  }

  @Bean
  public ThemeConfigDirective themeConfigDirective() {
    return new ThemeConfigDirective();
  }

  @Bean
  public FreeMarkerConfig freeMarkerConfig(ServletContext servletContext, SiteSet siteSet,
                                           SiteLinkDirective siteLinkDirective,
                                           CssLinkDirective cssLinkDirective,
                                           RenderCssLinksDirective renderCssLinksDirective,
                                           JsDirective jsDirective,
                                           RenderJsDirective renderJsDirective,
                                           BuildInfoDirective buildInfoDirective,
                                           FetchHtmlDirective fetchHtmlDirective,
                                           ThemeConfigDirective themeConfigDirective,
                                           AppLinkDirective appLinkDirective)
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
    variables.put("fetchHtml", fetchHtmlDirective);
    variables.put("themeConfig", themeConfigDirective);
    variables.put("appLink", appLinkDirective);
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
  public ArticleService articleService() {
    return new ArticleServiceImpl();
  }

  @Bean
  public ArticleTransformService articleTransformService() {
    return new ArticleTransformServiceImpl();
  }

  @Bean
  public AssetService assetService() {
    return new AssetServiceImpl();
  }

  @Bean
  public StoredHomepageService storedHomepageService() {
    return new StoredHomepageService();
  }

  @Bean
  public BuildInfoService buildInfoService() {
    return new BuildInfoServiceImpl();
  }

  @Bean
  public RecentArticleService recentArticleService() {
    return new RecentArticleServiceImpl();
  }

  @Bean
  public GitInfo gitInfo(Environment environment) {
    GitInfo gitInfo = new GitInfo(environment);
    return gitInfo;
  }

  @Bean
  public AppLinkDirective appLinkDirective() {
    return new AppLinkDirective();
  }

}
