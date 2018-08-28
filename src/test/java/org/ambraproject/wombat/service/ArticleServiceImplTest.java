package org.ambraproject.wombat.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.ambraproject.wombat.identity.ArticlePointer;
import org.ambraproject.wombat.identity.AssetPointer;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.ambraproject.wombat.service.remote.ApiAddress;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.ambraproject.wombat.service.remote.ContentKey;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class ArticleServiceImplTest {
  @Mock
  RequestedDoiVersion doi;

  @Mock
  AssetPointer assetPointer;

  @Mock
  ArticleApi articleApi;

  @Mock
  ArticlePointer assetParent;

  @Mock
  ApiAddress.Builder builder;

  @Mock
  ApiAddress itemAddress;

  @Mock
  ArticleResolutionService articleResolutionService;

  @InjectMocks
  ArticleServiceImpl articleService;

  @BeforeMethod
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  String doiString = "10.9999/abc";
  UUID crepoUuid = UUID.randomUUID();
  String crepoKey = "key";
  ContentKey contentKey = ContentKey.createForUuid(crepoKey, crepoUuid);

  private Map<String, Map<String, Map<String, Map<String, Map<String, String>>>>>
    createItem(String kind) {
    /* lol */
    Map<String, Map<String, Map<String, Map<String, Map<String, String>>>>> retval = new HashMap<>();
    Map<String, Map<String, Map<String, Map<String, String>>>> items = new HashMap<>();
    Map<String, Map<String, Map<String, String>>> item = new HashMap<>();
    Map<String, Map<String, String>> files = new HashMap<>();
    Map<String, String> file = new HashMap<>();
    retval.put("items", items);
    items.put(doiString, item);
    item.put("files", files);
    files.put(kind, file);
    file.put("crepoKey", crepoKey);
    file.put("crepoUuid", crepoUuid.toString());
    return retval;
  }
  
  
  @Test
  public void testCreateKeyFromMap() {
    String kind = "foo";
    Map<String, String> fileRepoMap =
      createItem(kind).get("items").get(doiString).get("files").get(kind);
    assertEquals(contentKey, articleService.createKeyFromMap(fileRepoMap));
  }

  @Test
  public void testGetThumbnailKey() throws IOException {
    when(doi.getDoi()).thenReturn(doiString);
    when(articleResolutionService.toParentIngestion(doi)).thenReturn(assetPointer);
    when(assetPointer.getParentArticle()).thenReturn(assetParent);
    when(assetPointer.getAssetDoi()).thenReturn(doiString);
    when(assetParent.asApiAddress()).thenReturn(builder);
    when(builder.addToken("items")).thenReturn(builder);
    when(builder.build()).thenReturn(itemAddress);
    when(articleApi.requestObject(itemAddress, Map.class)).thenReturn(createItem("thumbnail"));
    assertEquals(Optional.of(contentKey), articleService.getThumbnailKey(doi));
  }

  @Test
  public void testGetThumbnailKeyEmpty() throws IOException {
    when(doi.getDoi()).thenReturn(doiString);
    when(articleResolutionService.toParentIngestion(doi)).thenReturn(assetPointer);
    when(assetPointer.getParentArticle()).thenReturn(assetParent);
    when(assetPointer.getAssetDoi()).thenReturn(doiString);
    when(assetParent.asApiAddress()).thenReturn(builder);
    when(builder.addToken("items")).thenReturn(builder);
    when(builder.build()).thenReturn(itemAddress);
    when(articleApi.requestObject(itemAddress, Map.class)).thenReturn(createItem("notThumbnail"));
    assertEquals(Optional.empty(), articleService.getThumbnailKey(doi));
  }
}
