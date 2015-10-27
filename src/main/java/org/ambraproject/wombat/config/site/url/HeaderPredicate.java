package org.ambraproject.wombat.config.site.url;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Objects;

class HeaderPredicate implements SiteRequestPredicate {

  private final String headerName;
  private final String requiredValue;

  HeaderPredicate(String headerName, String requiredValue) {
    this.headerName = Objects.requireNonNull(headerName);
    this.requiredValue = Objects.requireNonNull(requiredValue);
  }

  @Override
  public boolean isForSite(HttpServletRequest request) {
    Enumeration headers = request.getHeaders(headerName);
    while (headers.hasMoreElements()) {
      if (requiredValue.equals(headers.nextElement())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return "HeaderPredicate{" +
        "headerName='" + headerName + '\'' +
        ", requiredValue='" + requiredValue + '\'' +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    HeaderPredicate that = (HeaderPredicate) o;

    if (!headerName.equals(that.headerName)) return false;
    if (!requiredValue.equals(that.requiredValue)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = headerName.hashCode();
    result = 31 * result + requiredValue.hashCode();
    return result;
  }

}
