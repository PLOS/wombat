package org.ambraproject.wombat.controller;

import static org.ambraproject.wombat.util.FileUtils.read;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
    List<Map<String, Object>> map = new Gson().fromJson(read("articleMeta/ppat.1005446.related.json"), ArrayList.class);

    ApiAddress address = ApiAddress.builder("articles").embedDoi(doi).addToken("relationships").build();

    Type t = TypeToken.getParameterized(List.class, Map.class).getType();
    when(articleApi.requestObject(address, t)).thenReturn(map);
    List<RelatedArticle> raList = browseController.fetchRelatedArticles(doi);
    assertEquals(1, raList.size());
    RelatedArticle ra = raList.get(0);
    assertEquals("10.1371/journal.ppat.1006021", ra.getDoi());
  }
}
