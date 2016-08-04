package org.ambraproject.wombat.service;

import com.google.gson.Gson;
import org.ambraproject.wombat.identity.ArticlePointer;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.ambraproject.wombat.service.remote.ContentKey;

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
  public Map<String, ?> requestArticleMetadata(RequestedDoiVersion articleId) throws IOException {
    BufferedReader reader = new BufferedReader(
            new FileReader("src/test/resources/articleMeta/" + articleId.getDoi().replace("10.1371/journal.","") + ".json"));
    return (Map<String, Object>) new Gson().fromJson(reader, HashMap.class);
  }

  @Override
  public Map<String, ?> requestArticleMetadata(ArticlePointer articleId) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public ContentKey getManuscriptKey(ArticlePointer articleId) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Map<String, ?> getItemTable(ArticlePointer articleId) throws IOException {
    throw new UnsupportedOperationException();
  }
}
