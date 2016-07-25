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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.ambraproject.wombat.service.remote.ContentKey;
import org.ambraproject.wombat.util.DoiSchemeStripper;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ArticleServiceImpl implements ArticleService {

  @Autowired
  private ArticleApi articleApi;
  @Autowired
  private ArticleResolutionService articleResolutionService;

  private static final ImmutableSet<String> FIGURE_TABLE_CONTEXT_ELEMENT =
      new ImmutableSet.Builder<String>()
          .add("fig").add("table-wrap").add("alternatives")
          .build();

  @Override
  public Map<String, Object> requestArticleMetadata(RequestedDoiVersion articleId, boolean excludeCitations)
      throws IOException {
    Map<String, Object> map = (Map<String, Object>) articleApi.requestObject(
        articleResolutionService.toIngestion(articleId).asApiAddress().build(),
        Map.class);
    return DoiSchemeStripper.strip(map);
  }

  @Override
  public List<ImmutableMap<String, String>> getArticleFiguresAndTables(Map<?, ?> articleMetadata) {
    List<Map<String, String>> assets = (List<Map<String, String>>) articleMetadata.get("figures");
    List<ImmutableMap<String, String>> figsAndTables = assets.stream()
        .filter(asset -> FIGURE_TABLE_CONTEXT_ELEMENT.contains(asset.get("contextElement")))
        .map(asset -> ImmutableMap.<String, String>builder().put("title", asset.get("title"))
            .put("doi", asset.get("doi"))
            .build())
        .collect(Collectors.toList());
    return figsAndTables;
  }

  @Override
  public ContentKey getManuscriptKey(RequestedDoiVersion articleId) throws IOException {
    ApiAddress itemAddress = articleResolutionService.toIngestion(articleId).asApiAddress().addToken("items").build();
    Map<String, ?> itemResponse = articleApi.requestObject(itemAddress, Map.class);
    Map<String, ?> articleItem = (Map<String, ?>) ((Map<String, ?>) itemResponse.get("items")).values().stream()
        .filter(itemObj -> ((Map<String, ?>) itemObj).get("itemType").equals("article"))
        .findAny().orElseThrow(RuntimeException::new);
    Map<String, ?> articleFiles = (Map<String, ?>) articleItem.get("files");
    Map<String, ?> manuscriptPointer = (Map<String, ?>) articleFiles.get("manuscript");

    String crepoKey = (String) manuscriptPointer.get("crepoKey");
    UUID crepoUuid = UUID.fromString((String) manuscriptPointer.get("crepoUuid"));
    return ContentKey.createForUuid(crepoKey, crepoUuid);
  }
}
