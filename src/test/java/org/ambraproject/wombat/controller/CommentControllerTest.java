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
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import org.ambraproject.wombat.identity.ArticlePointer;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.ambraproject.wombat.service.ArticleResolutionService;
import org.ambraproject.wombat.service.ArticleService;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class CommentControllerTest extends ControllerTest {

  @Configuration
  static class ContextConfiguration {
    @Bean
    CommentController commentController() {
      return new CommentController();
    }

    @Bean
    protected ArticleMetadata.Factory articleMetadataFactory(ArticleMetadata articleMetadata) {
      return spy(new ArticleMetadata.Factory());
    }
  }

  @Autowired
  ArticleMetadata articleMetadata;

  @Autowired
  ArticleResolutionService articleResolutionService;

  @Autowired
  ArticleMetadata.Factory articleMetadataFactory;

  @Autowired
  ArticleService articleService;

  @Autowired
  ArticleApi articleApi;
  
  RequestedDoiVersion expectedRequestedDoi;
  ArticlePointer expectedArticlePointer;

  @Before
  public void setup() throws IOException {
    when(articleMetadata.validateVisibility(anyString())).thenReturn(articleMetadata);
    when(articleMetadata.populate(any(), any())).thenReturn(articleMetadata);
    when(articleMetadata.fillAmendments(any())).thenReturn(articleMetadata);

    doReturn(articleMetadata).when(articleMetadataFactory).newInstance(any(), any(), any(), any(), any(), any());
    expectedRequestedDoi = RequestedDoiVersion.of(EXPECTED_DOI);

    expectedArticlePointer = new ArticlePointer(expectedRequestedDoi, EXPECTED_DOI,
        EXPECTED_INGESTION_NUMBER, expectedRequestedDoi.getRevisionNumber());

    when(articleResolutionService.toIngestion(expectedRequestedDoi)).thenReturn(expectedArticlePointer);

    Map<String, Object> itemResponse = ImmutableMap.of("items", ImmutableMap.of());
    doAnswer(invocation -> {
      return itemResponse;
    }).when(articleService).getItemTable(expectedArticlePointer);

    ImmutableMap<String, String> journal = ImmutableMap.of("journalKey", DESKTOP_PLOS_ONE);
    ImmutableMap<String, Object> ingestionMetadata = ImmutableMap.of("journal", journal);
    ImmutableMap<String, List<Map<String, ?>>> relationships = ImmutableMap.of();

    doReturn(ingestionMetadata, relationships).when(articleApi).requestObject(any(), eq(Map.class));
  }

  private static final String EXPECTED_DOI = "10.1371/journal.pbio.1001091";

  private static final int EXPECTED_INGESTION_NUMBER = 2;

  @After
  public void verifyCalls() throws IOException {
    verify(articleResolutionService).toIngestion(expectedRequestedDoi);
    verify(articleService).getItemTable(expectedArticlePointer);
    verify(articleApi, times(1)).requestObject(any(), eq(Map.class));
    verify(articleApi, times(1)).requestObject(any(), eq(List.class));
  }

  /**
   * Test successful rendering of an article's comments.
   *
   * @throws URISyntaxException if an invalid URI
   * @throws Exception if failed to render
   */
  @Test
  public void testRenderArticleCommentsShouldSucceed() throws URISyntaxException, Exception {
    verifyUriRender(NOSPACE_JOINER.join("/article/comments?id=", EXPECTED_DOI),
                    NOSPACE_JOINER.join(DESKTOP_PLOS_ONE, "/ftl/article/comment/comments"));
  }

  /**
   * Test successful rendering of an article's comment form.
   *
   * @throws URISyntaxException if an invalid URI
   * @throws Exception if failed to render
   */
  @Test
  public void testRenderArticleCommentFormShouldSucceed() throws URISyntaxException, Exception {
    verifyUriRender(NOSPACE_JOINER.join("/article/comments/new?id=", EXPECTED_DOI),
                    NOSPACE_JOINER.join(DESKTOP_PLOS_ONE, "/ftl/article/comment/newComment"));
  }
}
