package org.ambraproject.wombat.service.remote;

import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.identity.ArticlePointer;
import org.ambraproject.wombat.service.ArticleService;
import org.ambraproject.wombat.util.CacheKey;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;

public class CorpusContentApi extends AbstractContentApi {

  @Autowired
  private ArticleService articleService;

  @Override
  protected String getRepoConfigKey() {
    return "corpus";
  }

  /**
   * Consume an article manuscript.
   *
   * @param articleId    the article whose manuscript to read
   * @param site         the site in which the callback will render the manuscript, for caching purposes
   * @param cachePrefix  the cache space that stores the operation output
   * @param htmlCallback the operation to perform on the manuscript
   * @param <T>          the result type
   * @return the result of the operation
   * @throws IOException
   */
  public <T> T readManuscript(ArticlePointer articleId, Site site, String cachePrefix,
                              CacheDeserializer<InputStream, T> htmlCallback)
      throws IOException {
    CacheKey cacheKey = CacheKey.create(cachePrefix, site.getKey(),
        articleId.getDoi(), Integer.toString(articleId.getIngestionNumber()));
    ContentKey manuscriptKey = articleService.getManuscriptKey(articleId);
    return requestCachedStream(cacheKey, manuscriptKey, htmlCallback);
  }

}
