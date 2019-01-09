package org.ambraproject.wombat.controller;

import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.url.Link;
import org.ambraproject.wombat.config.site.url.SiteRequestScheme;
import org.ambraproject.wombat.config.theme.Theme;
import org.ambraproject.wombat.identity.ArticlePointer;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.ambraproject.wombat.service.ArticleResolutionService;
import org.ambraproject.wombat.service.PeerReviewService;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.ambraproject.wombat.service.remote.CorpusContentApi;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.ui.Model;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {ArticleMetadataTest.class})
public class ArticleMetadataTest extends AbstractTestNGSpringContextTests {

  @Mock
  public ArticleApi articleApi;

  @InjectMocks
  public CorpusContentApi corpusContentApi;

  @InjectMocks
  public ArticleResolutionService articleResolutionService;

  @InjectMocks
  public ArticleMetadata.Factory articleMetadataFactory;

  @BeforeMethod
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testPopulate() throws IOException {
    ArticleMetadata articleMetadata = spy(articleMetadataFactory.newInstance(mock(Site.class),
        mock(RequestedDoiVersion.class),
        mock(ArticlePointer.class),
        new HashMap(),
        new HashMap(),
        new HashMap()));
    doReturn(null).when(articleMetadata).getFigureView();
    doReturn(null).when(articleMetadata).getArticleType();
    doReturn(null).when(articleMetadata).getCommentCount();
    doReturn(null).when(articleMetadata).getContainingArticleLists();
    doReturn(null).when(articleMetadata).getCategoryTerms();
    doReturn(null).when(articleMetadata).getRelatedArticles();
    doReturn(null).when(articleMetadata).getRevisionMenu();
    doReturn(null).when(articleMetadata).getPeerReview();
    doNothing().when(articleMetadata).populateAuthors(any());

    articleMetadata.populate(mock(HttpServletRequest.class), mock(Model.class));
    verify(articleMetadata).getFigureView();
    verify(articleMetadata).getArticleType();
    verify(articleMetadata).getCommentCount();
    verify(articleMetadata).getContainingArticleLists();
    verify(articleMetadata).getCategoryTerms();
    verify(articleMetadata).getRelatedArticles();
    verify(articleMetadata).getRevisionMenu();
    verify(articleMetadata).getPeerReview();
    verify(articleMetadata).populateAuthors(any());
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
        new HashMap());
    
    articleMetadata.validateVisibility("whatever");
  }

  @Test(expectedExceptions = InternalRedirectException.class)
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
        new HashMap()));

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
        new HashMap());

    HashMap<String, String> expected = new HashMap<>();
    expected.put("type", "figure");
    expected.put("doi", "fakeDoi");
    List<Map<String, ?>> expectedFigureView = new ArrayList<>();
    expectedFigureView.add(expected);
    assertEquals(articleMetadata.getFigureView(), expectedFigureView);
  }
}