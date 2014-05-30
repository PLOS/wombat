package org.ambraproject.wombat.service;

import com.google.common.base.Preconditions;
import org.ambraproject.rhombat.HttpDateUtil;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.service.remote.SoaService;
import org.ambraproject.wombat.util.UrlParamBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.RandomAccess;

public class RecentArticleServiceImpl implements RecentArticleService {
  private static final Logger log = LoggerFactory.getLogger(RecentArticleServiceImpl.class);

  @Autowired
  private SoaService soaService;

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
   * The argument {@code sequence} must be mutable and {@link RandomAccess}. Its order will be clobbered by this method,
   * and the returned list will be a view into it.
   * <p/>
   * This is more efficient than using {@link Collections#shuffle(List)} if {@code n} is much smaller than {@code
   * sequence.size()}, as it shuffles only part of the list.
   *
   * @param sequence a mutable list of elements
   * @param n        the number of elements to select and return shuffled
   * @return a list of {@code n} elements selected at random from {@code sequence} and in a random order
   */
  private <T> List<T> shuffleSubset(List<T> sequence, int n) {
    final int size = sequence.size();
    Preconditions.checkArgument(size >= n);

    if (!(sequence instanceof RandomAccess)) {
      log.warn("Expected list to be RandomAccess; re-creating as ArrayList");
      sequence = new ArrayList<>(sequence);
    }

    for (int i = 0; i < n; i++) {
      int swapTarget = i + random.nextInt(size - i);
      Collections.swap(sequence, i, swapTarget);
    }
    return sequence.subList(0, n);
  }

  @Override
  public List<Object> getRecentArticles(Site site,
                                        int articleCount,
                                        double numberOfDaysAgo,
                                        boolean shuffle,
                                        List<String> articleTypes)
      throws IOException {
    String journalKey = site.getJournalKey();
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

    Class<ArrayList> responseClass = ArrayList.class; // need ArrayList for shuffleSubset
    List<Object> articles = soaService.requestObject("articles?" + params.format(), responseClass);

    if (articles.size() > articleCount) {
      articles = shuffle ? shuffleSubset(articles, articleCount) : articles.subList(0, articleCount);
    }
    return articles;
  }

}
