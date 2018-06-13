package org.ambraproject.wombat.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.net.URISyntaxException;

import org.ambraproject.wombat.config.SpringMvcConfiguration;
import org.ambraproject.wombat.service.ArticleResolutionService;
import org.ambraproject.wombat.service.ArticleService;
import org.ambraproject.wombat.service.ArticleServiceImpl;
import org.ambraproject.wombat.service.CitationDownloadService;
import org.ambraproject.wombat.service.CommentService;
import org.ambraproject.wombat.service.CommentValidationService;
import org.ambraproject.wombat.service.DoiToJournalResolutionService;
import org.ambraproject.wombat.service.ParseReferenceService;
import org.ambraproject.wombat.service.ParseXmlService;
import org.ambraproject.wombat.service.ParseXmlServiceImpl;
import org.ambraproject.wombat.service.remote.CorpusContentApi;
import org.ambraproject.wombat.service.remote.orcid.OrcidApi;
import org.ambraproject.wombat.service.remote.orcid.OrcidApiImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(
    classes = {ArticleController.class, NewArticleControllerTest.class, SpringMvcConfiguration.class})
@Configuration
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class NewArticleControllerTest extends ControllerTest {

  @Bean
  protected ArticleResolutionService articleResolutionService() {
    final ArticleResolutionService articleResolutionService = mock(ArticleResolutionService.class);
    return articleResolutionService;
  }

  @Bean
  protected ArticleService articleService() {
    final ArticleService articleService = spy(ArticleServiceImpl.class);
    return articleService;
  }

  @Bean
  protected ArticleMetadata.Factory articleMetadataFactory() {
    final ArticleMetadata.Factory articleMetadataFactory = new ArticleMetadata.Factory();
    return articleMetadataFactory;
  }

  @Bean
  protected CorpusContentApi corpusContentApi() {
    final CorpusContentApi corpusContentApi = mock(CorpusContentApi.class);
    return corpusContentApi;
  }

  @Bean
  protected CitationDownloadService citationDownloadService() {
    final CitationDownloadService citationDownloadService = mock(CitationDownloadService.class);
    return citationDownloadService;
  }

  @Bean
  protected CommentValidationService commentValidationService() {
    final CommentValidationService commentValidationService = mock(CommentValidationService.class);
    return commentValidationService;
  }

  @Bean
  protected CommentService commentService() {
    final CommentService commentService = mock(CommentService.class);
    return commentService;
  }

  @Bean
  protected ParseReferenceService parseReferenceService() {
    final ParseReferenceService parseReferenceService = new ParseReferenceService();
    return parseReferenceService;
  }

  @Bean
  protected ParseXmlService parseXmlService() {
    final ParseXmlService parseXmlService = spy(ParseXmlServiceImpl.class);
    return parseXmlService;
  }

  @Bean
  protected DoiToJournalResolutionService doiToJournalResolutionService() {
    final DoiToJournalResolutionService doiToJournalResolutionService =
        spy(DoiToJournalResolutionService.class);
    return doiToJournalResolutionService;
  }

  @Bean
  protected OrcidApi orcidApi() {
    final OrcidApi orcidApi = spy(OrcidApiImpl.class);
    return orcidApi;
  }

  /**
   * Test successful rendering of an article.
   *
   * @throws URISyntaxException if an invalid URI
   * @throws Exception if failed to render article
   */
  @Test
  public void testRenderArticleShouldSuceed() throws URISyntaxException, Exception {
    mockMvc.perform(get(new URI("/DesktopPlosOne/article?id=10.1371/journal.pbio.1001091")))
        .andExpect(status().isOk());
  }
}
