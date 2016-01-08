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
import org.ambraproject.wombat.config.site.RequestMappingContextDictionary;
import org.ambraproject.wombat.config.site.SiteResolver;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.site.SiteTemplateLoader;
import org.ambraproject.wombat.config.theme.InternalTheme;
import org.ambraproject.wombat.config.theme.ThemeTree;
import org.ambraproject.wombat.controller.AppRootPage;
import org.ambraproject.wombat.freemarker.AbbreviatedNameDirective;
import org.ambraproject.wombat.freemarker.AppLinkDirective;
import org.ambraproject.wombat.freemarker.BuildInfoDirective;
import org.ambraproject.wombat.freemarker.FetchHtmlDirective;
import org.ambraproject.wombat.freemarker.IsDevFeatureEnabledDirective;
import org.ambraproject.wombat.freemarker.Iso8601DateDirective;
import org.ambraproject.wombat.freemarker.RandomIntegerDirective;
import org.ambraproject.wombat.freemarker.ReplaceParametersDirective;
import org.ambraproject.wombat.freemarker.SiteLinkDirective;
import org.ambraproject.wombat.freemarker.ThemeConfigDirective;
import org.ambraproject.wombat.freemarker.asset.CssLinkDirective;
import org.ambraproject.wombat.freemarker.asset.JsDirective;
import org.ambraproject.wombat.freemarker.asset.RenderCssLinksDirective;
import org.ambraproject.wombat.freemarker.asset.RenderJsDirective;
import org.ambraproject.wombat.model.JournalFilterType;
import org.ambraproject.wombat.model.SearchFilterFactory;
import org.ambraproject.wombat.model.SearchFilterType;
import org.ambraproject.wombat.model.SearchFilterTypeMap;
import org.ambraproject.wombat.model.SingletonSearchFilterType;
import org.ambraproject.wombat.service.ArticleArchiveServiceImpl;
import org.ambraproject.wombat.service.ArticleService;
import org.ambraproject.wombat.service.ArticleServiceImpl;
import org.ambraproject.wombat.service.ArticleTransformService;
import org.ambraproject.wombat.service.ArticleTransformServiceImpl;
import org.ambraproject.wombat.service.AssetService;
import org.ambraproject.wombat.service.AssetServiceImpl;
import org.ambraproject.wombat.service.BuildInfoService;
import org.ambraproject.wombat.service.BuildInfoServiceImpl;
import org.ambraproject.wombat.service.CaptchaService;
import org.ambraproject.wombat.service.CaptchaServiceImpl;
import org.ambraproject.wombat.service.CitationDownloadService;
import org.ambraproject.wombat.service.CitationDownloadServiceImpl;
import org.ambraproject.wombat.service.CommentCensorService;
import org.ambraproject.wombat.service.CommentCensorServiceImpl;
import org.ambraproject.wombat.service.CommentValidationService;
import org.ambraproject.wombat.service.CommentValidationServiceImpl;
import org.ambraproject.wombat.service.FreemarkerMailService;
import org.ambraproject.wombat.service.FreemarkerMailServiceImpl;
import org.ambraproject.wombat.service.PowerPointService;
import org.ambraproject.wombat.service.PowerPointServiceImpl;
import org.ambraproject.wombat.service.RecentArticleService;
import org.ambraproject.wombat.service.RecentArticleServiceImpl;
import org.ambraproject.wombat.service.remote.EditorialContentService;
import org.ambraproject.wombat.service.remote.EditorialContentServiceImpl;
import org.ambraproject.wombat.service.remote.SearchFilterService;
import org.ambraproject.wombat.util.GitInfo;
import org.ambraproject.wombat.util.NullJavaMailSender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.net.URL;
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
  public IsDevFeatureEnabledDirective isDevFeatureEnabledDirective() {
    return new IsDevFeatureEnabledDirective();
  }

  @Bean
  public SiteResolver siteResolver() {
    return new SiteResolver();
  }

  @Bean
  public RequestMappingContextDictionary handlerDirectory() {
    return new RequestMappingContextDictionary();
  }

  @Bean
  public SiteLinkDirective siteLinkDirective() {
    return new SiteLinkDirective();
  }

  @Bean
  public RenderCssLinksDirective renderCssLinksDirective() {
    return new RenderCssLinksDirective();
  }

  @Bean
  public RenderJsDirective renderJsDirective() {
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
                                           IsDevFeatureEnabledDirective isDevFeatureEnabledDirective,
                                           SiteLinkDirective siteLinkDirective,
                                           RenderCssLinksDirective renderCssLinksDirective,
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
    variables.put("isDevFeatureEnabled", isDevFeatureEnabledDirective);
    variables.put("cssLink", new CssLinkDirective());
    variables.put("renderCssLinks", renderCssLinksDirective);
    variables.put("js", new JsDirective());
    variables.put("renderJs", renderJsDirective);
    variables.put("buildInfo", buildInfoDirective);
    variables.put("fetchHtml", fetchHtmlDirective);
    variables.put("themeConfig", themeConfigDirective);
    variables.put("appLink", appLinkDirective);
    variables.put("abbreviatedName", new AbbreviatedNameDirective());
    config.setFreemarkerVariables(variables.build());
    return config;
  }

  @Bean
  public FreemarkerMailService freemarkerMailService() {
    return new FreemarkerMailServiceImpl();
  }

  @Bean
  public Charset charset() {
    return Charsets.UTF_8;
  }

  @Bean
  public JournalFilterType journalFilterType() { return new JournalFilterType(); }

  @Bean
  public AppRootPage appRootPage() {
    return new AppRootPage();
  }

  @Bean
  public JavaMailSender javaMailSender(RuntimeConfiguration runtimeConfiguration) {
    String mailServer = runtimeConfiguration.getMailServer();
    if (mailServer == null) return NullJavaMailSender.INSTANCE;
    JavaMailSenderImpl sender = new JavaMailSenderImpl();
    sender.setHost(mailServer);
    return sender;
  }

  @Bean
  public SearchFilterTypeMap searchFilterTypeMap(JournalFilterType journalFilterType) {
    ImmutableMap.Builder<String, SearchFilterType> builder = ImmutableMap.builder();
    builder.put("journal", journalFilterType);

    for (SingletonSearchFilterType value : SingletonSearchFilterType.values()) {
      builder.put(value.name().toLowerCase(), value);
    }
    return new SearchFilterTypeMap(builder.build());
  }

  @Bean
  public SearchFilterFactory searchFilterFactory() { return new SearchFilterFactory(); }

  @Bean
  public SearchFilterService searchFilterService() {
    return new SearchFilterService();
  }

  @Bean
  public FreeMarkerViewResolver viewResolver(Charset charset) {
    FreeMarkerViewResolver resolver = new FreeMarkerViewResolver();
    resolver.setContentType("text/html;charset=" + charset);
    resolver.setCache(true);
    resolver.setPrefix("");
    resolver.setSuffix(".ftl");
    resolver.setRequestContextAttribute("requestContext");
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
  public BuildInfoService buildInfoService() {
    return new BuildInfoServiceImpl();
  }

  @Bean
  public RecentArticleService recentArticleService() {
    return new RecentArticleServiceImpl();
  }

  @Bean
  public GitInfo gitInfo() {
    return new GitInfo();
  }

  @Bean
  public AppLinkDirective appLinkDirective() {
    return new AppLinkDirective();
  }

  @Bean
  public EditorialContentService editorialRepoService() {
    return new EditorialContentServiceImpl();
  }

  @Bean
  public PowerPointService powerPointService() {
    return new PowerPointServiceImpl();
  }

  @Bean
  public ArticleArchiveServiceImpl articleArchiveService() {
    return new ArticleArchiveServiceImpl();
  }

  @Bean
  public CitationDownloadService citationDownloadService() {
    return new CitationDownloadServiceImpl();
  }

  @Bean
  public CaptchaService captchaService() {
    return new CaptchaServiceImpl();
  }

  @Bean
  public CommentCensorService commentCensorService() {
    return new CommentCensorServiceImpl();
  }

  @Bean
  public CommentValidationService commentValidationService() {
    return new CommentValidationServiceImpl();
  }

}
