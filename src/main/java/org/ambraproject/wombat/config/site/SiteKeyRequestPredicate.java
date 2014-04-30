package org.ambraproject.wombat.config.site;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.ambraproject.wombat.util.PathUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;

class SiteKeyRequestPredicate implements SiteRequestPredicate {
  private final String key;

  SiteKeyRequestPredicate(String key) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(key));
    this.key = key;
  }

  @Override
  public boolean isForSite(HttpServletRequest request) {
    Iterator<String> path = PathUtil.SPLITTER.split(request.getServletPath()).iterator();
    return path.hasNext() && key.equals(path.next());
  }

  @Override
  public String toString() {
    return "SiteKeyRequestPredicate{" +
        "key='" + key + '\'' +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    return key.equals(((SiteKeyRequestPredicate) o).key);

  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }

}
