package org.ambraproject.wombat.controller;

import static org.ambraproject.wombat.util.FileUtils.read;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import javax.servlet.http.HttpServletRequest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;

import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.url.Link;
import org.ambraproject.wombat.config.site.url.SiteRequestScheme;
import org.ambraproject.wombat.config.theme.Theme;
import org.ambraproject.wombat.identity.ArticlePointer;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.ambraproject.wombat.model.RelatedArticle;
import org.ambraproject.wombat.model.RelatedArticleType;
import org.ambraproject.wombat.service.remote.ApiAddress;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.ui.Model;

@ContextConfiguration(classes = {ArticleMetadataTest.class})
public class ArticleMetadataTest extends AbstractJUnit4SpringContextTests {

  @InjectMocks
  public ArticleMetadata.Factory articleMetadataFactory;

  @Autowired
  ArticleApi articleApi;
  
  @Bean
  protected ArticleApi articleApi() {
    return mock(ArticleApi.class);
  }

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testPopulate() throws IOException {
    List<RelatedArticle> relations = ImmutableList.of();

    ArticleMetadata articleMetadata = spy(articleMetadataFactory.newInstance(mock(Site.class),
        mock(RequestedDoiVersion.class), mock(ArticlePointer.class), new HashMap(), new HashMap(), relations));
    doReturn(null).when(articleMetadata).getFigureView();
    doReturn(null).when(articleMetadata).getArticleType();
    doReturn(null).when(articleMetadata).getCommentCount();
    doReturn(null).when(articleMetadata).getContainingArticleLists();
    doReturn(null).when(articleMetadata).getCategoryTerms();
    doReturn(null).when(articleMetadata).getRevisionMenu();
    doReturn(null).when(articleMetadata).getPeerReviewHtml();
    doNothing().when(articleMetadata).populateAuthors(any());

    articleMetadata.populate(mock(HttpServletRequest.class), mock(Model.class));
    verify(articleMetadata).getFigureView();
    verify(articleMetadata).getArticleType();
    verify(articleMetadata).getCommentCount();
    verify(articleMetadata).getContainingArticleLists();
    verify(articleMetadata).getCategoryTerms();
    verify(articleMetadata).getRelatedArticles();
    verify(articleMetadata).getRevisionMenu();
    verify(articleMetadata).getPeerReviewHtml();
    verify(articleMetadata).populateAuthors(any());
  }

  @Test
  public void testGetRelatedArticlesSortInReversePublicationOrder() throws IOException {
    RelatedArticle rel1 = mock(RelatedArticle.class);
    RelatedArticle rel2 = mock(RelatedArticle.class);
    when(rel1.isPublished()).thenReturn(true);
    when(rel1.getPublicationDate()).thenReturn(LocalDate.of(2018, 1, 1));
    when(rel2.isPublished()).thenReturn(true);
    when(rel2.getPublicationDate()).thenReturn(LocalDate.of(2019, 1, 1));
    List<RelatedArticle> relations = ImmutableList.of(rel1, rel2);
    ArticleMetadata articleMetadata = articleMetadataFactory.newInstance(mock(Site.class),
        mock(RequestedDoiVersion.class), mock(ArticlePointer.class), new HashMap(), new HashMap(), relations);
    assertEquals(ImmutableList.of(rel2, rel1), articleMetadata.getRelatedArticles());
  }

  @Test
  public void testGetRelatedArticleByType() throws IOException {
    RelatedArticle rel1 = mock(RelatedArticle.class);
    RelatedArticle rel2 = mock(RelatedArticle.class);
    RelatedArticleType type1 = RelatedArticleType.get("retracted-article");
    RelatedArticleType type2 = RelatedArticleType.get("retraction-forward");
    when(rel1.isPublished()).thenReturn(true);
    when(rel1.getPublicationDate()).thenReturn(LocalDate.of(2018, 1, 1));
    when(rel1.getType()).thenReturn(type1);
    when(rel2.isPublished()).thenReturn(true);
    when(rel2.getPublicationDate()).thenReturn(LocalDate.of(2019, 1, 1));
    when(rel2.getType()).thenReturn(type2);
    List<RelatedArticle> relations = ImmutableList.of(rel1, rel2);
    ArticleMetadata articleMetadata = articleMetadataFactory
      .newInstance(mock(Site.class),
                   mock(RequestedDoiVersion.class),
                   mock(ArticlePointer.class),
                   new HashMap(),
                   new HashMap(),
            relations);
    SortedMap<RelatedArticleType, List<RelatedArticle>> map =
      articleMetadata.getRelatedArticlesByType();
    assertEquals(ImmutableSet.of(type1, type2), map.keySet());
  }

  @Test
  public void testValidateVisibility() {
    HashMap<String, String> journalMetadata = new HashMap<>();
    journalMetadata.put("journalKey", "fakeKey");
    HashMap<String, HashMap<String, String>> ingestionMetadata = new HashMap<>();
    ingestionMetadata.put("journal", journalMetadata);

    Theme theme = mock(Theme.class);
    HashMap<String, Object> journalAttrs = new HashMap<>();
    journalAttrs.put("journalKey", "fakeKey");
    journalAttrs.put("journalName", "fakeName");
    when(theme.getConfigMap(any())).thenReturn(journalAttrs);

    Site site = new Site("foo", theme, mock(SiteRequestScheme.class), "foo");
    ArticleMetadata articleMetadata = articleMetadataFactory.newInstance(site,
        mock(RequestedDoiVersion.class),
        mock(ArticlePointer.class),
        ingestionMetadata,
        new HashMap(),
        new ArrayList());
    
    articleMetadata.validateVisibility("whatever");
  }

  @Test(expected = InternalRedirectException.class)
  public void testValidateVisibilityFail() {
    HashMap<String, String> journalMetadata = new HashMap<>();
    journalMetadata.put("journalKey", "someKey");
    HashMap<String, HashMap<String, String>> ingestionMetadata = new HashMap<>();
    ingestionMetadata.put("journal", journalMetadata);

    Theme theme = mock(Theme.class);
    HashMap<String, Object> journalAttrs = new HashMap<>();
    journalAttrs.put("journalKey", "fakeKey");
    journalAttrs.put("journalName", "fakeName");
    when(theme.getConfigMap(any())).thenReturn(journalAttrs);

    Site site = new Site("foo", theme, mock(SiteRequestScheme.class), "foo");
    when(theme.resolveForeignJournalKey(any(), any())).thenReturn(site);

    ArticleMetadata articleMetadata = spy(articleMetadataFactory.newInstance(site,
        mock(RequestedDoiVersion.class),
        mock(ArticlePointer.class),
        ingestionMetadata,
        new HashMap(),
        new ArrayList()));

    Link mockLink = mock(Link.class);
    doReturn(mockLink).when(articleMetadata).buildCrossSiteRedirect(any(), any());
    articleMetadata.validateVisibility("whatever");
  }

  @Test
  public void testGetFigureView() {
    Map<String, String> asset = new HashMap<>();
    asset.put("doi", "fakeDoi");
    List<Map<String, String>> assets = new ArrayList<>();
    assets.add(asset);
    HashMap<String, List<Map<String, String>>> ingestionMetadata = new HashMap<>();
    ingestionMetadata.put("assetsLinkedFromManuscript", assets);

    Map<String, String> item = new HashMap<>();
    item.put("itemType", "figure");
    Map<String, Map<String, String>> itemTable = new HashMap<>();
    itemTable.put("fakeDoi", item);

    Theme theme = mock(Theme.class);
    HashMap<String, Object> journalAttrs = new HashMap<>();
    journalAttrs.put("journalKey", "fakeKey");
    journalAttrs.put("journalName", "fakeName");
    when(theme.getConfigMap(any())).thenReturn(journalAttrs);

    Site site = new Site("foo", theme, mock(SiteRequestScheme.class), "foo");
    ArticleMetadata articleMetadata = articleMetadataFactory.newInstance(site,
        mock(RequestedDoiVersion.class),
        mock(ArticlePointer.class),
        ingestionMetadata,
        itemTable,
        new ArrayList());

    HashMap<String, String> expected = new HashMap<>();
    expected.put("type", "figure");
    expected.put("doi", "fakeDoi");
    List<Map<String, ?>> expectedFigureView = new ArrayList<>();
    expectedFigureView.add(expected);
    assertEquals(articleMetadata.getFigureView(), expectedFigureView);
  }

  @Test
  public void testFetchRelatedArticles() throws Exception {
    String doi = "10.9999/journal.xxxx.0";
    List<RelatedArticle> map = new Gson().fromJson(read("articleMeta/ppat.1005446.related.json"),
        ArticleMetadata.Factory.RELATED_ARTICLE_GSON_TYPE);

    ApiAddress address =
        ApiAddress.builder("articles").embedDoi(doi).addToken("relationships").build();

    when(articleApi.requestObject(address, ArticleMetadata.Factory.RELATED_ARTICLE_GSON_TYPE))
        .thenReturn(map);
    List<RelatedArticle> raList = articleMetadataFactory.fetchRelatedArticles(doi);
    assertEquals(1, raList.size());
    RelatedArticle ra = raList.get(0);
    assertEquals("10.1371/journal.ppat.1006021", ra.getDoi());
    assertEquals(null, ra.getSpecificUse());
  }

  @Test
  public void testFetchRelatedArticlesWithSpecificUse() throws Exception {
    String doi = "10.9999/journal.xxxx.0";
    List<RelatedArticle> map = new Gson().fromJson(read("articleMeta/ppat.1005446.related2.json"),
                                                   ArticleMetadata.Factory.RELATED_ARTICLE_GSON_TYPE);

    ApiAddress address = ApiAddress.builder("articles").embedDoi(doi).addToken("relationships").build();

    when(articleApi.requestObject(address, ArticleMetadata.Factory.RELATED_ARTICLE_GSON_TYPE)).thenReturn(map);
    List<RelatedArticle> raList = articleMetadataFactory.fetchRelatedArticles(doi);
    assertEquals(1, raList.size());
    RelatedArticle ra = raList.get(0);
    assertEquals("foo", ra.getSpecificUse());
  }
}
