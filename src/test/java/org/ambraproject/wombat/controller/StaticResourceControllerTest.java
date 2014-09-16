package org.ambraproject.wombat.controller;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.net.HttpHeaders;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.site.url.SiteRequestScheme;
import org.ambraproject.wombat.config.theme.Theme;
import org.ambraproject.wombat.service.AssetService;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;

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

  @Configuration
  @EnableWebMvc
  static class TestConfig extends WombatControllerTestConfig {

    @Bean
    public StaticResourceController staticResourceController() {
      return new StaticResourceController();
    }

    @Bean
    public AssetService assetService() {
      return assetService;
    }

    @Override
    @Bean
    public SiteSet siteSetDependency() {
      SiteRequestScheme mockRequestScheme = mock(SiteRequestScheme.class);
      when(mockRequestScheme.isForSite(any(HttpServletRequest.class))).thenReturn(true);

      Theme theme = mock(Theme.class);
      try {
        // Mock out static assets served by the theme
        when(theme.getStaticResource(any(String.class))).thenAnswer(new Answer<Object>() {
          @Override
          public Object answer(InvocationOnMock invocation) throws Throwable {
            // Use as the response body a special value that can identify the input path
            String path = (String) invocation.getArguments()[0];
            return new ByteArrayInputStream(getMockResource(path).getBytes());
          }
        });
        when(theme.getResourceAttributes(any(String.class))).thenReturn(new Theme.ResourceAttributes() {
          @Override
          public long getLastModified() {
            return Long.MAX_VALUE; // always modified
          }

          @Override
          public long getContentLength() {
            return 0; // dummy
          }
        });

        // Mock out the configuration that controls whether to put CORS headers
        when(theme.getConfigMap("resource")).thenReturn(ImmutableMap.<String, Object>builder()
            .put("cors", ImmutableList.of("allow/"))
            .build());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

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
