package org.ambraproject.wombat.service;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.ambraproject.rhombat.HttpDateUtil;
import org.ambraproject.rhombat.cache.Cache;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.service.remote.SoaService;
import org.ambraproject.wombat.util.UrlParamBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RecentArticleServiceImpl implements RecentArticleService {
  private static final Logger log = LoggerFactory.getLogger(RecentArticleServiceImpl.class);

  @Autowired
  private SoaService soaService;
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
   * <p/>
   * The argument {@code sequence} is not mutated.
   * <p/>
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
  public List<Map<String, Object>> getRecentArticles(Site site,
                                                     int articleCount,
                                                     double numberOfDaysAgo,
                                                     boolean shuffle,
                                                     List<String> articleTypes,
                                                     List<String> articleTypesToExclude,
                                                     Optional<Integer> cacheDuration)
      throws IOException {
    String journalKey = site.getJournalKey();
    String cacheKey = "recentArticles:" + journalKey;
    List<Map<String, Object>> articles = null;
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

  private List<Map<String, Object>> retrieveRecentArticles(String journalKey,
                                              int articleCount,
                                              double numberOfDaysAgo,
                                              List<String> articleTypes,
                                              List<String> articleTypesToExclude)
      throws IOException {
    Calendar threshold = Calendar.getInstance();
    threshold.add(Calendar.SECOND, (int) (-numberOfDaysAgo * SECONDS_PER_DAY));

    UrlParamBuilder params = UrlParamBuilder.params()
        .add("journal", journalKey)
        .add("min", Integer.toString(articleCount))
        .add("since", HttpDateUtil.format(threshold));
    if (articleTypes != null) {
      for (String articleType : articleTypes) {
        params.add("type", articleType);
      }
    }
    if (articleTypesToExclude != null) {
      for (String articleType : articleTypesToExclude) {
        params.add("exclude", articleType);
      }
    }

    return soaService.requestObject("articles?" + params.format(), List.class);
  }

}
