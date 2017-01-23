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

package org.ambraproject.wombat.service;

import org.ambraproject.wombat.config.site.Site;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface RecentArticleService {

  /**
   * Query for recent articles.
   * <p/>
   * The service may retain a reference to the returned list for caching purposes. The caller is required not to modify
   * the list, nor any object nested inside it.
   *
   * @param site                  The site on which to display the articles. Articles will be filtered for this site's
   *                              journal.
   * @param articleCount          The number of articles to find. Unless there are fewer total articles in the journal,
   *                              the returned list is guaranteed to be exactly this size.
   * @param numberOfDaysAgo       The size in days of the "time window" to query. Find articles whose publication date
   *                              is after this many days before present system time. If not enough articles have been
   *                              published in the window to fulfill {@code articleCount}, go beyond the window in
   *                              chronological order to find enough articles.
   * @param shuffle               Whether to randomly select and reorder results. If {@code true}, and the number of
   *                              articles published in the time window is greater than {@code articleCount}, select a
   *                              random subset numbering {@code articleCount} and return them in a random order. If
   *                              {@code false}, keep them in chronological order and truncate down to {@code
   *                              articleCount}. Ignored if the number of articles published in the time window does not
   *                              exceed {@code articleCount}.
   * @param articleTypes          The article types to return, in order of preference. Articles with the same
   *                              publication date are ordered by type, in the same order as this list. The string
   *                              {@code "*"} means "all article types".
   * @param articleTypesToExclude Article types to exclude from results, taking precedence over all other criteria. An
   *                              article with both one of these types and a type in {@code articleTypes} will not be
   *                              matched. Articles of these types will also not be matched by {@code "*"}.
   * @param cacheTtl              The time, in seconds, to cache the results of a query to the service tier. In other
   *                              words, amount of time between refreshing recent articles. If absent, don't read or add
   *                              to the cache and always check for new recent articles.  @return the list of results
   * @throws IOException
   */
  public abstract List<SolrArticleAdapter> getRecentArticles(Site site,
                                                             int articleCount,
                                                             double numberOfDaysAgo,
                                                             boolean shuffle,
                                                             List<String> articleTypes,
                                                             List<String> articleTypesToExclude,
                                                             Optional<Integer> cacheTtl)
      throws IOException;

}
