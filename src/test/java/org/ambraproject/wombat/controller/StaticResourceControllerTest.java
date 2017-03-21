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
import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;
import freemarker.cache.TemplateLoader;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.site.url.SiteRequestScheme;
import org.ambraproject.wombat.config.theme.Theme;
import org.ambraproject.wombat.service.AssetService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import static java.lang.String.format;
import static org.apache.http.HttpStatus.SC_OK;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration
public class StaticResourceControllerTest extends ControllerTest {

  private static final AssetService assetService = mock(AssetService.class);

  @BeforeMethod
  public void setUp() throws IOException {
    super.setUp();
    reset(assetService);
  }

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
  @Import(WombatControllerTestConfig.class)
  static class TestConfig {

    @Bean
    public StaticResourceController staticResourceController() {
      return new StaticResourceController();
    }

    @Bean
    public AssetService assetService() {
      return assetService;
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
