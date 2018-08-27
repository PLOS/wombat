/*
 * Copyright (c) 2017 Public Library of Science
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package org.ambraproject.wombat.service;

import com.google.gson.Gson;
import org.ambraproject.wombat.identity.ArticlePointer;
import org.ambraproject.wombat.identity.AssetPointer;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.ambraproject.wombat.service.remote.ContentKey;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

  @Override
  public Map<String, ?> getItemMetadata(AssetPointer assetId) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Map<String, ?> getItemFiles(AssetPointer assetId) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Optional<ContentKey> getThumbnailKey(RequestedDoiVersion doi) {
    throw new UnsupportedOperationException();
  }
}
