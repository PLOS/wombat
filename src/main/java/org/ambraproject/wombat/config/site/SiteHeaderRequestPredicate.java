package org.ambraproject.wombat.config.site;

import com.google.common.base.Preconditions;

import javax.servlet.http.HttpServletRequest;

class SiteHeaderRequestPredicate implements SiteRequestPredicate {

  private final String headerName;
  private final String expectedValue;

  SiteHeaderRequestPredicate(String headerName, String expectedValue) {
    this.headerName = Preconditions.checkNotNull(headerName);
    this.expectedValue = Preconditions.checkNotNull(expectedValue);
  }

  @Override
  public boolean isForSite(HttpServletRequest request) {
    String headerValue = request.getHeader(headerName);
    return expectedValue.equals(headerValue);
  }

  @Override
  public String toString() {
    return "SiteHeaderRequestPredicate{" +
        "headerName='" + headerName + '\'' +
        ", expectedValue='" + expectedValue + '\'' +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SiteHeaderRequestPredicate that = (SiteHeaderRequestPredicate) o;

    if (!headerName.equals(that.headerName)) return false;
    if (!expectedValue.equals(that.expectedValue)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = headerName.hashCode();
    result = 31 * result + expectedValue.hashCode();
    return result;
  }

}
