package org.ambraproject.wombat.util;

import javax.servlet.http.HttpServletRequest;

public class HostnameUtil {
  private HostnameUtil() {
    throw new AssertionError("Not instantiable");
  }

  /**
   * Recover a request's client-side hostname.
   * <p/>
   * If the request was forwarded by a proxy that supports the {@code "X-Forwarded-Host"} header, return the hostname
   * stored as that header value. Else, default to {@link HttpServletRequest#getServerName}.
   *
   * @param request a request
   * @return the client-side hostname, according to best available information
   */
  public static String getClientHostname(HttpServletRequest request) {
    String forwardedHost = request.getHeader("X-Forwarded-Host");
    return (forwardedHost != null) ? forwardedHost : request.getServerName();
  }

}
