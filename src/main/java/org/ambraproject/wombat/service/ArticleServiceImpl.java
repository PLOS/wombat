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

  private static final ImmutableSet<String> FIGURE_TABLE_CONTEXT_ELEMENT =
      new ImmutableSet.Builder<String>()
      .add("fig").add("table-wrap").add("alternatives")
      .build();

  @Override
  public Map<String, Object> requestArticleMetadata(String articleId, Boolean excludeCitations) throws IOException {
    String revisionNumber = "1"; // TODO: Get as parameter
    Map<String, Object> map = (Map<String, Object>) articleApi.requestObject(
        ApiAddress.builder("articles").addToken(articleId)
            .addParameter("versionedPreview").addParameter("revision", revisionNumber)
            .addParameter("excludeCitations", excludeCitations.toString())
            .build(),
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
