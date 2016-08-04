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
import org.ambraproject.wombat.identity.ArticlePointer;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.ambraproject.wombat.service.remote.ContentKey;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Service class that deals with article metadata.
 */
public interface ArticleService {

  /**
   * Requests metadata about an article.
   *
   * @param articleId        DOI string
   * @return deserialized JSON data structure as returned by the SOA layer
   * @throws IOException
   */
  Map<String, ?> requestArticleMetadata(RequestedDoiVersion articleId) throws IOException;

  Map<String, ?> getItemTable(ArticlePointer articleId) throws IOException;

  ContentKey getManuscriptKey(RequestedDoiVersion articleId) throws IOException;

  /**
   * Get the list of article figures and tables from the article meta data
   *
   * @param articleMetadata article metadata
   * @return list of figures and tables
   */
  List<ImmutableMap<String, String>> getArticleFiguresAndTables(Map<?, ?> articleMetadata);
}
