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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import freemarker.cache.TemplateLoader;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.site.url.SiteRequestScheme;
import org.ambraproject.wombat.config.theme.Theme;
import org.ambraproject.wombat.service.ApiAddress;
import org.ambraproject.wombat.service.ArticleService;
import org.ambraproject.wombat.service.ArticleTransformService;
import org.ambraproject.wombat.service.TestArticleServiceImpl;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ContextConfiguration
public class ArticleControllerTest extends ControllerTest {

  private static final ArticleTransformService articleTransformService = mock(ArticleTransformService.class);
  private static final String SITE_UNDER_TEST = "DesktopPlosOne";
  private static final ImmutableMap<String, String> SITE_CONFIG = ImmutableMap.<String, String>builder()
          .put("DesktopPlosOne", "PLoSONE")
          .put("DesktopPlosCollections", "PLoSCollections")
          .build();
  private static Map<String, String> MOCK_CONFIG_MAP = new HashMap<>();


  private static class MockTheme extends Theme {

    private MockTheme(String key) {
      super(key, ImmutableList.of());
    }

    @Override
    protected InputStream fetchStaticResource(String path) throws IOException {
      String response;
      if (path.equals(("config/journal.json"))) {
        response = MOCK_CONFIG_MAP.get(this.getKey());
      } else {
        return null;
      }
      return new ByteArrayInputStream(response.getBytes());
    }

    @Override
    protected ResourceAttributes fetchResourceAttributes(String path) throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    protected Collection<String> fetchStaticResourcePaths(String root) throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public TemplateLoader getTemplateLoader() throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public String describeSource() {
      return getClass().getSimpleName();
    }
  }

  @Configuration
  @EnableWebMvc
  static class TestConfig extends WombatControllerTestConfig {

    @Bean
    public ArticleController articleController() {
      return new ArticleController();
    }

    @Bean
    public ArticleApi articleApi() throws IOException {
      ArticleApi articleApi = mock(ArticleApi.class);
      when(articleApi.requestObject(
          argThat(new ArgumentMatcher<ApiAddress>() {
            @Override
            public boolean matches(Object o) {
              return (o instanceof ApiAddress) && ((ApiAddress) o).getAddress().endsWith("authors");
            }
          }),
          any(Class.class))
      ).thenReturn(ImmutableList.of());
      return articleApi;
    }

    @Bean
    public ArticleTransformService articleTransformService() {
      return articleTransformService;
    }

    @Bean
    public ArticleService articleService() {
      return new TestArticleServiceImpl();
    }

    @Override
    @Bean
    public SiteSet siteSetDependency() {

      SiteSet siteSet = mock(SiteSet.class);

      ImmutableSet.Builder<Site> testSiteBuilder = ImmutableSet.builder();
      for (String key : SITE_CONFIG.keySet()){
        SiteRequestScheme mockRequestScheme = mock(SiteRequestScheme.class);
        when(mockRequestScheme.isForSite(any(HttpServletRequest.class))).thenReturn(key.contentEquals(SITE_UNDER_TEST));

        Theme theme = new MockTheme(key);

        Site site = mock(Site.class);
        when(site.getRequestScheme()).thenReturn(mockRequestScheme);
        when(site.getTheme()).thenReturn(theme);
        when(site.getKey()).thenReturn(key);
        when(site.toString()).thenReturn(key);
        when(site.getJournalKey()).thenReturn(SITE_CONFIG.get(key));

        when(siteSet.getSite(Matchers.matches(key))).thenReturn(site);

        testSiteBuilder.add(site);
      }

      when(siteSet.getSites()).thenReturn(testSiteBuilder.build());

      return siteSet;
    }
  }
}