package org.ambraproject.wombat.service;

import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.identity.ArticlePointer;

import java.util.Objects;

public class RenderContext {

  private final Site site;
  private final ArticlePointer articleId;

  public RenderContext(Site site, ArticlePointer articleId) {
    this.site = Objects.requireNonNull(site);
    this.articleId = Objects.requireNonNull(articleId);
  }

  public Site getSite() {
    return site;
  }

  public ArticlePointer getArticleId() {
    return articleId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    RenderContext that = (RenderContext) o;

    if (!articleId.equals(that.articleId)) return false;
    if (!site.equals(that.site)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = site.hashCode();
    result = 31 * result + articleId.hashCode();
    return result;
  }
}
