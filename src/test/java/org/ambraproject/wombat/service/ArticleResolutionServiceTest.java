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
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;
import java.util.OptionalInt;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;

@ContextConfiguration(classes = {ArticleResolutionServiceTest.class})
public class ArticleResolutionServiceTest extends AbstractJUnit4SpringContextTests {

  @Mock
  private ArticleApi articleApi;

  @InjectMocks
  private ArticleResolutionService articleResolutionService;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testToParentIngestion() throws IOException {
    Map<String, Object> doiOverview = makeSampleDoiOverview();
    when(articleApi.requestObject(any(), eq(Map.class))).thenReturn(doiOverview);

    RequestedDoiVersion assetId = RequestedDoiVersion.of("info:doi/10.1371/journal.pcbi.1002012.g002");
    AssetPointer assetPointer = articleResolutionService.toParentIngestion(assetId);
    assertEquals("10.1371/journal.pcbi.1002012.g002", assetPointer.getAssetDoi());
    assertEquals("10.1371/journal.pcbi.1002012", assetPointer.getParentArticle().getDoi());
    assertEquals(1, assetPointer.getParentArticle().getIngestionNumber());
    assertEquals(1, assetPointer.getParentArticle().getRevisionNumber().getAsInt());
  }

  @Test(expected = NotFoundException.class)
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
    assertEquals("10.1371/journal.pcbi.1002012.g002", assetPointer.getAssetDoi());
    assertEquals("10.1371/journal.pcbi.1002012", assetPointer.getParentArticle().getDoi());
    assertEquals(1, assetPointer.getParentArticle().getIngestionNumber());
    assertEquals(OptionalInt.empty(), assetPointer.getParentArticle().getRevisionNumber());
  }

  @Test
  public void testToParentIngestionForRevisionNumber() throws IOException {
    Map<String, Object> doiOverview = makeSampleDoiOverview();
    when(articleApi.requestObject(any(), eq(Map.class))).thenReturn(doiOverview);

    RequestedDoiVersion assetId = RequestedDoiVersion.ofRevision(
        "info:doi/10.1371/journal.pcbi.1002012.g002", 1);
    AssetPointer assetPointer = articleResolutionService.toParentIngestion(assetId);
    assertEquals("10.1371/journal.pcbi.1002012.g002", assetPointer.getAssetDoi());
    assertEquals("10.1371/journal.pcbi.1002012", assetPointer.getParentArticle().getDoi());
    assertEquals(1, assetPointer.getParentArticle().getIngestionNumber());
    assertEquals(1, assetPointer.getParentArticle().getRevisionNumber().getAsInt());

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
