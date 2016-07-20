package org.ambraproject.wombat.service;

import org.ambraproject.wombat.config.RemoteCacheSpace;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.model.ScholarlyWorkId;
import org.ambraproject.wombat.service.remote.RemoteCacheKey;

import java.util.Objects;
import java.util.Optional;

public class RenderContext {

  private final Site site;
  private final Optional<ScholarlyWorkId> articleId;

  public RenderContext(Site site) {
    this.site = Objects.requireNonNull(site);
    this.articleId = Optional.empty();
  }

  public RenderContext(Site site, ScholarlyWorkId articleId) {
    this.site = Objects.requireNonNull(site);
    this.articleId = Optional.of(articleId);
  }

  public Site getSite() {
    return site;
  }

  public Optional<ScholarlyWorkId> getArticleId() {
    return articleId;
  }

  public RemoteCacheKey getCacheKey(RemoteCacheSpace space) {
    throw new UnsupportedOperationException("TODO: Implement"); // TODO: Implement
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
