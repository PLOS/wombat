package org.ambraproject.wombat.service;

import org.ambraproject.wombat.config.site.Site;

import java.io.IOException;
import java.util.List;

public interface RecentArticleService {

  public abstract List<Object> getRecentArticles(Site site,
                                                 int articleCount,
                                                 double shuffleFromDaysAgo,
                                                 List<String> articleTypes)
      throws IOException;

}
