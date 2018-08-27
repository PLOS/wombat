package org.ambraproject.wombat.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.ambraproject.wombat.service.remote.ContentKey;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.junit.Assert.assertEquals;

public class ArticleServiceImplTest {
  @InjectMocks
  private ArticleServiceImpl articleService;

  @BeforeMethod
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testCreateKeyFromMap() {
    Map<String, String> fileRepoMap = new HashMap<>();
    fileRepoMap.put("crepoKey", "foo");
    UUID uuid =  UUID.randomUUID();
    fileRepoMap.put("crepoUuid", uuid.toString());
    ContentKey key = articleService.createKeyFromMap(fileRepoMap);
    assertEquals(String.format("[key: foo, uuid: %s]", uuid), key.toString());
  }
}
