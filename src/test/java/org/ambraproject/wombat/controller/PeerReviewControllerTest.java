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

import org.ambraproject.wombat.config.SpringMvcConfiguration;
import org.ambraproject.wombat.service.ArticleResolutionService;
import org.ambraproject.wombat.service.ArticleService;
import org.ambraproject.wombat.service.ArticleServiceImpl;
import org.ambraproject.wombat.service.PeerReviewService;
import org.ambraproject.wombat.service.remote.CorpusContentApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ContextConfiguration(
    classes = {SpringMvcConfiguration.class, PeerReviewController.class, PeerReviewControllerTest.class})
@Configuration
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class PeerReviewControllerTest extends ControllerTest {
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

  @Test
  public void testPeerReviewNotFound(){
    PeerReviewController controller = new PeerReviewController();
    Map<String, Object> map = new HashMap<String,Object>();

    try {
      controller.throwIfPeerReviewNotFound(map);
    } catch (NotFoundException e) {
      return;
    }
    fail("should have thrown a NotFound exception");
  }

  @Test
  public void testPeerReviewFound(){
    PeerReviewController controller = new PeerReviewController();
    Map<String, Object> map = new HashMap<String,Object>();

    map.put("peerReview", "foo");
    controller.throwIfPeerReviewNotFound(map);
  }
}
