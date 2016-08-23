package org.ambraproject.wombat.service;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.ambraproject.rhombat.cache.Cache;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.service.remote.ArticleSearchQuery;
import org.ambraproject.wombat.service.remote.SolrSearchApi;
import org.ambraproject.wombat.service.remote.SolrSearchApiImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RecentArticleServiceImpl implements RecentArticleService {
  private static final Logger log = LoggerFactory.getLogger(RecentArticleServiceImpl.class);

  private final int MAXIMUM_RESULTS = 1000;

  @Autowired
  private SolrSearchApi solrSearchApi;
  @Autowired
  private Cache cache;

  /*
   * This could be injected as a bean instead if needed.
   *
   * Does not need to be SecureRandom. Although it is important for feature correctness that we use a fair random
   * distribution, there is no security risk if the randomness becomes predictable.
   */
  private final Random random = new Random();

  private static final int SECONDS_PER_DAY = 60 * 60 * 24;

  /**
   * Select a random subset of elements and shuffle their order.
   * <p>
   * The argument {@code sequence} is not mutated.
   * <p>
   * This is more efficient than using {@link Collections#shuffle(List)} if {@code n} is much smaller than {@code
   * sequence.size()}, as it shuffles only part of the list.
   *
   * @param elements a collection of elements
   * @param n        the number of elements to select and return shuffled
   * @return a list of {@code n} elements selected at random from {@code sequence} and in a random order
   */
  private <T> List<T> shuffleSubset(Collection<? extends T> elements, int n) {
    List<T> sequence = new ArrayList<>(elements);
    final int size = sequence.size();
    Preconditions.checkArgument(size >= n);

    for (int i = 0; i < n; i++) {
      int swapTarget = i + random.nextInt(size - i);
      Collections.swap(sequence, i, swapTarget);
    }
    return sequence.subList(0, n);
  }

  @Override
  public List<SolrArticleAdapter> getRecentArticles(Site site,
                                                    int articleCount,
                                                    double numberOfDaysAgo,
                                                    boolean shuffle,
                                                    List<String> articleTypes,
                                                    List<String> articleTypesToExclude,
                                                    Optional<Integer> cacheDuration)
      throws IOException {
    String journalKey = site.getJournalKey();
    String cacheKey = "recentArticles:" + journalKey;
    List<SolrArticleAdapter> articles = null;
    if (cacheDuration.isPresent()) {
      articles = cache.get(cacheKey); // remains null if not cached
    }
    if (articles == null) {
      articles = retrieveRecentArticles(journalKey, articleCount, numberOfDaysAgo, articleTypes, articleTypesToExclude);
      if (cacheDuration.isPresent()) {
        /*
         * Casting to Serializable relies on all data structures that Gson uses to be serializable, which is safe
         * enough. We could avoid the cast with a shallow copy to a serializable List, but we would still rely on all
         * nested Lists and Maps being serializable. We'd rather avoid a deep copy until it's necessary.
         */
        cache.put(cacheKey, (Serializable) articles, cacheDuration.get());
      }
    }

    if (articles.size() > articleCount) {
      articles = shuffle ? shuffleSubset(articles, articleCount) : articles.subList(0, articleCount);
    }

    /*
     * Returning this object, we rely on the caller not to modify the contents, as documented for
     * RecentArticleService.getRecentArticles. Depending on cache implementation and whether we made a copy to shuffle,
     * mutating the returned object (or its contents) could disrupt future calls to this method. Merely wrapping the
     * return value in java.util.Collections.unmodifiableList would (similar to the serializability thing above) leave
     * nested Lists and Maps mutable. Let's not recursively wrap every data structure until it's necessary.
     */
    return articles;
  }

  private List<SolrArticleAdapter> retrieveRecentArticles(String journalKey,
                                                          int articleCount,
                                                          double numberOfDaysAgo,
                                                          List<String> articleTypes,
                                                          List<String> articleTypesToExclude)
      throws IOException {

    ArrayList<String> journalKeys = new ArrayList<>();
    journalKeys.add(journalKey);

    LocalDate startDate = LocalDate.now().minusDays((long) numberOfDaysAgo);
    SolrSearchApiImpl.SolrExplicitDateRange dateRange = new SolrSearchApiImpl.SolrExplicitDateRange
        ("Recent Articles", startDate.toString(), LocalDate.now().toString());

    ArticleSearchQuery recentArticleSearchQuery = ArticleSearchQuery.builder()
        .setStart(0)
        .setRows(MAXIMUM_RESULTS)
        .setSortOrder(SolrSearchApiImpl.SolrSortOrder.DATE_NEWEST_FIRST)
        .setArticleTypes(articleTypes)
        .setArticleTypesToExclude(articleTypesToExclude)
        .setDateRange(dateRange)
        .setJournalKeys(journalKeys)
        .build();

    Map<String, ?> results = solrSearchApi.search(recentArticleSearchQuery);
    List<SolrArticleAdapter> articles = SolrArticleAdapter.unpackSolrQuery(results);
    if (articles.size() < articleCount) {
      if (articleTypes.size() > 1) {
        String errorMessage = "" +
            "Service does not support queries for a minimum number of recent articles " +
            "filtered by multiple article types. " +
            "To make a valid query, client must either " +
            "(1) omit the 'min' parameter, " +
            "(2) use no more than one 'type' parameter, or " +
            "(3) include the wildcard type parameter ('type=*').";
        throw new RuntimeException(errorMessage);
      } else {
        articles = getAllArticles(articleTypes, articleTypesToExclude, journalKeys);
      }
    }
    return articles;
  }

  private List<SolrArticleAdapter> getAllArticles(List<String> articleTypes,
                                                  List<String> articleTypesToExclude,
                                                  ArrayList<String> journalKeys)
      throws IOException {
    ArticleSearchQuery allArticleSearchQuery = ArticleSearchQuery.builder()
        .setStart(0)
        .setRows(MAXIMUM_RESULTS)
        .setSortOrder(SolrSearchApiImpl.SolrSortOrder.DATE_NEWEST_FIRST)
        .setArticleTypes(articleTypes)
        .setArticleTypesToExclude(articleTypesToExclude)
        .setJournalKeys(journalKeys)
        .build();
    return SolrArticleAdapter.unpackSolrQuery(solrSearchApi.search(allArticleSearchQuery));
  }

}
