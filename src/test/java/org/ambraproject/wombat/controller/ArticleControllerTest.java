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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.ambraproject.wombat.config.SpringMvcConfiguration;
import org.ambraproject.wombat.identity.ArticlePointer;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.ambraproject.wombat.model.Reference;
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
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.ambraproject.wombat.service.remote.CorpusContentApi;
import org.ambraproject.wombat.service.remote.orcid.OrcidApi;
import org.ambraproject.wombat.service.remote.orcid.OrcidApiImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

@ContextConfiguration(
    classes = {SpringMvcConfiguration.class, ArticleController.class, ArticleControllerTest.class})
@Configuration
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class ArticleControllerTest extends ControllerTest {

  private static final String EXPECTED_DOI = "10.1371/journal.pbio.1001091";

  private static final int EXPECTED_INGESTION_NUMBER = 2;

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
  protected ArticleMetadata articleMetadata() throws IOException {
    final ArticleMetadata mockArticleMetadata = mock(ArticleMetadata.class);
    when(mockArticleMetadata.validateVisibility(anyString())).thenReturn(mockArticleMetadata);
    when(mockArticleMetadata.populate(any(), any())).thenReturn(mockArticleMetadata);
    when(mockArticleMetadata.fillAmendments(any())).thenReturn(mockArticleMetadata);
    return mockArticleMetadata;
  }

  @Bean
  protected ArticleMetadata.Factory articleMetadataFactory(ArticleMetadata mockArticleMetadata) {
    final ArticleMetadata.Factory articleMetadataFactory = spy(new ArticleMetadata.Factory());
    doReturn(mockArticleMetadata).when(articleMetadataFactory).newInstance(any(), any(), any(),
        any(), any(), any());
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
    final RequestedDoiVersion expectedRequestedDoi = RequestedDoiVersion.of(EXPECTED_DOI);

    final ArticlePointer expectedArticlePointer =
        new ArticlePointer(
            expectedRequestedDoi, EXPECTED_DOI, EXPECTED_INGESTION_NUMBER,
            expectedRequestedDoi.getRevisionNumber());

    final ArticleResolutionService mockArticleResolutionService = 
        applicationContext.getBean(ArticleResolutionService.class);
    when(mockArticleResolutionService.toIngestion(expectedRequestedDoi))
        .thenReturn(expectedArticlePointer);

    final Map<String, Object> itemResponse = ImmutableMap.of("items", ImmutableMap.of());
    final ArticleService mockArticleService = applicationContext.getBean(ArticleService.class);
    doAnswer(invocation -> {
      return itemResponse;
    }).when(mockArticleService).getItemTable(expectedArticlePointer);

    final ImmutableMap<String, String> journal = ImmutableMap.of("journalKey", DESKTOP_PLOS_ONE);
    final ImmutableMap<String, Object> ingestionMetadata = ImmutableMap.of("journal", journal);
    final ImmutableMap<String, List<Map<String, ?>>> relationships = ImmutableMap.of();

    final ArticleApi mockArticleApi = applicationContext.getBean(ArticleApi.class);
    doReturn(ingestionMetadata, relationships).when(mockArticleApi)
        .requestObject(any(), eq(Map.class));

    final ArticleMetadata mockArticleMetadata = applicationContext.getBean(ArticleMetadata.class);
    when(mockArticleMetadata.getArticlePointer()).thenReturn(expectedArticlePointer);

    final String expectedHtml =
        "<html><title>This is a title</title><body>This is the body</body></html>";
    final Reference reference =
        Reference.build().setTitle("Reference title").setPublisherName("Publisher Name")
            .setAuthors(ImmutableList.of()).setCollabAuthors(ImmutableList.of()).build();
    final ImmutableList<Reference> expectedReferences = ImmutableList.of(reference);
    final ArticleController.XmlContent expectedXmlContent =
        new ArticleController.XmlContent(expectedHtml, expectedReferences);

    final CorpusContentApi mockCorpusContentApi =
        applicationContext.getBean(CorpusContentApi.class);
    when(mockCorpusContentApi.readManuscript(any(), any(), any(), any()))
        .thenReturn(expectedXmlContent);

    final String expectedViewName = NOSPACE_JOINER.join(DESKTOP_PLOS_ONE, "/ftl/article/article");
    final String requestUri = NOSPACE_JOINER.join("/article?id=", EXPECTED_DOI);
    mockMvc.perform(get(new URI(requestUri))).andExpect(status().isOk())
        .andExpect(model().attribute("articleText", expectedHtml))
        .andExpect(model().attribute("references", expectedReferences))
        .andExpect(view().name(expectedViewName));

    verify(mockArticleResolutionService).toIngestion(expectedRequestedDoi);
    verify(mockArticleService).getItemTable(expectedArticlePointer);
    verify(mockArticleApi, times(2)).requestObject(any(), eq(Map.class));
    verify(mockArticleMetadata).getArticlePointer();
    verify(mockCorpusContentApi).readManuscript(any(), any(), any(), any());
  }
}