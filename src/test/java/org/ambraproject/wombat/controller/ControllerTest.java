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

package org.ambraproject.wombat.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.ambraproject.rhombat.gson.Iso8601DateAdapter;
import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.ambraproject.wombat.config.TestRuntimeConfiguration;
import org.ambraproject.wombat.config.site.RequestMappingContextDictionary;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteResolver;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.site.url.SiteRequestScheme;
import org.ambraproject.wombat.config.theme.Theme;
import org.ambraproject.wombat.service.ArticleTransformService;
import org.ambraproject.wombat.service.FreemarkerMailService;
import org.ambraproject.wombat.service.HoneypotService;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.ambraproject.wombat.service.remote.ArticleApiImpl;
import org.ambraproject.wombat.service.remote.CachedRemoteService;
import org.ambraproject.wombat.service.remote.JsonService;
import org.ambraproject.wombat.service.remote.SolrSearchApi;
import org.ambraproject.wombat.service.remote.SolrSearchApiImpl;
import org.ambraproject.wombat.service.remote.UserApi;
import org.ambraproject.wombat.util.JodaTimeLocalDateAdapter;
import org.ambraproject.wombat.util.ThemeTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;
import org.testng.annotations.BeforeMethod;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@WebAppConfiguration
public class ControllerTest extends AbstractTestNGSpringContextTests {

  private static final RuntimeConfiguration runtimeConfiguration = new TestRuntimeConfiguration();

  protected static final ImmutableMap<String, String> SITE_CONFIG = ImmutableMap.of(
      "DesktopPlosOne", "DesktopPlosOne",
      "DesktopPlosCollections", "DesktopPlosCollections");

  @Autowired
  protected WebApplicationContext wac;

  protected MockMvc mockMvc;

  @Bean
  protected Gson gson() {
    GsonBuilder builder = new GsonBuilder();
    builder.setPrettyPrinting();
    builder.registerTypeAdapter(Date.class, new Iso8601DateAdapter());
    builder.registerTypeAdapter(org.joda.time.LocalDate.class, JodaTimeLocalDateAdapter.INSTANCE);
    return builder.create();
  }

  @Bean
  protected JsonService jsonService() {
    return new JsonService();
  }

  @Bean
  protected RuntimeConfiguration runtimeConfiguration() {
    return runtimeConfiguration;
  }

  @Bean
  protected Charset charset() {
    return Charsets.UTF_8;
  }

  @Bean
  protected SiteResolver siteResolver() {
    return new SiteResolver();
  }

  @Bean
  protected DoiVersionArgumentResolver doiVersionArgumentResolver() {
    return new DoiVersionArgumentResolver();
  }

  @Bean
  protected RequestMappingContextDictionary handlerDirectory() {
    final RequestMappingContextDictionary handlerDirectory = new RequestMappingContextDictionary();
    return handlerDirectory;
  }

  @Bean
  protected SiteSet siteSet() {
    final SiteSet siteSet = mock(SiteSet.class);

    ImmutableSet.Builder<Site> testSiteBuilder = ImmutableSet.builder();
    for (String key : SITE_CONFIG.keySet()){
      final SiteRequestScheme mockRequestScheme = mock(SiteRequestScheme.class);
      when(mockRequestScheme.isForSite(any(HttpServletRequest.class))).thenReturn(true);

      final Theme mockTheme = mock(ThemeTest.class);
      final Site site = mock(Site.class);
      when(site.getRequestScheme()).thenReturn(mockRequestScheme);
      when(site.getTheme()).thenReturn(mockTheme);
      when(site.getKey()).thenReturn(key);
      when(site.toString()).thenReturn(key);
      when(site.getJournalKey()).thenReturn(SITE_CONFIG.get(key));

      when(siteSet.getSite(key)).thenReturn(site);

      testSiteBuilder.add(site);
    }
    when(siteSet.getSites()).thenReturn(testSiteBuilder.build());

    return siteSet;
  }

  @Bean
  protected HoneypotService honeypotService() {
    final HoneypotService honeypotService = mock(HoneypotService.class);
    return honeypotService;
  }

  @Bean
  protected UserApi userApi() {
    final UserApi userApi = mock(UserApi.class);
    return userApi;
  }

  @Bean
  protected CachedRemoteService<Reader> cachedRemoteReader() {
    @SuppressWarnings("unchecked")
    final CachedRemoteService<Reader> cachedRemoteReader = mock(CachedRemoteService.class);
    return cachedRemoteReader;
  }

  @Bean
  protected SolrSearchApi solrSearchApi() {
    final SolrSearchApi solrSearchApi = spy(SolrSearchApiImpl.class);
    return solrSearchApi;
  }

  @Bean
  protected CachedRemoteService<InputStream> cachedRemoteInputStream() {
    @SuppressWarnings("unchecked")
    final CachedRemoteService<InputStream> cachedRemoteReader = mock(CachedRemoteService.class);
    return cachedRemoteReader;
  }

  @Bean
  protected ArticleApi articleApi() {
    final ArticleApi articleApi = spy(ArticleApiImpl.class);
    return articleApi;
  }

  @Bean
  protected ArticleTransformService articleTransformService() {
    final ArticleTransformService articleTransformService = mock(ArticleTransformService.class);
    return articleTransformService;
  }

  @Bean
  protected FreeMarkerConfig freeMarkerConfig() {
    final FreeMarkerConfig freeMarkerConfig = mock(FreeMarkerConfig.class);
    return freeMarkerConfig;
  }

  @Bean
  protected FreemarkerMailService freemarkerMailService() {
    final FreemarkerMailService freemarkerMailService = mock(FreemarkerMailService.class);
    return freemarkerMailService;
  }

  @Bean
  protected JavaMailSender javaMailSender() {
    final JavaMailSender javaMailSender = mock(JavaMailSender.class);
    return javaMailSender;
  }

  @BeforeMethod
  public void setUp() throws IOException {
    mockMvc = webAppContextSetup(wac).alwaysDo(print()).build();
  }
}
