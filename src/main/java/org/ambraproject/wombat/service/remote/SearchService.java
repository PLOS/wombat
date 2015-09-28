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

package org.ambraproject.wombat.service.remote;

import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
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
   * @param journalKeys list of the journals in which to search
   * @param articleTypes types of articles in which to search
   * @param start     starting result, zero-based.  0 will start at the first result.
   * @param rows      max number of results to return
   * @param sortOrder specifies the desired ordering for results
   * @param dateRange specifies the date range for the results
   * @return deserialized JSON returned by the search server
   * @throws IOException
   */
  public Map<?, ?> simpleSearch(String query, List<String> journalKeys, List<String> articleTypes,
      int start, int rows, SearchCriterion sortOrder, SearchCriterion dateRange) throws IOException;

  /**
   * Performs a faceted "simple" search (against all article fields) and returns the results.
   *
   * @param facetField the field that should be treated as a facet
   * @param query term we are searching for.  If this is null, all articles will be returned (modulo sortOrder,
   *              start, rows, and dateRange)
   * @param journalKeys list of the journals in which to search
   * @param articleTypes types of articles in which to search
   * @param dateRange specifies the date range for the results
   * @return deserialized JSON returned by the search server
   * @throws IOException
   */
  public Map<?, ?> simpleSearch(String facetField, String query, List<String> journalKeys, List<String> articleTypes,
      SearchCriterion dateRange) throws IOException;

  /**
   * Performs a "simple" search (against all article fields) and returns the results. It allows to pass raw
   * query parameters to Solr
   *
   * @param query     term we are searching for.  If this is null, all articles will be returned (modulo sortOrder,
   *                  start, rows, and dateRange).
   * @param journalKeys list of the journals in which to search
   * @param articleTypes types of articles in which to search
   * @param start     starting result, zero-based.  0 will start at the first result.
   * @param rows      max number of results to return
   * @param sortOrder specifies the desired ordering for results
   * @param dateRange specifies the date range for the results
   * @param rawQueryParams specifies the raw query parameters passed as name/value pairs
   * @return deserialized JSON returned by the search server
   * @throws IOException
   */
  public Map<?, ?> simpleSearch(String query, List<String> journalKeys, List<String> articleTypes,
      int start, int rows, SearchCriterion sortOrder, SearchCriterion dateRange, Map<String, String> rawQueryParams)
      throws IOException;

  /**
   * Performs an "advanced search": the query parameter will be directly parsed by solr.
   *
   * @param query specifies the solr fields and the values we are searching for; may be a boolean
   *     combination of these.  Example: "(abstract:gene) AND author:smith".
   * @param journalKeys list of the journals in which to search
   * @param articleTypes types of articles in which to search
   * @param subjectList only articles associated with these subjects will be returned
   * @param start starting result, zero-based.  0 will start at the first result.
   * @param rows max number of results to return
   * @param sortOrder specifies the desired ordering for results
   * @param dateRange specifies the date range for the results
   * @return deserialized JSON returned by the search server
   * @throws IOException
   */
  public Map<?, ?> advancedSearch(String query, List<String> journalKeys, List<String> articleTypes,
      List<String> subjectList, int start, int rows, SearchCriterion sortOrder,
      SearchCriterion dateRange) throws IOException;

  /**
   * Performs a faceted "advanced search": the query parameter will be directly parsed by solr.
   *
   * @param facetField the field that should be treated as a facet
   * @param query specifies the solr fields and the values we are searching for; may be a boolean
   *              combination of these.  Example: "(abstract:gene) AND author:smith"
   * @param journalKeys list of the journals in which to search
   * @param articleTypes types of articles in which to search
   * @param subjectList only articles associated with these subjects will be returned
   * @param dateRange specifies the date range for the results
   * @return deserialized JSON returned by the search server
   * @throws IOException
   */
  public Map<?, ?> advancedSearch(String facetField, String query, List<String> journalKeys,
      List<String> articleTypes, List<String> subjectList, SearchCriterion dateRange) throws IOException;

  /**
   * Performs a search by the subject fields.
   *
   * @param subjects   taxonomy terms the search will be restricted to
   * @param journalKeys list of the journals in which to search
   * @param start     starting result, zero-based.  0 will start at the first result.
   * @param rows      max number of results to return
   * @param sortOrder specifies the desired ordering for results
   * @param dateRange specifies the date range for the results
   * @return deserialized JSON returned by the search server
   * @throws IOException
   */
  public Map<?, ?> subjectSearch(List<String> subjects, List<String> journalKeys,
      int start, int rows, SearchCriterion sortOrder, SearchCriterion dateRange) throws IOException;

  /**
   * Performs a faceted search by the subject fields.
   *
   * @param facetField the field that should be treated as a facet
   * @param subjects taxonomy terms the search will be restricted to
   * @param journalKeys list of the journals in which to search
   * @param articleTypes types of articles in which to search
   * @param dateRange specifies the date range for the results
   * @return deserialized JSON returned by the search server
   * @throws IOException
   */
  public Map<?, ?> subjectSearch(String facetField, List<String> subjects, List<String> journalKeys,
      List<String> articleTypes, SearchCriterion dateRange) throws IOException;

  /**
   * Performs a search for an author's name.
   *
   * @param author    full or partial author name
   * @param journalKeys list of the journals in which to search
   * @param start     starting result, zero-based.  0 will start at the first result.
   * @param rows      max number of results to return
   * @param sortOrder specifies the desired ordering for results
   * @param dateRange specifies the date range for the results
   * @return deserialized JSON returned by the search server
   * @throws IOException
   */
  public Map<?, ?> authorSearch(String author, List<String> journalKeys, int start, int rows,
      SearchCriterion sortOrder, SearchCriterion dateRange) throws IOException;

  /**
   * Performs a faceted search for an author's name.
   *
   * @param facetField the field that should be treated as a facet
   * @param author full or partial author name
   * @param journalKeys list of the journals in which to search
   * @param articleTypes types of articles in which to search
   * @param dateRange specifies the date range for the results
   * @return deserialized JSON returned by the search server
   * @throws IOException
   */
  public Map<?, ?> authorSearch(String facetField, String author, List<String> journalKeys,
      List<String> articleTypes, SearchCriterion dateRange) throws IOException;

  /**
   * Searches for articles within a volume of a journal.
   *
   * @param volume the volume number
   * @param journalKeys list of the journals in which to search
   * @param articleTypes types of articles in which to search
   * @param start starting result, zero-based.  0 will start at the first result.
   * @param rows max number of results to return
   * @param sortOrder specifies the desired ordering for results
   * @param dateRange specifies the date range for the results
   * @return deserialized JSON returned by the search server
   * @throws IOException
   */
  public Map<?, ?> volumeSearch(int volume, List<String> journalKeys, List<String> articleTypes, int start, int rows,
      SearchCriterion sortOrder, SearchCriterion dateRange) throws IOException;

  /**
   * Searches for articles within a volume of a journal. This is a faceted search.
   *
   * @param facetField the field that should be treated as a facet
   * @param volume the volume number
   * @param journalKeys list of the journals in which to search
   * @param articleTypes types of articles in which to search
   * @param dateRange specifies the date range for the results
   * @return deserialized JSON returned by the search server
   * @throws IOException
   */
  public Map<?, ?> volumeSearch(String facetField, int volume, List<String> journalKeys,
      List<String> articleTypes, SearchCriterion dateRange) throws IOException;
  /**
   * Retrieves articles for display on a journal home page, where there is no actual query.
   *
   * @param journalKey journal in which to search
   * @param start     starting result, zero-based.  0 will start at the first result.
   * @param rows      max number of results to return
   * @param sortOrder specifies the desired ordering for results
   * @return deserialized JSON returned by the search server
   * @throws IOException
   */
  public Map<?, ?> getHomePageArticles(String journalKey, int start, int rows,
      SearchCriterion sortOrder) throws IOException;

  /**
   * Attempts to retrieve information about an article based on the DOI.
   *
   * @param doi identifies the article
   * @return information about the article, if it exists; otherwise an empty result set
   * @throws IOException
   */
  public Map<?, ?> lookupArticleByDoi(String doi) throws IOException;

  /**
   * Attempts to retrieve information about an article based on the journal key and eLocationId.
   *
   * @param eLocationId identifies the article within a journal
   * @param journalKey the journal in which to search
   * @return information about the article, if it exists; otherwise an empty result set
   * @throws IOException
   */
  public Map<?, ?> lookupArticleByELocationId(String eLocationId, String journalKey) throws IOException;

  /**
   * Adds a new property, link, to each search result passed in.  The value of this property
   * is the correct URL to the article on this environment.  Calling this method is necessary
   * since article URLs need to be specific to the site of the journal the article is published
   * in, not the site in which the search results are being viewed.
   *
   * @param searchResults deserialized search results JSON
   * @param request current request
   * @param site site of the current request (for the search results)
   * @param siteSet site set of the current request
   * @param includeApplicationRoot if true, the root context path of the application will be included
   *     in the link; if false, it will not.  If you are generating a link to use in template code,
   *     this should be true; if you are using this value for a "redirect:" string returned from a
   *     spring controller, it should be false.
   * @return searchResults decorated with the new property
   * @throws IOException
   */
  public Map<?, ?> addArticleLinks(Map<?, ?> searchResults, HttpServletRequest request, Site site, SiteSet siteSet,
      boolean includeApplicationRoot) throws IOException;

  /**
   * Retrieves Solr stats for a given field in a given journal
   *
   * @param fieldName specifies the name of the field
   * @param journalKey specifies the name of the journal
   * @return Solr stats for the given field
   * @throws IOException
   */
  public Map<?, ?> getStats(String fieldName, String journalKey) throws IOException;
}
