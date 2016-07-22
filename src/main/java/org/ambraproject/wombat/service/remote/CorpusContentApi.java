package org.ambraproject.wombat.service.remote;

import org.ambraproject.wombat.config.RemoteCacheSpace;
import org.ambraproject.wombat.service.ArticleService;
import org.ambraproject.wombat.service.RenderContext;
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
   * @param cacheSpace   the cache space that stores the operation output
   * @param htmlCallback the operation to perform on the manuscript
   * @return the result of the operation
   * @throws IOException
   */
  public String readManuscript(RenderContext articleId, RemoteCacheSpace cacheSpace,
                               CacheDeserializer<InputStream, String> htmlCallback)
      throws IOException {
    RemoteCacheKey cacheKey = articleId.getCacheKey(cacheSpace);
    ContentKey manuscriptKey = articleService.getManuscriptKey(articleId.getArticleId().get());
    return requestCachedStream(cacheKey, manuscriptKey, htmlCallback);
  }

}
