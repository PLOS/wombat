package org.ambraproject.wombat.config.site.url;

import com.google.common.base.Preconditions;
import org.ambraproject.wombat.util.PathUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;

public class PathTokenScheme implements SiteRequestScheme {

  private final String token;

  public PathTokenScheme(String token) {
    Preconditions.checkArgument(!token.isEmpty());
    this.token = token;
  }

  @Override
  public boolean isForSite(HttpServletRequest request) {
    Iterator<String> path = PathUtil.SPLITTER.split(request.getServletPath()).iterator();
    return path.hasNext() && token.equals(path.next());
  }

  @Override
  public String buildLink(HttpServletRequest request, String path) {
    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    return PathUtil.JOINER.join(request.getContextPath(), token, path);
  }

  @Override
  public String toString() {
    return "PathTokenScheme{" +
        "token='" + token + '\'' +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    return token.equals(((PathTokenScheme) o).token);
  }

  @Override
  public int hashCode() {
    return token.hashCode();
  }

}
