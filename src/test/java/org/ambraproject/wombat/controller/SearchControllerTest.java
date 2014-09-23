package org.ambraproject.wombat.controller;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import freemarker.cache.TemplateLoader;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.site.url.SiteRequestScheme;
import org.ambraproject.wombat.config.theme.Theme;
import org.ambraproject.wombat.service.remote.SearchService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import static org.apache.http.HttpStatus.SC_MOVED_TEMPORARILY;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@ContextConfiguration
public class SearchControllerTest extends ControllerTest {

  private static final SearchService searchService = mock(SearchService.class);
  private static final String TEST_SITE = "plosbiology"; // TODO: iterate over 6 journals?
  private static final String TEST_SITE_JOURNAL_KEY = "PLoSBiology";

  private static final String LEGACY_URL_PREFIX = String.format("http://www.%s.org/",TEST_SITE);
  private static final String LEGACY_SEARCH_PATTERN = "search/simple?from=globalSimpleSearch&filterJournals={journalKey}&query={query}";

  @BeforeMethod
  public void setUp() throws IOException {
    super.setUp();
    reset(searchService);
  }

  private static String getMockConfigMap() {
    return String.format("{'legacyPattern':'%s', 'urlPrefix':'%s'}", LEGACY_SEARCH_PATTERN, LEGACY_URL_PREFIX);
  }


  @Test
  public void testLegacyRedirect() throws Exception {

    // test query from Wombat homepage which should redirect to Ambra (a.k.a. legacy search)
    String escapedQueryString = "testquery";
    String redirectUrl = LEGACY_URL_PREFIX + LEGACY_SEARCH_PATTERN
            .replace("{query}", escapedQueryString)
            .replace("{journalKey}", TEST_SITE_JOURNAL_KEY);

    mockMvc.perform(get("/search").param("legacy","true").param("q", escapedQueryString))
            .andExpect(handler().handlerType(SearchController.class))
            .andExpect(handler().methodName("search"))
            .andExpect(status().is(SC_MOVED_TEMPORARILY))
            .andExpect(forwardedUrl(null))
            .andExpect(redirectedUrl(redirectUrl))
            .andReturn();

    // test for missing query param which should be replaced with empty param to ensure similar legacy redirect
    escapedQueryString = "";
    redirectUrl = LEGACY_URL_PREFIX + LEGACY_SEARCH_PATTERN
            .replace("{query}", escapedQueryString)
            .replace("{journalKey}", TEST_SITE_JOURNAL_KEY);

    mockMvc.perform(get("/search").param("legacy","true"))
            .andExpect(handler().handlerType(SearchController.class))
            .andExpect(handler().methodName("search"))
            .andExpect(status().is(SC_MOVED_TEMPORARILY))
            .andExpect(forwardedUrl(null))
            .andExpect(redirectedUrl(redirectUrl))
            .andReturn();
  }

  private static class MockTheme extends Theme {
    private MockTheme() {
      super("mockTheme", ImmutableList.<Theme>of());
    }

    @Override
    protected InputStream fetchStaticResource(String path) throws IOException {
      String response;
      if (path.equals(("config/search.json")) || path.equals(("config/legacy.json"))) {
        response = getMockConfigMap();
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


  }

  @Configuration
  @EnableWebMvc
  static class TestConfig extends WombatControllerTestConfig {

    @Bean
    public SearchController searchController() {
      return new SearchController();
    }

    @Bean
    public SearchService searchService() {
      return searchService;
    }

    @Override
    @Bean
    public SiteSet siteSetDependency() {
      SiteRequestScheme mockRequestScheme = mock(SiteRequestScheme.class);
      when(mockRequestScheme.isForSite(any(HttpServletRequest.class))).thenReturn(true);

      Theme theme = new MockTheme();

      Site site = mock(Site.class);
      when(site.getRequestScheme()).thenReturn(mockRequestScheme);
      when(site.getTheme()).thenReturn(theme);

      when(site.getJournalKey()).thenReturn(TEST_SITE_JOURNAL_KEY);
      SiteSet siteSet = mock(SiteSet.class);
      when(siteSet.getSites()).thenReturn(ImmutableSet.of(site));
      when(siteSet.getSite(any(String.class))).thenReturn(site);
      return siteSet;
    }
  }
}