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

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.ambraproject.wombat.config.site.RequestMappingContextDictionary;
import org.ambraproject.wombat.config.site.SiteResolver;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.site.SiteTemplateLoader;
import org.ambraproject.wombat.config.theme.InternalTheme;
import org.ambraproject.wombat.config.theme.ThemeBuilder;
import org.ambraproject.wombat.config.theme.ThemeGraph;
import org.ambraproject.wombat.config.theme.ThemeSource;
import org.ambraproject.wombat.controller.AppRootPage;
import org.ambraproject.wombat.controller.ArticleMetadata;
import org.ambraproject.wombat.controller.DoiVersionArgumentResolver;
import org.ambraproject.wombat.feed.ArticleFeedView;
import org.ambraproject.wombat.feed.CommentFeedView;
import org.ambraproject.wombat.freemarker.AbbreviatedNameDirective;
import org.ambraproject.wombat.freemarker.AppLinkDirective;
import org.ambraproject.wombat.freemarker.ArticleExcerptTransformDirective;
import org.ambraproject.wombat.freemarker.BuildInfoDirective;
import org.ambraproject.wombat.freemarker.FetchHtmlDirective;
import org.ambraproject.wombat.freemarker.GlobalConfigDirective;
import org.ambraproject.wombat.freemarker.IsDevFeatureEnabledDirective;
import org.ambraproject.wombat.freemarker.Iso8601DateDirective;
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
import org.ambraproject.wombat.service.AlertService;
import org.ambraproject.wombat.service.ArticleArchiveServiceImpl;
import org.ambraproject.wombat.service.ArticleResolutionService;
import org.ambraproject.wombat.service.ArticleService;
import org.ambraproject.wombat.service.ArticleServiceImpl;
import org.ambraproject.wombat.service.ArticleTransformService;
import org.ambraproject.wombat.service.ArticleTransformServiceImpl;
import org.ambraproject.wombat.service.AssetService;
import org.ambraproject.wombat.service.AssetServiceImpl;
import org.ambraproject.wombat.service.BrowseTaxonomyService;
import org.ambraproject.wombat.service.BrowseTaxonomyServiceImpl;
import org.ambraproject.wombat.service.BuildInfoService;
import org.ambraproject.wombat.service.BuildInfoServiceImpl;
import org.ambraproject.wombat.service.CaptchaService;
import org.ambraproject.wombat.service.CaptchaServiceImpl;
import org.ambraproject.wombat.service.CitationDownloadService;
import org.ambraproject.wombat.service.CitationDownloadServiceImpl;
import org.ambraproject.wombat.service.CommentCensorService;
import org.ambraproject.wombat.service.CommentCensorServiceImpl;
import org.ambraproject.wombat.service.CommentService;
import org.ambraproject.wombat.service.CommentServiceImpl;
import org.ambraproject.wombat.service.CommentValidationService;
import org.ambraproject.wombat.service.CommentValidationServiceImpl;
import org.ambraproject.wombat.service.DoiToJournalResolutionService;
import org.ambraproject.wombat.service.FreemarkerMailService;
import org.ambraproject.wombat.service.FreemarkerMailServiceImpl;
import org.ambraproject.wombat.service.ParseReferenceService;
import org.ambraproject.wombat.service.ParseXmlService;
import org.ambraproject.wombat.service.ParseXmlServiceImpl;
import org.ambraproject.wombat.service.RecentArticleService;
import org.ambraproject.wombat.service.RecentArticleServiceImpl;
import org.ambraproject.wombat.service.TopLevelLockssManifestService;
import org.ambraproject.wombat.service.remote.CorpusContentApi;
import org.ambraproject.wombat.service.remote.EditorialContentApi;
import org.ambraproject.wombat.service.remote.EditorialContentApiImpl;
import org.ambraproject.wombat.service.SearchFilterService;
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
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class SpringConfiguration {

  @Bean
  public ThemeGraph themeGraph(ServletContext servletContext, RuntimeConfiguration runtimeConfiguration)
      throws ThemeGraph.ThemeConfigurationException, IOException {
    String path = "/WEB-INF/themes/";
    InternalTheme root = new InternalTheme(".Root", ImmutableList.of(), servletContext, path + "root/");
    InternalTheme desktop = new InternalTheme(".Desktop", ImmutableList.of(root), servletContext, path + "desktop/");
    InternalTheme mobile = new InternalTheme(".Mobile", ImmutableList.of(root), servletContext, path + "mobile/");
    Collection<InternalTheme> internalThemes = ImmutableList.of(root, desktop, mobile);

    Collection<ThemeSource<?>> themeSources = runtimeConfiguration.getThemeSources();
    Collection<ThemeBuilder<?>> themeBuilders = themeSources.stream()
        .flatMap(ts -> ts.readThemes().stream())
        .collect(Collectors.toList());

    return ThemeGraph.create(root, internalThemes, themeBuilders);
  }

  @Bean
  public SiteSet siteSet(RuntimeConfiguration runtimeConfiguration,
                         ThemeGraph themeGraph) {
    Collection<ThemeSource<?>> themeSources = runtimeConfiguration.getThemeSources();
    List<Map<String, ?>> siteSpecs = themeSources.stream()
        .flatMap(ts -> ts.readSites().stream())
        .collect(Collectors.toList());
    if (siteSpecs.isEmpty()) {
      throw new RuntimeException("No sites found in sites.yaml files at: " +
          themeSources.stream().map(Object::toString).collect(Collectors.joining(", ")));
    }
    return SiteSet.create(siteSpecs, themeGraph);
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
  public DoiVersionArgumentResolver doiVersionArgumentResolver() {
    return new DoiVersionArgumentResolver();
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
  public ArticleExcerptTransformDirective articleExcerptTransformDirective() {
    return new ArticleExcerptTransformDirective();
  }

  @Bean
  public GlobalConfigDirective globalConfigDirective() {
    return new GlobalConfigDirective();
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
                                           AppLinkDirective appLinkDirective,
                                           ArticleExcerptTransformDirective articleExcerptTransformDirective,
                                           GlobalConfigDirective globalConfigDirective)
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
    variables.put("xform", articleExcerptTransformDirective);
    variables.put("globalConfig", globalConfigDirective);
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
  public JournalFilterType journalFilterType() {
    return new JournalFilterType();
  }

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
  public SearchFilterFactory searchFilterFactory() {
    return new SearchFilterFactory();
  }

  @Bean
  public SearchFilterService searchFilterService() {
    return new SearchFilterService();
  }

  @Bean
  public AlertService alertService() {
    return new AlertService();
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
  public ArticleResolutionService articleResolutionService() {
    return new ArticleResolutionService();
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
  public EditorialContentApi editorialContentApi() {
    return new EditorialContentApiImpl();
  }

  @Bean
  public CorpusContentApi corpusContentApi() {
    return new CorpusContentApi();
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

  @Bean
  public BrowseTaxonomyService browseTaxonomyService() {
    return new BrowseTaxonomyServiceImpl();
  }

  @Bean
  public CommentService commentService() {
    return new CommentServiceImpl();
  }

  @Bean
  public ArticleFeedView articleFeedView() {
    return new ArticleFeedView();
  }

  @Bean
  public CommentFeedView commentFeedView() {
    return new CommentFeedView();
  }

  @Bean
  public ArticleMetadata.Factory articleMetadataFactory() {
    return new ArticleMetadata.Factory();
  }

  @Bean
  public ParseXmlService parseXmlService() {
    return new ParseXmlServiceImpl();
  }

  @Bean
  public ParseReferenceService parseReferenceService() {
    return new ParseReferenceService();
  }

  @Bean
  public DoiToJournalResolutionService doiToJournalResolutionService() {
    return new DoiToJournalResolutionService();
  }

  @Bean
  public TopLevelLockssManifestService topLevelLockssManifestService() {
    return new TopLevelLockssManifestService();
  }

}
