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

import com.google.common.collect.MoreCollectors;
import org.ambraproject.wombat.identity.ArticlePointer;
import org.ambraproject.wombat.identity.AssetPointer;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.ambraproject.wombat.service.remote.ApiAddress;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.ambraproject.wombat.service.remote.ContentKey;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ArticleServiceImpl implements ArticleService {

  @Autowired
  private ArticleApi articleApi;
  @Autowired
  private ArticleResolutionService articleResolutionService;

  @Override
  public Map<String, ?> requestArticleMetadata(RequestedDoiVersion articleId)
      throws IOException {
    return requestArticleMetadata(articleResolutionService.toIngestion(articleId));
  }

  @Override
  public Map<String, ?> requestArticleMetadata(ArticlePointer articleId) throws IOException {
    return (Map<String, ?>) articleApi.requestObject(articleId.asApiAddress().build(), Map.class);
  }

  @Override
  public Map<String, ?> getItemTable(ArticlePointer articleId) throws IOException {
    ApiAddress itemAddress = articleId.asApiAddress().addToken("items").build();
    Map<String, ?> itemResponse = articleApi.requestObject(itemAddress, Map.class);
    return (Map<String, ?>) itemResponse.get("items");
  }

  @Override
  public Map<String, ?> getItemMetadata(AssetPointer assetId) throws IOException {
    Map<String, ?> itemTable = getItemTable(assetId.getParentArticle());
    return (Map<String, ?>) itemTable.get(assetId.getAssetDoi());
  }

  @Override
  public Map<String, ?> getItemFiles(AssetPointer assetId) throws IOException {
    return (Map<String, ?>) getItemMetadata(assetId).get("files");
  }

  @Override
  public ContentKey getManuscriptKey(ArticlePointer articleId) throws IOException {
    Map<String, ?> itemTable = getItemTable(articleId);
    Map<String, ?> articleItem = (Map<String, ?>) itemTable.values().stream()
        .filter(itemObj -> ((Map<String, ?>) itemObj).get("itemType").equals("article"))
        .collect(MoreCollectors.onlyElement());
    Map<String, ?> articleFiles = (Map<String, ?>) articleItem.get("files");
    Map<String, ?> manuscriptPointer = (Map<String, ?>) articleFiles.get("manuscript");

    String crepoKey = (String) manuscriptPointer.get("crepoKey");
    UUID crepoUuid = UUID.fromString((String) manuscriptPointer.get("crepoUuid"));
    ContentKey key = ContentKey.createForUuid(crepoKey, crepoUuid);
    String bucketName = (String) manuscriptPointer.get("bucketName");
    key.setBucketName(bucketName);
    return key;
  }

  @Override
  public Optional<ContentKey> getThumbnailKey(RequestedDoiVersion doi) {
    try {
      AssetPointer asset = articleResolutionService.toParentIngestion(doi);
      Map<String, ?> files = getItemFiles(asset);
      return Optional.of(createKeyFromMap((Map<String, String>) files.get("thumbnail")));
    } catch (IOException ex) {
      return Optional.empty();
    }
  }

  public ContentKey createKeyFromMap(Map<String, String> fileRepoMap) {
    String key = fileRepoMap.get("crepoKey");
    UUID uuid = UUID.fromString(fileRepoMap.get("crepoUuid"));
    ContentKey contentKey = ContentKey.createForUuid(key, uuid);
    if (fileRepoMap.get("bucketName") != null) {
      contentKey.setBucketName(fileRepoMap.get("bucketName"));
    }
    return contentKey;
  }
}
