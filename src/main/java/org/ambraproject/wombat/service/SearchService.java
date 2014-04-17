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

import org.ambraproject.wombat.config.Site;

import java.io.IOException;
import java.util.Map;

/**
 * Interface to the article search service for the application.
 */
public interface SearchService {

  /**
   * Type representing some restriction on the desired search results--for instance, a date range, or a sort order.
   * Implementations of SearchService should also provide appropriate implementations of this interface.
   */
  public interface SearchCriterion {

    /**
     * @return description of this criterion, suitable for exposing in the UI
     */
    public String getDescription();

    /**
     * @return implementation-dependent String value specifying this criterion
     */
    public String getValue();
  }

  /**
   * Performs a "simple" search (against all article fields) and returns the results.
   *
   * @param query     term we are searching for.  If this is null, all articles will be returned (modulo sortOrder,
   *                  start, rows, and dateRange).
   * @param site      name of the site in which to search.
   * @param start     starting result, zero-based.  0 will start at the first result.
   * @param rows      max number of results to return
   * @param sortOrder specifies the desired ordering for results
   * @param dateRange specifies the date range for the results
   * @return deserialized JSON returned by the search server
   * @throws IOException
   */
  public Map<?, ?> simpleSearch(String query, Site site, int start, int rows, SearchCriterion sortOrder,
                                SearchCriterion dateRange) throws IOException;

  /**
   * Performs a search by the subject fields.
   *
   * @param subject   taxonomy term the search will be restricted to
   * @param site      name of the site in which to search.
   * @param start     starting result, zero-based.  0 will start at the first result.
   * @param rows      max number of results to return
   * @param sortOrder specifies the desired ordering for results
   * @param dateRange specifies the date range for the results
   * @return deserialized JSON returned by the search server
   * @throws IOException
   */
  public Map<?, ?> subjectSearch(String subject, Site site, int start, int rows, SearchCriterion sortOrder,
                                 SearchCriterion dateRange) throws IOException;

  /**
   * Performs a search for an author's name.
   *
   * @param author    full or partial author name
   * @param site      name of the site in which to search.
   * @param start     starting result, zero-based.  0 will start at the first result.
   * @param rows      max number of results to return
   * @param sortOrder specifies the desired ordering for results
   * @param dateRange specifies the date range for the results
   * @return deserialized JSON returned by the search server
   * @throws IOException
   */
  public Map<?, ?> authorSearch(String author, Site site, int start, int rows, SearchCriterion sortOrder,
                                SearchCriterion dateRange) throws IOException;

  /**
   * Retrieves articles for display on a journal home page, where there is no actual query.
   *
   * @param site name of the site in which to search
   * @param start starting result, zero-based.  0 will start at the first result.
   * @param rows max number of results to return
   * @param sortOrder specifies the desired ordering for results
   * @return deserialized JSON returned by the search server
   * @throws IOException
   */
  public Map<?, ?> getHomePageArticles(Site site, int start, int rows, SearchCriterion sortOrder) throws IOException;
}
