package org.ambraproject.wombat.controller;

import static org.ambraproject.wombat.util.FileUtils.read;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import org.ambraproject.wombat.model.RelatedArticle;
import org.ambraproject.wombat.service.remote.ApiAddress;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration
public class BrowseControllerTest extends ControllerTest {
  @Configuration
  static class ContextConfiguration {
    @Bean
    protected BrowseController browseController() {
      return new BrowseController();
    }

    @Bean
    protected ArticleApi articleApi() {
      return mock(ArticleApi.class);
    }
  }

  @Autowired
  BrowseController browseController;

  @Autowired
  ArticleApi articleApi;

  @Test
  public void testFetchRelatedArticles() throws Exception {
    String doi = "10.9999/journal.xxxx.0";
    Map<String, Object> map = new Gson().fromJson(read("articleMeta/ppat.1005446.related.json"), HashMap.class);

    ApiAddress address = ApiAddress.builder("articles").embedDoi(doi).addToken("relationships").build();

    when(articleApi.requestObject(address, Map.class)).thenReturn(map);
    List<RelatedArticle> ra = browseController.fetchRelatedArticles(doi);
    assertEquals(2, ra.size());
  }
}
