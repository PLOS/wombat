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

import static java.lang.String.format;
import static org.apache.http.HttpStatus.SC_OK;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;

import org.ambraproject.wombat.config.TestSpringConfiguration;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteResolver;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.site.url.SiteRequestScheme;
import org.ambraproject.wombat.config.theme.Theme;
import org.ambraproject.wombat.freemarker.Iso8601DateDirective;
import org.ambraproject.wombat.freemarker.ReplaceParametersDirective;
import org.ambraproject.wombat.service.AssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import org.junit.Before;
import org.junit.Test;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.TemplateDirectiveModel;

@ContextConfiguration
public class StaticResourceControllerTest extends ControllerTest {

  @Autowired
  AssetService assetService;

  private static String getMockResource(String path) {
    return "This is a mock resource for: " + path;
  }

  @Test
  public void testServeResource() throws Exception {
    mockMvc.perform(get(format("/resource/%s", "testResource")))
        .andExpect(handler().handlerType(StaticResourceController.class))
        .andExpect(handler().methodName("serveResource"))
        .andExpect(status().is(SC_OK))
        .andExpect(content().string(getMockResource("resource/testResource")))
        .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, (String) null))
        .andExpect(forwardedUrl(null))
        .andExpect(redirectedUrl(null))
        .andReturn();
  }

  @Test
  public void testServeResourceWithCors() throws Exception {
    mockMvc.perform(get(format("/resource/allow/%s", "testResource")))
        .andExpect(handler().handlerType(StaticResourceController.class))
        .andExpect(handler().methodName("serveResource"))
        .andExpect(status().is(SC_OK))
        .andExpect(content().string(getMockResource("resource/allow/testResource")))
        .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*"))
        .andExpect(forwardedUrl(null))
        .andExpect(redirectedUrl(null))
        .andReturn();
  }

  private static class MockTheme extends Theme {
    private MockTheme() {
      super("mockTheme", ImmutableList.of());
    }

    private static final ImmutableMap<String, Object> resourceConfig = ImmutableMap.<String, Object>builder()
        .put("cors", ImmutableList.of("allow/"))
        .build();

    @Override
    protected InputStream fetchStaticResource(String path) throws IOException {
      String response;
      if (path.equals("config/resource.json")) {
        response = new Gson().toJson(resourceConfig);
      } else if (path.startsWith("resource/")) {
        response = getMockResource(path);
      } else {
        return null;
      }
      return new ByteArrayInputStream(response.getBytes());
    }

    @Override
    protected ResourceAttributes fetchResourceAttributes(String path) throws IOException {
      return new Theme.ResourceAttributes() {
        @Override
        public long getLastModified() {
          return Long.MAX_VALUE; // always modified
        }

        @Override
        public long getContentLength() {
          return 0; // dummy
        }
      };
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
  @Import(TestSpringConfiguration.class)
  static class TestConfig extends WebMvcConfigurerAdapter {

    private static SiteResolver resolver = null;
    public static final String MOCK_SITE_URL = "";

    public TemplateDirectiveModel getEmptyTemplateDirectiveModel() {
      return (env, params, loopVars, body) -> {
      };
    }

    @Bean
    public AssetService assetService() {
      return mock(AssetService.class);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
      super.addArgumentResolvers(argumentResolvers);
      argumentResolvers.add(siteResolverDependency());
    }

    @Bean
    public FreeMarkerConfig freeMarkerConfig(ServletContext servletContext) throws IOException {

      String[] templates = getFreeMarkerTemplateDirsForTest();
      List<TemplateLoader> loaders = new ArrayList<TemplateLoader>();
      for (String template : templates) {
        FileTemplateLoader loader = new FileTemplateLoader(new File(servletContext.getRealPath(template)));
        loaders.add(loader);
      }

      MultiTemplateLoader mtl = new MultiTemplateLoader(loaders.toArray(new TemplateLoader[0]));

      FreeMarkerConfigurer config = new FreeMarkerConfigurer();
      config.setPreTemplateLoaders(mtl);

      ImmutableMap.Builder<String, Object> variables = ImmutableMap.builder();
      variables.put("formatJsonDate", new Iso8601DateDirective());
      variables.put("replaceParams", new ReplaceParametersDirective());
      variables.put("siteLink", getEmptyTemplateDirectiveModel());
      variables.put("cssLink", getEmptyTemplateDirectiveModel());
      variables.put("renderCssLinks", getEmptyTemplateDirectiveModel());
      variables.put("js", getEmptyTemplateDirectiveModel());
      variables.put("renderJs", getEmptyTemplateDirectiveModel());
      variables.put("buildInfo", getEmptyTemplateDirectiveModel());
      variables.put("fetchHtml", getEmptyTemplateDirectiveModel());
      variables.put("themeConfig", getEmptyTemplateDirectiveModel());
      variables.put("appLink", getEmptyTemplateDirectiveModel());
      config.setFreemarkerVariables(variables.build());
      return config;
    }

    private String[] getFreeMarkerTemplateDirsForTest() {
      return new String[] { "WEB-INF/themes/root", "WEB-INF/themes/mobile" }; //"WEB-INF/themes/desktop",
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

    /* This type of view resolver will always return a view, so needs to be the last in the chain.
     It has been left unconfigured intentionally simply to allow for MockMvc-based testing
     of controllers that return a logical view name where rendering of an actual template is
     not desired or necessary
     */
    @Bean
    public ViewResolver internalResViewResolver() {
      InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
      return viewResolver;
    }

    @Bean
    public SiteSet siteSetDependency() {

      Site mockSite = mock(Site.class);
      ImmutableSet<Site> sitesSet = new ImmutableSet.Builder<Site>().add(mockSite).build();
      when(mockSite.getJournalKey()).thenReturn("daJournalKey");

      SiteRequestScheme mockRequestScheme = mock(SiteRequestScheme.class);
      when(mockRequestScheme.isForSite(any(HttpServletRequest.class))).thenReturn(true);

      when(mockSite.getRequestScheme()).thenReturn(mockRequestScheme);
      when(mockSite.toString()).thenReturn(MOCK_SITE_URL);

      SiteSet mockSet = mock(SiteSet.class);
      when(mockSet.getSites()).thenReturn(sitesSet);
      return mockSet;
    }

    @Bean
    public synchronized SiteResolver siteResolverDependency() {
      if (resolver == null) {
        resolver = new SiteResolver();
      }
      return resolver;
    }

    @Bean
    public Charset charsetDependency() {
      return Charsets.UTF_8;
    }

    @Bean
    public StaticResourceController staticResourceController() {
      return new StaticResourceController();
    }

    @Bean
    public SiteSet siteSet() {
      SiteRequestScheme mockRequestScheme = mock(SiteRequestScheme.class);
      when(mockRequestScheme.isForSite(any(HttpServletRequest.class))).thenReturn(true);

      Theme theme = new MockTheme();

      Site site = mock(Site.class);
      when(site.getRequestScheme()).thenReturn(mockRequestScheme);
      when(site.getTheme()).thenReturn(theme);

      SiteSet siteSet = mock(SiteSet.class);
      when(siteSet.getSites()).thenReturn(ImmutableSet.of(site));
      when(siteSet.getSite(any(String.class))).thenReturn(site);
      return siteSet;
    }
  }
}
