package org.ambraproject.wombat.service;

import com.google.common.base.Preconditions;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.util.RevisionId;

public class RenderContext {

  private final Site site;
  private final RevisionId revisionId;

  public RenderContext(Site site, RevisionId revisionId) {
    this.site = Preconditions.checkNotNull(site);
    this.revisionId = Preconditions.checkNotNull(revisionId);
  }

  public Site getSite() {
    return site;
  }

  public RevisionId getRevisionId() {
    return revisionId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    RenderContext that = (RenderContext) o;

    if (revisionId != null ? !revisionId.equals(that.revisionId) : that.revisionId != null) return false;
    if (!site.equals(that.site)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = site.hashCode();
    result = 31 * result + (revisionId != null ? revisionId.hashCode() : 0);
    return result;
  }
}
