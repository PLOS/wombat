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

import org.ambraproject.wombat.util.RevisionId;

import java.io.IOException;
import java.util.Map;

/**
 * Service class that deals with article metadata.
 */
public interface ArticleService {

  /**
   * Requests metadata about an article.
   *
   * @param revisionId DOI string
   * @param excludeCitations if true, citation data will not be returned (optimization param)
   * @return deserialized JSON data structure as returned by the SOA layer
   * @throws IOException
   */
  Map<?, ?> requestArticleMetadata(RevisionId revisionId, boolean excludeCitations) throws IOException;
}
