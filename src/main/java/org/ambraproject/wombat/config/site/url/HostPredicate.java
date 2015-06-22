package org.ambraproject.wombat.config.site.url;

import com.google.common.base.Preconditions;
import javax.servlet.http.HttpServletRequest;


class HostPredicate implements SiteRequestPredicate {

  private final String hostName;

  HostPredicate(String hostName) {
    Preconditions.checkArgument(!hostName.isEmpty());
    this.hostName = hostName;
  }

  @Override
  public boolean isForSite(HttpServletRequest request) {
    return hostName.equals(request.getServerName());
  }

  @Override
  public String toString() {
    return "HostPredicate{" +
        "host='" + hostName + '\'' +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    return hostName.equals(((HostPredicate) o).hostName);
  }

  @Override
  public int hashCode() {
    return hostName.hashCode();
  }

}
