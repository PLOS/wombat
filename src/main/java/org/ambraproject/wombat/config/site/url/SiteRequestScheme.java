package org.ambraproject.wombat.config.site.url;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

/**
 * Encapsulates the scheme by which URLs and other request attributes are mapped onto {@link
 * org.ambraproject.wombat.config.site.Site} objects. Each instance of this class corresponds to a particular {@code
 * Site} (but does not have the {@code Site} as a field -- the composition goes in the other direction).
 * <p/>
 * The class has two jobs: (1) given a request, determine whether the request is for this object's site; and (2) given a
 * path to an application page, format that path as a full link to that page within this object's site.
 */
public class SiteRequestScheme implements SiteRequestPredicate {

  /**
   * The host name, if any, that should be part of links to the site.
   */
  private final Optional<String> hostName;

  /**
   * The site-identifying token, if any, that should be part of links to the site.
   */
  private final Optional<String> pathToken;

  /**
   * A request is for this object's site if and only if all of these predicates are true for the request.
   * <p/>
   * If {@link #pathToken} or {@link #hostName} is present, this must include a {@link PathTokenPredicate} or
   * {@link HostPredicate}, respectively, to be correct. This is the responsibility of {@link Builder#setPathToken} and
   * {@link Builder#specifyHost}.
   */
  private final ImmutableSet<SiteRequestPredicate> requestPredicates;

  private SiteRequestScheme(String hostName, String pathToken, Iterable<? extends SiteRequestPredicate> requestPredicates) {
    this.hostName = Optional.fromNullable(hostName);
    this.pathToken = Optional.fromNullable(pathToken);
    this.requestPredicates = ImmutableSet.copyOf(requestPredicates);
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * A mutable object that gathers the site definition from configuration code.
   */
  public static class Builder {
    private String hostName;
    private String pathToken;
    private final Collection<SiteRequestPredicate> requestPredicates;

    private Builder() {
      requestPredicates = Lists.newArrayListWithCapacity(3);
    }

    /*
     * Expose the various SiteRequestPredicate implementations through public methods here.
     */

    /**
     * Identify the host name for the site
     * @param hostName
     */
    public Builder specifyHost(String hostName) {
      if (this.hostName != null) {
        throw new IllegalStateException("host name has already been set");
      }
      this.hostName = hostName;
      requestPredicates.add(new HostPredicate(hostName));
      return this;
    }

    /**
     * Identify the site by a token that appears at the beginning of the servlet URL.
     *
     * @param pathToken the token
     */
    public Builder setPathToken(String pathToken) {
      if (this.pathToken != null) {
        throw new IllegalStateException("pathToken has already been set");
      }
      this.pathToken = pathToken;
      requestPredicates.add(new PathTokenPredicate(this.pathToken));
      return this;
    }

    /**
     * Identify the site by a header value.
     *
     * @param headerName    the header name
     * @param requiredValue the expected value of the header associated with the site
     */
    public Builder requireHeader(String headerName, String requiredValue) {
      requestPredicates.add(new HeaderPredicate(headerName, requiredValue));
      return this;
    }

    public SiteRequestScheme build() {
      return new SiteRequestScheme(hostName, pathToken, requestPredicates);
    }
  }

  @Override
  public boolean isForSite(HttpServletRequest request) {
    for (SiteRequestPredicate requestPredicate : requestPredicates) {
      if (!requestPredicate.isForSite(request)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Build an absolute link to a path within a site.
   * <p/>
   * The link always starts with {@code 'http(s)://'} and is usable as an absolute link to a path on the current site.
   *
   * @param request a request to the current site
   * @param path    a site-independent path
   * @return a link to that path within the same site
   */
  public String buildLink(HttpServletRequest request, String path) {
    StringBuilder link = new StringBuilder(request.getScheme() + "://");

    if (hostName.isPresent()) {
      link.append(hostName.get());
    } else {
      link.append(request.getServerName());
    }

    if (request.getServerPort() != 80){ // TODO: grab from Java constant?
      link.append(":").append(String.valueOf(request.getServerPort()));
    }

    link.append(request.getContextPath());

    if (pathToken.isPresent()) {
      link.append('/').append(pathToken.get());
    }

    if (!path.startsWith("/")) {
      link.append('/');
    }

    link.append(path);

    return link.toString();
  }

  @Override
  public String toString() {
    return "SiteRequestScheme{" +
        "hostName=" + hostName +
        "pathToken=" + pathToken +
        ", requestPredicates=" + requestPredicates +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SiteRequestScheme that = (SiteRequestScheme) o;

    if (!hostName.equals(that.hostName)) return false;
    if (!pathToken.equals(that.pathToken)) return false;
    if (!requestPredicates.equals(that.requestPredicates)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = hostName.hashCode();
    result = 31 * result + pathToken.hashCode();
    result = 31 * result + requestPredicates.hashCode();
    return result;
  }

}
