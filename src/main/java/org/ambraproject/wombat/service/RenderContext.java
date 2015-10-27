package org.ambraproject.wombat.service;

import org.ambraproject.wombat.config.site.Site;

import java.util.Objects;

public class RenderContext {

  private final Site site;
  private String articleId;

  public RenderContext(Site site) {
    this.site = Objects.requireNonNull(site);
  }

  public Site getSite() {
    return site;
  }

  public String getArticleId() {
    return articleId;
  }

  public void setArticleId(String articleId) {
    this.articleId = articleId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    RenderContext that = (RenderContext) o;

    if (articleId != null ? !articleId.equals(that.articleId) : that.articleId != null) return false;
    if (!site.equals(that.site)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = site.hashCode();
    result = 31 * result + (articleId != null ? articleId.hashCode() : 0);
    return result;
  }
}
