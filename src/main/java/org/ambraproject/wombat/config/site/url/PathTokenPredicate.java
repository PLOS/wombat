package org.ambraproject.wombat.config.site.url;

import com.google.common.base.Preconditions;
import org.ambraproject.wombat.util.PathUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;

class PathTokenPredicate implements SiteRequestPredicate {

  private final String token;

  PathTokenPredicate(String token) {
    Preconditions.checkArgument(!token.isEmpty());
    this.token = token;
  }

  @Override
  public boolean isForSite(HttpServletRequest request) {
    Iterator<String> path = PathUtil.SPLITTER.split(request.getServletPath()).iterator();
    return path.hasNext() && token.equals(path.next());
  }

  @Override
  public String toString() {
    return "PathTokenPredicate{" +
        "token='" + token + '\'' +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    return token.equals(((PathTokenPredicate) o).token);
  }

  @Override
  public int hashCode() {
    return token.hashCode();
  }

}
