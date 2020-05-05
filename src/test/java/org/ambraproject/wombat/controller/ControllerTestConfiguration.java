package org.ambraproject.wombat.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import javax.servlet.http.HttpServletRequest;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.ambraproject.wombat.config.TestRuntimeConfiguration;
import org.ambraproject.wombat.config.site.RequestMappingContextDictionary;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteResolver;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.site.url.SiteRequestScheme;
import org.ambraproject.wombat.config.theme.Theme;
import org.ambraproject.wombat.service.ArticleResolutionService;
import org.ambraproject.wombat.service.ArticleService;
import org.ambraproject.wombat.service.ArticleTransformService;
import org.ambraproject.wombat.service.BrowseTaxonomyService;
import org.ambraproject.wombat.service.CommentService;
import org.ambraproject.wombat.service.CommentValidationService;
import org.ambraproject.wombat.service.DoiToJournalResolutionService;
import org.ambraproject.wombat.service.HoneypotService;
import org.ambraproject.wombat.service.ParseXmlService;
import org.ambraproject.wombat.service.PeerReviewService;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.ambraproject.wombat.service.remote.ArticleApiImpl;
import org.ambraproject.wombat.service.remote.CachedRemoteService;
import org.ambraproject.wombat.service.remote.CorpusContentApi;
import org.ambraproject.wombat.service.remote.JsonService;
import org.ambraproject.wombat.service.remote.SolrSearchApi;
import org.ambraproject.wombat.service.remote.SolrSearchApi;
import org.ambraproject.wombat.service.remote.UserApi;
import org.ambraproject.wombat.service.remote.orcid.OrcidApi;
import org.ambraproject.wombat.util.ThemeTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.web.WebAppConfiguration;

@WebAppConfiguration
public class ControllerTestConfiguration {
  private static final RuntimeConfiguration runtimeConfiguration = new TestRuntimeConfiguration();

  protected static final String DESKTOP_PLOS_ONE = "DesktopPlosOne";

  @Bean
  protected Gson gson() {
    return RuntimeConfiguration.makeGson();
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
  protected CommentValidationService commentValidationService() {
    return mock(CommentValidationService.class);
  }

  @Bean
  protected CommentService commentService() {
    return mock(CommentService.class);
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
    final SolrSearchApi solrSearchApi = spy(SolrSearchApi.class);
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
    final ArticleApi articleApi = mock(ArticleApiImpl.class);
    return articleApi;
  }

  @Bean
  protected ArticleTransformService articleTransformService() {
    final ArticleTransformService articleTransformService = mock(ArticleTransformService.class);
    return articleTransformService;
  }

  @Bean
  protected Theme activeTheme() {
    final Theme theme = mock(ThemeTest.class);
    return theme;
  }

  /**
   *  Unit test can only work with a single site.
   */
  @Bean
  protected Site activeSite(Theme theme) {
    final SiteRequestScheme mockRequestScheme = mock(SiteRequestScheme.class);
    doAnswer(invocation -> {
      final Object[] args = invocation.getArguments();
      final HttpServletRequest request = (HttpServletRequest) args[0];
      return true;
    }).when(mockRequestScheme).isForSite(any(HttpServletRequest.class));


    final Site mockSite = mock(Site.class);
    when(mockSite.getRequestScheme()).thenReturn(mockRequestScheme);
    when(mockSite.getTheme()).thenReturn(theme);
    when(mockSite.getKey()).thenReturn(DESKTOP_PLOS_ONE);
    when(mockSite.toString()).thenReturn(DESKTOP_PLOS_ONE);
    when(mockSite.getJournalKey()).thenReturn(DESKTOP_PLOS_ONE);
    return mockSite;
  }

  @Bean
  protected SiteSet siteSet(Site site) {
    final SiteSet siteSet = mock(SiteSet.class);
    when(siteSet.getSites()).thenReturn(ImmutableSet.of(site));
    return siteSet;
  }
  
  @Bean
  protected ArticleService articleService() {
    return mock(ArticleService.class);
  }

  @Bean
  protected CorpusContentApi corpusContentApi() {
    return mock(CorpusContentApi.class);
  }

  @Bean
  protected PeerReviewService peerReviewService() {
    return mock(PeerReviewService.class);
  }

  @Bean
  protected ArticleResolutionService articleResolutionService() {
    return mock(ArticleResolutionService.class);
  }
  
  @Bean
  protected ArticleMetadata articleMetadata() {
    return mock(ArticleMetadata.class);
  }
  
  @Bean
  protected ArticleMetadata.Factory articleMetadataFactory(ArticleMetadata mockArticleMetadata) {
    return mock(ArticleMetadata.Factory.class);
  }

  @Bean
  public BrowseTaxonomyService browseTaxonomyService() {
    return mock(BrowseTaxonomyService.class);
  }
  
  @Bean
  protected ParseXmlService parseXmlService() {
    return mock(ParseXmlService.class);
  }

  @Bean
  protected OrcidApi orcidApi() {
    return mock(OrcidApi.class);
  }
  
  @Bean
  protected DoiToJournalResolutionService doiToJournalResolutionService() {
    return mock(DoiToJournalResolutionService.class);
  }
}
