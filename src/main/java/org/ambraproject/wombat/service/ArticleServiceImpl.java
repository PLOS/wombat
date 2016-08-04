/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2014 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.wombat.service;

import org.ambraproject.wombat.identity.ArticlePointer;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.ambraproject.wombat.service.remote.ContentKey;
import org.ambraproject.wombat.util.DoiSchemeStripper;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class ArticleServiceImpl implements ArticleService {

  @Autowired
  private ArticleApi articleApi;
  @Autowired
  private ArticleResolutionService articleResolutionService;

  @Override
  public Map<String, Object> requestArticleMetadata(RequestedDoiVersion articleId)
      throws IOException {
    Map<String, Object> map = (Map<String, Object>) articleApi.requestObject(
        articleResolutionService.toIngestion(articleId).asApiAddress().build(),
        Map.class);
    return DoiSchemeStripper.strip(map);
  }

  @Override
  public Map<String, ?> getItemTable(ArticlePointer articleId) throws IOException {
    ApiAddress itemAddress = articleId.asApiAddress().addToken("items").build();
    Map<String, ?> itemResponse = articleApi.requestObject(itemAddress, Map.class);
    return (Map<String, ?>) itemResponse.get("items");
  }

  @Override
  public ContentKey getManuscriptKey(RequestedDoiVersion articleId) throws IOException {
    Map<String, ?> itemTable = getItemTable(articleResolutionService.toIngestion(articleId));
    Map<String, ?> articleItem = (Map<String, ?>) itemTable.values().stream()
        .filter(itemObj -> ((Map<String, ?>) itemObj).get("itemType").equals("article"))
        .findAny().orElseThrow(RuntimeException::new);
    Map<String, ?> articleFiles = (Map<String, ?>) articleItem.get("files");
    Map<String, ?> manuscriptPointer = (Map<String, ?>) articleFiles.get("manuscript");

    String crepoKey = (String) manuscriptPointer.get("crepoKey");
    UUID crepoUuid = UUID.fromString((String) manuscriptPointer.get("crepoUuid"));
    return ContentKey.createForUuid(crepoKey, crepoUuid);
  }
}
