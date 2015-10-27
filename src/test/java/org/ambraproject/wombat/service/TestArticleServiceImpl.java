package org.ambraproject.wombat.service;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * used to return article metadata from json files during tests of article controller functionality
 */
public class TestArticleServiceImpl implements ArticleService {
  @Override
  public Map<?, ?> requestArticleMetadata(String articleId, Boolean excludeCitations) throws IOException {
    BufferedReader reader = new BufferedReader(
        new FileReader("src/test/resources/articleMeta/" + articleId.replace("10.1371/journal.", "") + ".json"));
    return (Map<String, Object>) new Gson().fromJson(reader, HashMap.class);
  }
}
