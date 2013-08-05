/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2013 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.wombat.service;

import java.io.IOException;
import java.util.Map;

/**
 * Interface to the article search service for the application.
 */
public interface SearchService {

  /**
   * Performs a "simple" search (against all article fields) and returns the results.
   *
   * @param query term we are searching for
   * @param journal name of the journal in which to search.  TODO: add a value for "all journals",
   *     and change this to an enum.
   * @param start starting result, one-based.  1 will start at the first result.
   * @param rows max number of results to return
   * @return deserialized JSON returned by the search server
   * @throws IOException
   */
  // TODO: add parameter for sort order.
  public Map<?, ?> simpleSearch(String query, String journal, int start, int rows) throws IOException;
}
