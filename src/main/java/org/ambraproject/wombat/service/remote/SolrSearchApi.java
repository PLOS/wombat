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

package org.ambraproject.wombat.service.remote;

import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Interface to the article search service for the application.
 */
public interface SolrSearchApi {

  public static final Integer MAXIMUM_SOLR_RESULT_COUNT = 1000;

  /**
   * Performs a search and returns the results.
   *
   * @return deserialized JSON returned by the search server
   * @throws IOException
   */
  public Map<String, ?> search(ArticleSearchQuery query) throws IOException;


  /**
   * Attempts to retrieve information about an article based on the DOI.
   *
   * @param doi identifies the article
   * @param site
   * @return information about the article, if it exists; otherwise an empty result set
   * @throws IOException
   */
  public Map<?, ?> lookupArticleByDoi(String doi, Site site) throws IOException;

  public Map<?, ?> lookupArticlesByDois(List<String> dois, Site site) throws IOException;

  /**
   * Adds a new property, link, to each search result passed in.  The value of this property is the correct URL to the
   * article on this environment.  Calling this method is necessary since article URLs need to be specific to the site
   * of the journal the article is published in, not the site in which the search results are being viewed.
   *
   * @param searchResults deserialized search results JSON
   * @param request       current request
   * @param site          site of the current request (for the search results)
   * @param siteSet       site set of the current request
   * @return searchResults decorated with the new property
   * @throws IOException
   */
  public Map<?, ?> addArticleLinks(Map<?, ?> searchResults, HttpServletRequest request, Site site, SiteSet
      siteSet) throws IOException;

  /**
   * Retrieves Solr stats for a given field in a given journal
   *
   * @param fieldName  specifies the name of the field
   * @param journalKey specifies the name of the journal
   * @param site the current site
   * @return Solr stats for the given field
   * @throws IOException
   */
  public Map<?, ?> getStats(String fieldName, String journalKey, Site site) throws IOException;

  /**
   * Returns a list of all subject categories associated with all papers ever published
   * for the given journal.
   *
   * @param journalKey name of the journal in question
   * @param site the current site
   * @return List of category names
   */
  public List<String> getAllSubjects(String journalKey, Site site) throws IOException;

  /**
   * Returns the number of articles, for a given journal, associated with all the subject
   * categories in the taxonomy.
   *
   * @param journalKey specifies the journal
   * @param site the current site
   * @throws IOException
   */
  public Map<String, Long> getAllSubjectCounts(String journalKey, Site site) throws IOException;
}
