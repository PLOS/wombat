package org.ambraproject.wombat.service;

import org.ambraproject.wombat.config.site.Site;

import java.io.IOException;
import java.util.List;

public interface RecentArticleService {

  /**
   * Query for recent articles.
   *
   * @param site            The site on which to display the articles. Articles will be filtered for this site's
   *                        journal.
   * @param articleCount    The number of articles to find. Unless there are fewer total articles in the journal, the
   *                        returned list is guaranteed to be exactly this size.
   * @param numberOfDaysAgo The size in days of the "time window" to query. Find articles whose publication date is
   *                        after this many days before present system time. If not enough articles have been published
   *                        in the window to fulfill {@code articleCount}, go beyond the window in chronological order
   *                        to find enough articles.
   * @param shuffle         Whether to randomly select and reorder results. If {@code true}, and the number of articles
   *                        published in the time window is greater than {@code articleCount}, select a random subset
   *                        numbering {@code articleCount} and return them in a random order. If {@code false}, keep
   *                        them in chronological order and truncate down to {@code articleCount}. Ignored if the number
   *                        of articles published in the time window does not exceed {@code articleCount}.
   * @param articleTypes    The article types to return, in order of preference. If enough articles of these types have
   *                        been published in the time window to fulfill {@code articleCount}, they are sorted by type
   *                        in this order, then in chronological order. If the query went beyond the time window, the
   *                        order is ignored and the results are sorted in chronological order. The string {@code "*"}
   *                        means "all article types".
   * @return the list of results
   * @throws IOException
   */
  public abstract List<Object> getRecentArticles(Site site,
                                                 int articleCount,
                                                 double numberOfDaysAgo,
                                                 boolean shuffle,
                                                 List<String> articleTypes)
      throws IOException;

}
