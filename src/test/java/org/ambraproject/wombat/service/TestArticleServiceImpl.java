package org.ambraproject.wombat.service;

import com.google.gson.Gson;
import org.ambraproject.wombat.util.RevisionId;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * used to return article metadata from json files during tests of article controller functionality
 */
public class TestArticleServiceImpl implements ArticleService {
  @Override
  public Map<?, ?> requestArticleMetadata(RevisionId revisionId, boolean excludeCitations) throws IOException {
    String name = revisionId.getArticleId().replace("10.1371/journal.", "");
    try (Reader reader = new BufferedReader(new FileReader("src/test/resources/articleMeta/" + name + ".json"))) {
      return (Map<String, Object>) new Gson().fromJson(reader, HashMap.class);
    }
  }
}
