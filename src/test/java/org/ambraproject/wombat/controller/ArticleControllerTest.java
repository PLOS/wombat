package org.ambraproject.wombat.controller;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.ambraproject.wombat.service.ArticleService;
import org.ambraproject.wombat.service.ArticleTransformService;
import org.ambraproject.wombat.service.remote.CacheDeserializer;
import org.ambraproject.wombat.service.remote.SoaService;
import org.ambraproject.wombat.util.CacheParams;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Charsets.UTF_8;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.entity.ContentType.TEXT_HTML;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.testng.Assert.*;

/**
 * Created by jkrzemien on 7/15/14.
 */

@ContextConfiguration
public class ArticleControllerTest extends ControllerTest {

    private static SoaService soaService = mock(SoaService.class);
    private static ArticleService articleService = mock(ArticleService.class);
    private static ArticleTransformService articleTransformService = mock(ArticleTransformService.class);

    @BeforeMethod
    public void setUp() throws IOException {
        super.setUp();
        reset(soaService, articleService, articleTransformService);
    }

    private Map createTestArticleMetadata() {
        Map journalsMap = new HashMap<String, Object>();
        journalsMap.put("daJournalKey", "something");

        Map articleMetadata = new HashMap<String, Object>();
        articleMetadata.put("state", "published");
        articleMetadata.put("journals", journalsMap);

        articleMetadata.put("title", "daTitle");
        articleMetadata.put("doi", "daDOI");

        return articleMetadata;
    }

    @Test
    public void renderArticleTest() throws Exception {
        /**
         * Define response objects & setup expectations
         */
        Map articleMetadata = createTestArticleMetadata();

        String articleId = "1234";
        when(articleService.requestArticleMetadata(articleId)).thenReturn(articleMetadata);

        CacheParams cacheKey = CacheParams.create("html:" + articleId);
        String xmlAssetPath = "assetfiles/" + articleId + ".xml";
        String responseHTML = "An HTML snippet should come along here...";
        when(soaService.requestCachedStream(eq(cacheKey), eq(xmlAssetPath), any(CacheDeserializer.class))).thenReturn(responseHTML);

        List<?> authors = new ArrayList<Object>();
        when(soaService.requestObject(format("articles/%s?authors", articleId), List.class)).thenReturn(authors);

        List<?> comments = new ArrayList<Object>();
        when(soaService.requestObject(format("articles/%s?comments", articleId), List.class)).thenReturn(comments);

        /**
         * Perform actual invocation of method for class under test - Method I
         */
        MvcResult result = mockMvc.perform(get(format("/article?id=%s", articleId)))
                .andExpect(handler().handlerType(ArticleController.class))
                .andExpect(handler().methodName("renderArticle"))
                .andExpect(status().is(SC_OK))
                .andExpect(forwardedUrl(null))
                .andExpect(redirectedUrl(null))
                .andReturn();

        /**
         * Validations section
         */
        Map<String, Object> model = result.getModelAndView().getModel();
        assertEquals(model.get("article"), articleMetadata);
        assertEquals(model.get("articleText"), responseHTML);
        assertEquals(model.get("amendments"), emptyMap());
        assertEquals(model.get("correspondingAuthors"), emptyList());
        assertEquals(model.get("equalContributors"), emptyList());
        assertEquals(model.get("authors"), authors);
        assertEquals(model.get("articleComments"), comments);

        View view = result.getModelAndView().getView();
        assertNull(view);

        verify(articleService).requestArticleMetadata(articleId);
        verify(soaService).requestCachedStream(eq(cacheKey), eq(xmlAssetPath), any(CacheDeserializer.class));
        verify(soaService).requestObject(format("articles/%s?authors", articleId), List.class);
        verify(soaService).requestObject(format("articles/%s?comments", articleId), List.class);

        verifyZeroInteractions(articleTransformService);
        verifyNoMoreInteractions(articleService, soaService);
    }

    @Test
    public void renderArticleHtmlUnitTest() throws Exception {
        /**
         * Define response objects & setup expectations
         */
        Map articleMetadata = createTestArticleMetadata();

        String articleId = "1234";
        when(articleService.requestArticleMetadata(articleId)).thenReturn(articleMetadata);

        CacheParams cacheKey = CacheParams.create("html:" + articleId);
        String xmlAssetPath = "assetfiles/" + articleId + ".xml";
        String responseHTML = "An HTML snippet should come along here...";
        when(soaService.requestCachedStream(eq(cacheKey), eq(xmlAssetPath), any(CacheDeserializer.class))).thenReturn(responseHTML);

        List<?> authors = new ArrayList<Object>();
        when(soaService.requestObject(format("articles/%s?authors", articleId), List.class)).thenReturn(authors);

        List<?> comments = new ArrayList<Object>();
        when(soaService.requestObject(format("articles/%s?comments", articleId), List.class)).thenReturn(comments);

        /**
         * Perform actual invocation of method for class under test - Method II
         *
         * HTMLUnit needs a CONTEXT prior to the actual namespace to invoke, will be discarded, don't worry...
         * In this example, it is the "/context/" part of the URL...
         */
        HtmlPage page = webClient.getPage("http://localhost/context/article?id=" + articleId);

        /**
         * Validations section
         */
        assertTrue(page.isHtmlPage());
        assertEquals(page.getWebResponse().getStatusCode(), SC_OK);
        assertEquals(page.getWebResponse().getContentType(), TEXT_HTML.getMimeType());
        assertEquals(page.getWebResponse().getContentCharset(), UTF_8.toString());

        assertTrue(page.getTitleText().endsWith(articleMetadata.get("title").toString()));
        assertEquals(page.getElementById("articleText").getTextContent().trim(), responseHTML);
        assertEquals(page.getElementById("article-content").getElementsByTagName("h2").get(0).getTextContent(), articleMetadata.get("title").toString());

        verify(articleService).requestArticleMetadata(articleId);
        verify(soaService).requestCachedStream(eq(cacheKey), eq(xmlAssetPath), any(CacheDeserializer.class));
        verify(soaService).requestObject(format("articles/%s?authors", articleId), List.class);
        verify(soaService).requestObject(format("articles/%s?comments", articleId), List.class);

        verifyZeroInteractions(articleTransformService);
        verifyNoMoreInteractions(articleService, soaService);
    }

    @Test
    public void renderArticleCommentsTest() throws Exception {
        /**
         * Define response objects & setup expectations
         */
        Map articleMetadata = createTestArticleMetadata();

        String articleId = "1234";
        when(articleService.requestArticleMetadata(articleId)).thenReturn(articleMetadata);

        CacheParams cacheKey = CacheParams.create("html:" + articleId);
        String xmlAssetPath = "assetfiles/" + articleId + ".xml";
        String responseHTML = "An HTML snippet should come along here...";
        when(soaService.requestCachedStream(eq(cacheKey), eq(xmlAssetPath), any(CacheDeserializer.class))).thenReturn(responseHTML);

        List<Map> comments = new ArrayList<Map>();
        Map<String, Object> comment = new HashMap<String, Object>();
        comment.put("created", "2014-07-29");
        comment.put("totalNumReplies", 0);
        comment.put("title", "A title!");
        comment.put("creatorDisplayName", "Juan Krzemien");
        comment.put("annotationUri", "I don't know what should go here...");
        comments.add(comment);
        when(soaService.requestObject(format("articles/%s?comments", articleId), List.class)).thenReturn(comments);

        /**
         * Perform actual invocation of method for class under test - Method I
         */
        MvcResult result = mockMvc.perform(get(format("/article/comments?id=%s", articleId)))
                .andExpect(handler().handlerType(ArticleController.class))
                .andExpect(handler().methodName("renderArticleComments"))
                .andExpect(status().is(SC_OK))
                .andExpect(forwardedUrl(null))
                .andExpect(redirectedUrl(null))
                .andReturn();

        /**
         * Validations section
         */
        Map<String, Object> model = result.getModelAndView().getModel();
        assertEquals(model.get("article"), articleMetadata);
        assertEquals(model.get("site").toString(), "");
        assertEquals(model.get("articleComments"), comments);

        View view = result.getModelAndView().getView();
        assertNull(view);

        verify(articleService).requestArticleMetadata(articleId);
        verify(soaService).requestObject(format("articles/%s?comments", articleId), List.class);

        verifyNoMoreInteractions(articleService, soaService);
        verifyZeroInteractions(articleTransformService);
    }

    @Test
    public void renderArticleCommentsHtmlUnirTest() throws Exception {
        /**
         * Define response objects & setup expectations
         */
        Map articleMetadata = createTestArticleMetadata();


        String articleId = "1234";
        when(articleService.requestArticleMetadata(articleId)).thenReturn(articleMetadata);

        CacheParams cacheKey = CacheParams.create("html:" + articleId);
        String xmlAssetPath = "assetfiles/" + articleId + ".xml";
        String responseHTML = "An HTML snippet should come along here...";
        when(soaService.requestCachedStream(eq(cacheKey), eq(xmlAssetPath), any(CacheDeserializer.class))).thenReturn(responseHTML);

        List<Map> comments = new ArrayList<Map>();
        Map<String, Object> comment = new HashMap<String, Object>();
        comment.put("created", "2014-07-29");
        comment.put("totalNumReplies", 0);
        comment.put("title", "A title!");
        comment.put("creatorDisplayName", "Juan Krzemien");
        comment.put("annotationUri", "I don't know what should go here...");
        comments.add(comment);
        when(soaService.requestObject(format("articles/%s?comments", articleId), List.class)).thenReturn(comments);

        /**
         * Perform actual invocation of method for class under test - Method II
         *
         * HTMLUnit needs a CONTEXT prior to the actual namespace to invoke, will be discarded, don't worry...
         * In this example, it is the "/context/" part of the URL...
         */
        HtmlPage page = webClient.getPage("http://localhost/context/article/comments?id=" + articleId);

        /**
         * Validations section
         */
        assertTrue(page.isHtmlPage());
        assertEquals(page.getWebResponse().getStatusCode(), SC_OK);
        assertEquals(page.getWebResponse().getContentType(), TEXT_HTML.getMimeType());
        assertEquals(page.getWebResponse().getContentCharset(), UTF_8.toString());

        assertEquals(page.getTitleText(), "An Ambra-Hosted Site: PLOS - Comments");

        verify(articleService).requestArticleMetadata(articleId);
        verify(soaService).requestObject(format("articles/%s?comments", articleId), List.class);

        verifyNoMoreInteractions(articleService, soaService);
        verifyZeroInteractions(articleTransformService);

    }

    /**
     * Spring container configuration for this test.
     * I don't like having multiple XML Spring files (per test) nor having a single huge one for all tests.
     * I think it communicates better if each test suite has it's own Spring config embedded in it as Java code.
     */
    @Configuration
    @EnableWebMvc
    static class TestConfig extends WombatControllerTestConfig {

        @Bean
        public SoaService soaServiceDependency() {
            return soaService;
        }

        @Bean
        public ArticleService articleServiceDependency() {
            return articleService;
        }

        @Bean
        public ArticleTransformService articleTransformServiceDependency() {
            return articleTransformService;
        }

        @Bean
        public ArticleController classUnderTest() {
            return new ArticleController();
        }

    }

}

