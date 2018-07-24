package org.ambraproject.wombat.service;

import com.google.gson.internal.LinkedTreeMap;
import org.ambraproject.wombat.controller.NotFoundException;
import org.ambraproject.wombat.identity.AssetPointer;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;
import java.util.OptionalInt;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = {ArticleResolutionServiceTest.class})
public class ArticleResolutionServiceTest extends AbstractTestNGSpringContextTests {

  @Mock
  private ArticleApi articleApi;

  @InjectMocks
  private ArticleResolutionService articleResolutionService;

  @BeforeMethod
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testToParentIngestion() throws IOException {
    Map<String, Object> doiOverview = makeSampleDoiOverview();
    when(articleApi.requestObject(any(), eq(Map.class))).thenReturn(doiOverview);

    RequestedDoiVersion assetId = RequestedDoiVersion.of("info:doi/10.1371/journal.pcbi.1002012.g002");
    AssetPointer assetPointer = articleResolutionService.toParentIngestion(assetId);
    assertEquals(assetPointer.getAssetDoi(), "10.1371/journal.pcbi.1002012.g002");
    assertEquals(assetPointer.getParentArticle().getDoi(), "10.1371/journal.pcbi.1002012");
    assertEquals(assetPointer.getParentArticle().getIngestionNumber(), 1);
    assertEquals(assetPointer.getParentArticle().getRevisionNumber().getAsInt(), 1);
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void testToParentIngestionNotAnAsset() throws IOException {
    Map<String, Object> doiOverview = makeSampleDoiOverview();
    doiOverview.put("type", "not_an_asset_type");
    when(articleApi.requestObject(any(), eq(Map.class))).thenReturn(doiOverview);

    RequestedDoiVersion assetId = RequestedDoiVersion.of("info:doi/10.1371/journal.pcbi.1002012.g002");
    articleResolutionService.toParentIngestion(assetId);
  }

  @Test
  public void testToParentIngestionForIngestionNumber() throws IOException {
    Map<String, Object> doiOverview = makeSampleDoiOverview();
    when(articleApi.requestObject(any(), eq(Map.class))).thenReturn(doiOverview);

    RequestedDoiVersion assetId = RequestedDoiVersion.ofIngestion(
        "info:doi/10.1371/journal.pcbi.1002012.g002", 1);
    AssetPointer assetPointer = articleResolutionService.toParentIngestion(assetId);
    assertEquals(assetPointer.getAssetDoi(), "10.1371/journal.pcbi.1002012.g002");
    assertEquals(assetPointer.getParentArticle().getDoi(), "10.1371/journal.pcbi.1002012");
    assertEquals(assetPointer.getParentArticle().getIngestionNumber(), 1);
    assertEquals(assetPointer.getParentArticle().getRevisionNumber(), OptionalInt.empty());
  }

  @Test
  public void testToParentIngestionForRevisionNumber() throws IOException {
    Map<String, Object> doiOverview = makeSampleDoiOverview();
    when(articleApi.requestObject(any(), eq(Map.class))).thenReturn(doiOverview);

    RequestedDoiVersion assetId = RequestedDoiVersion.ofRevision(
        "info:doi/10.1371/journal.pcbi.1002012.g002", 1);
    AssetPointer assetPointer = articleResolutionService.toParentIngestion(assetId);
    assertEquals(assetPointer.getAssetDoi(), "10.1371/journal.pcbi.1002012.g002");
    assertEquals(assetPointer.getParentArticle().getDoi(), "10.1371/journal.pcbi.1002012");
    assertEquals(assetPointer.getParentArticle().getIngestionNumber(), 1);
    assertEquals(assetPointer.getParentArticle().getRevisionNumber().getAsInt(), 1);

  }

  private Map<String, Object> makeSampleDoiOverview() {
    Map<String, Object> doiOverview = new LinkedTreeMap<>();
    doiOverview.put("doi", "10.1371/journal.pcbi.1002012.g002");
    doiOverview.put("type", "asset");
    LinkedTreeMap<String, Object> articleInfo = new LinkedTreeMap<>();
    articleInfo.put("doi", "10.1371/journal.pcbi.1002012");
    LinkedTreeMap<String, Object> ingestions = new LinkedTreeMap<>();
    ingestions.put("1", 1.0d);
    articleInfo.put("ingestions", ingestions);
    LinkedTreeMap<String, Object> revisions = new LinkedTreeMap<>();
    revisions.put("1", 1.0d);
    articleInfo.put("revisions", revisions);
    doiOverview.put("article", articleInfo);
    return doiOverview;
  }

}