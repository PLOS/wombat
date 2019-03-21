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

import com.google.common.collect.ImmutableMap;
import org.ambraproject.wombat.identity.ArticlePointer;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.ambraproject.wombat.service.*;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.ambraproject.wombat.service.remote.CorpusContentApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ContextConfiguration(
    classes = {CommentController.class, CommentControllerTest.class})
@Configuration
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class CommentControllerTest extends ControllerTest {

  private static final String EXPECTED_DOI = "10.1371/journal.pbio.1001091";

  private static final int EXPECTED_INGESTION_NUMBER = 2;

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
  protected PeerReviewService peerReviewService() {
    final PeerReviewService peerReviewService = mock(PeerReviewService.class);
    return peerReviewService;
  }

  @Bean
  protected ArticleResolutionService articleResolutionService() {
    final ArticleResolutionService articleResolutionService = mock(ArticleResolutionService.class);
    return articleResolutionService;
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


  /**
   * Test successful rendering of an article's comments.
   *
   * @throws URISyntaxException if an invalid URI
   * @throws Exception if failed to render
   */
  @Test
  public void testRenderArticleCommentsShouldSucceed() throws URISyntaxException, Exception {
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

    final String expectedViewName = NOSPACE_JOINER.join(DESKTOP_PLOS_ONE, "/ftl/article/comment/comments");
    final String requestUri = NOSPACE_JOINER.join("/article/comments?id=", EXPECTED_DOI);
    mockMvc.perform(get(new URI(requestUri))).andExpect(status().isOk())
        .andExpect(view().name(expectedViewName));

    verify(mockArticleResolutionService).toIngestion(expectedRequestedDoi);
    verify(mockArticleService).getItemTable(expectedArticlePointer);
    verify(mockArticleApi, times(2)).requestObject(any(), eq(Map.class));
  }

  /**
   * Test successful rendering of an article's comment form.
   *
   * @throws URISyntaxException if an invalid URI
   * @throws Exception if failed to render
   */
  @Test
  public void testRenderArticleCommentFormShouldSucceed() throws URISyntaxException, Exception {
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

    final String expectedViewName = NOSPACE_JOINER.join(DESKTOP_PLOS_ONE, "/ftl/article/comment/newComment");
    final String requestUri = NOSPACE_JOINER.join("/article/comments/new?id=", EXPECTED_DOI);
    mockMvc.perform(get(new URI(requestUri))).andExpect(status().isOk())
        .andExpect(view().name(expectedViewName));

    verify(mockArticleResolutionService).toIngestion(expectedRequestedDoi);
    verify(mockArticleService).getItemTable(expectedArticlePointer);
    verify(mockArticleApi, times(2)).requestObject(any(), eq(Map.class));
  }
}
