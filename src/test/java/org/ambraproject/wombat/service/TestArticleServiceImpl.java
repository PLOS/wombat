package org.ambraproject.wombat.service;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import org.ambraproject.wombat.model.ScholarlyWorkId;
import org.ambraproject.wombat.service.remote.ContentKey;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * used to return article metadata from json files during tests of article controller functionality
 */
public class TestArticleServiceImpl implements ArticleService {
  @Override
  public Map<String, ?> requestArticleMetadata(ScholarlyWorkId articleId, boolean excludeCitations) throws IOException {
    BufferedReader reader = new BufferedReader(
            new FileReader("src/test/resources/articleMeta/" + articleId.getDoi().replace("10.1371/journal.","") + ".json"));
    return (Map<String, Object>) new Gson().fromJson(reader, HashMap.class);
  }

  @Override
  public List<ImmutableMap<String, String>> getArticleFiguresAndTables(Map<?, ?> articleMetadata) {
    ImmutableMap<String, String> asset1 = ImmutableMap.<String, String>builder()
        .put("doi", "info:doi/10.1371/journal.pone.0008083.g001")
        .put("title", "Figure 1")
        .build();
    ImmutableMap<String, String> asset2 = ImmutableMap.<String, String>builder()
        .put("doi", "info:doi/10.1371/journal.pone.0008083.t001")
        .put("title", "Table 1")
        .build();
    ImmutableMap<String, String> asset3 = ImmutableMap.<String, String>builder()
        .put("doi", "info:doi/10.1371/journal.pone.0008083.g002")
        .put("title", "Figure 2")
        .build();
    ImmutableMap<String, String> asset4 = ImmutableMap.<String, String>builder()
        .put("doi", "info:doi/10.1371/journal.pone.0008083.g003")
        .put("title", "Figure 3")
        .build();
    return Arrays.asList(asset1, asset2, asset3, asset4);
  }

  @Override
  public ContentKey getManuscriptKey(ScholarlyWorkId articleId) throws IOException {
    throw new UnsupportedOperationException();
  }
}
