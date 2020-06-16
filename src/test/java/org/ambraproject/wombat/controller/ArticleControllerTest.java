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
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.ambraproject.wombat.identity.ArticlePointer;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.ambraproject.wombat.model.Reference;
import org.ambraproject.wombat.model.RelatedArticle;
import org.ambraproject.wombat.service.ArticleResolutionService;
import org.ambraproject.wombat.service.ArticleService;
import org.ambraproject.wombat.service.ParseXmlService;
import org.ambraproject.wombat.service.remote.ApiAddress;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.ambraproject.wombat.service.remote.CorpusContentApi;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration
public class ArticleControllerTest extends ControllerTest {
  @Configuration
  static class ContextConfiguration {
    @Bean
    protected ArticleController articleController() {
      return new ArticleController();
    }
    
    @Bean
    protected ArticleMetadata.Factory articleMetadataFactory(ArticleMetadata articleMetadata) {
      return spy(new ArticleMetadata.Factory());
    }
  }
  
  private static String EXPECTED_DOI = "10.1371/journal.pbio.1001091";

  private static int EXPECTED_INGESTION_NUMBER = 2;

  @Autowired
  ArticleController articleController;

  @Autowired
  ArticleResolutionService articleResolutionService;

  @Autowired
  ArticleService articleService;

  @Autowired
  ArticleApi articleApi;

  @Autowired
  ArticleMetadata articleMetadata;
  
  @Autowired
  CorpusContentApi corpusContentApi;

  @Autowired
  ArticleMetadata.Factory articleMetadataFactory;
  
  /**
   * Test successful rendering of an article.
   *
   * @throws URISyntaxException if an invalid URI
   * @throws Exception if failed to render article
   */
  @Test
  public void testRenderArticleShouldSucceed() throws URISyntaxException, Exception {
    when(articleMetadata.validateVisibility(anyString())).thenReturn(articleMetadata);
    when(articleMetadata.populate(any(), any())).thenReturn(articleMetadata);
    when(articleMetadata.fillAmendments(any())).thenReturn(articleMetadata);

    doReturn(articleMetadata).when(articleMetadataFactory)
      .newInstance(any(), any(), any(), any(), any(), any());

    RequestedDoiVersion expectedRequestedDoi = RequestedDoiVersion.of(EXPECTED_DOI);

    ArticlePointer expectedArticlePointer =
        new ArticlePointer(
            expectedRequestedDoi, EXPECTED_DOI, EXPECTED_INGESTION_NUMBER,
            expectedRequestedDoi.getRevisionNumber());

    when(articleResolutionService.toIngestion(expectedRequestedDoi))
        .thenReturn(expectedArticlePointer);

    Map<String, Object> itemResponse = ImmutableMap.of("items", ImmutableMap.of());
    doAnswer(invocation -> {
      return itemResponse;
    }).when(articleService).getItemTable(expectedArticlePointer);

    ImmutableMap<String, String> journal = ImmutableMap.of("journalKey", DESKTOP_PLOS_ONE);
    ImmutableMap<String, Object> ingestionMetadata = ImmutableMap.of("journal", journal);

    ApiAddress address = ApiAddress.builder("articles").embedDoi(EXPECTED_DOI).addToken("relationships").build();
    when(articleApi.requestObject(address, ArticleMetadata.Factory.RELATED_ARTICLE_GSON_TYPE))
        .thenReturn(ImmutableList.<RelatedArticle>of());

    when(articleApi.requestObject(any(), eq(Map.class))).thenReturn(ingestionMetadata);

    when(articleMetadata.getArticlePointer()).thenReturn(expectedArticlePointer);

    String expectedHtml =
        "<html><title>This is a title</title><body>This is the body</body></html>";
    Reference reference =
        Reference.build().setTitle("Reference title").setPublisherName("Publisher Name")
            .setAuthors(ImmutableList.of()).setCollabAuthors(ImmutableList.of()).build();
    ImmutableList<Reference> expectedReferences = ImmutableList.of(reference);
    final ParseXmlService.XmlContent expectedXmlContent =
        new ParseXmlService.XmlContent(expectedHtml, expectedReferences);

    final ParseXmlService mockParseXmlService = applicationContext.getBean(ParseXmlService.class);
    when(mockParseXmlService.getXmlContent(any(), any(), any())).thenReturn(expectedXmlContent);

    String expectedViewName = NOSPACE_JOINER.join(DESKTOP_PLOS_ONE, "/ftl/article/article");
    String requestUri = NOSPACE_JOINER.join("/article?id=", EXPECTED_DOI);
    mockMvc.perform(get(new URI(requestUri))).andExpect(status().isOk())
        .andExpect(model().attribute("articleText", expectedHtml))
        .andExpect(model().attribute("references", expectedReferences))
        .andExpect(view().name(expectedViewName));

    verify(articleResolutionService).toIngestion(expectedRequestedDoi);
    verify(articleService).getItemTable(expectedArticlePointer);
    verify(articleApi, times(1)).requestObject(any(), eq(Map.class));
    verify(articleMetadata).getArticlePointer();
  }
}
