package org.ambraproject.wombat.controller;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import freemarker.cache.TemplateLoader;
import junit.framework.Assert;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.site.url.SiteRequestScheme;
import org.ambraproject.wombat.config.theme.Theme;
import org.ambraproject.wombat.service.ArticleService;
import org.ambraproject.wombat.service.ArticleTransformService;
import org.ambraproject.wombat.service.TestArticleServiceImpl;
import org.ambraproject.wombat.service.remote.SoaRequest;
import org.ambraproject.wombat.service.remote.SoaService;
import org.apache.http.NameValuePair;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.mockito.Matchers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration
public class ArticleControllerTest extends ControllerTest {

  private static final ArticleTransformService articleTransformService = mock(ArticleTransformService.class);
  private static final String SITE_UNDER_TEST = "DesktopPlosOne";
  private static final ImmutableMap<String, String> SITE_CONFIG = ImmutableMap.<String, String>builder()
          .put("DesktopPlosOne", "PLoSONE")
          .put("DesktopPlosCollections", "PLoSCollections")
          .build();
  private static Map<String, String> MOCK_CONFIG_MAP = new HashMap<>();

  @DataProvider
  public Object[][] collectionIssuesConfig() {
    String articleDoi = "10.1371/journal.pone.0008083";

    Map<String, String> configMap1 = new HashMap<>();
    configMap1.put("DesktopPlosOne", "{'isCollection': false, 'italicizeTitle': false, " +
                    "'otherJournals': {'PLoSCollections': 'DesktopPlosCollections'}}");
    configMap1.put("DesktopPlosCollections", "{'isCollection': true, 'italicizeTitle': false, " +
                    "'otherJournals': {'PLoSONE': 'DesktopPlosOne'}}");

    Map<String, String> configMap2 = new HashMap<>();
    configMap2.put("DesktopPlosOne", "{'isCollection': false, 'italicizeTitle': false, " +
                    "'otherJournals': {'PLoSCollections': 'DesktopPlosCollections'}}");
    configMap2.put("DesktopPlosCollections", "{'isCollection': false, 'italicizeTitle': false, " +
                    "'otherJournals': {'PLoSONE': 'DesktopPlosOne'}}");
    ImmutableMap<String, Object> expectations1 = ImmutableMap.<String, Object>of("issuesCount", 3);
    ImmutableMap<String, Object> expectations2 = ImmutableMap.<String, Object>of("issuesCount", 0);

    return new Object[][]{{articleDoi, configMap1, expectations1},
                          {articleDoi, configMap2, expectations2}
    };
  }


  @Test (dataProvider = "collectionIssuesConfig")
  public void testGetCollectionIssues(String articleDoi, Map<String, String> configMap,
                                      ImmutableMap<String, Object> expectations) throws Exception {

    // TODO: use spring to reset context with each test invocation for iteration of environment config values
    MOCK_CONFIG_MAP = configMap;

    MvcResult result = mockMvc.perform(get("/" + SITE_UNDER_TEST + "/article").param("id", articleDoi))
            .andExpect(handler().handlerType(ArticleController.class))
            .andExpect(handler().methodName("renderArticle"))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("collectionIssues"))
            .andReturn();

    Map<String, Object> collectionIssues = (Map<String,Object>) result.getModelAndView().getModel().get("collectionIssues");
    Assert.assertEquals(collectionIssues.size(), expectations.get("issuesCount"));


  }

  private static class MockTheme extends Theme {

    private MockTheme(String key) {
      super(key, ImmutableList.<Theme>of());
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


  }

  private static final Matcher<SoaRequest> IS_FOR_AUTHORS = new BaseMatcher<SoaRequest>() {
    @Override
    public boolean matches(Object item) {
      SoaRequest request = (SoaRequest) item;
      List<NameValuePair> params = request.getParams();
      for (NameValuePair param : params) {
        if (param.getName().equals("authors")) return true;
      }
      return false;
    }

    @Override
    public void describeTo(Description description) {
    }
  };

  @Configuration
  @EnableWebMvc
  static class TestConfig extends WombatControllerTestConfig {

    @Bean
    public ArticleController articleController() {
      return new ArticleController();
    }

    @Bean
    public SoaService soaService() throws IOException {
      SoaService soaService = mock(SoaService.class);
      when(soaService.requestObject(Matchers.argThat(IS_FOR_AUTHORS), any(Class.class))).thenReturn(ImmutableList.of());
      return soaService;
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