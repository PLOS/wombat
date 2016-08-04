package org.ambraproject.wombat.service;

import org.ambraproject.wombat.config.RemoteCacheSpace;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.ambraproject.wombat.service.remote.RemoteCacheKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class RenderContext {

  private final Site site;
  private final Optional<RequestedDoiVersion> articleId;

  public RenderContext(Site site, RequestedDoiVersion articleId) {
    this.site = Objects.requireNonNull(site);
    this.articleId = Optional.of(articleId);
  }

  public Site getSite() {
    return site;
  }

  public Optional<RequestedDoiVersion> getArticleId() {
    return articleId;
  }

  public RemoteCacheKey getCacheKey(RemoteCacheSpace space) {
    List<String> keyTokens = new ArrayList<>(4);
    keyTokens.add(site.getKey());

    this.articleId.ifPresent(articleId -> {
      keyTokens.add(articleId.getDoi());

      articleId.getIngestionNumber().ifPresent(ingestionNumber -> {
        keyTokens.add("ingestion");
        keyTokens.add(Integer.toString(ingestionNumber));
      });

      articleId.getRevisionNumber().ifPresent(revisionNumber -> {
        keyTokens.add("revision");
        keyTokens.add(Integer.toString(revisionNumber));
      });
    });

    return RemoteCacheKey.create(space, keyTokens);
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
