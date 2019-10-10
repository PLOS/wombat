package org.ambraproject.wombat.controller;

import static org.ambraproject.wombat.util.FileUtils.read;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    RelatedArticle.ArticleMetadata thisArticle = RelatedArticle.ArticleMetadata.builder()
      .setDoi(doi)
      .setTitle(Optional.of("My title"))
      .setRevisionNumber(Optional.of(1))
      .setPublicationDate(Optional.of(LocalDate.of(2019, 10, 10)))
      .build();
    List<RelatedArticle> ra = browseController.fetchRelatedArticles(thisArticle);
    assertEquals(2, ra.size());
    List<RelatedArticle> raDoubleInverted = ra.stream().map(RelatedArticle::invert).map(RelatedArticle::invert).collect(Collectors.toList());
    assertEquals(ra, raDoubleInverted);
  }
}
