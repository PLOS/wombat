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
import org.ambraproject.wombat.model.ScholarlyWorkId;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.ambraproject.wombat.util.DoiSchemeStripper;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.Map;
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
  public Map<String, Object> requestArticleMetadata(ScholarlyWorkId articleId, boolean excludeCitations)
      throws IOException {
    Map<String, Object> map = (Map<String, Object>) articleApi.requestObject(
        articleResolutionService.toIngestion(articleId).build(),
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
}
