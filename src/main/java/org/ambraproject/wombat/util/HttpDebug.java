package org.ambraproject.wombat.util;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

public class HttpDebug {
  private HttpDebug() {
  }

  private static String enumerationToString(Enumeration enumeration) {
    if (enumeration == null) {
      return null;
    }
    return Iterators.toString(Iterators.forEnumeration(enumeration));
  }

  private static List<String> formatParameters(HttpServletRequest request) {
    List<String> parameterStrings = Lists.newArrayList();
    Map<?, ?> parameterMap = request.getParameterMap();
    if (parameterMap == null) {
      return null;
    }
    for (Object entryObj : parameterMap.entrySet()) {
      Map.Entry<?, ?> entry = (Map.Entry<?, ?>) entryObj;
      String key = (String) entry.getKey();
      String[] value = (String[]) entry.getValue();
      String parameter = String.format("%s=%s", key, Arrays.toString(value));
      parameterStrings.add(parameter);
    }
    return parameterStrings;
  }

  private static List<String> formatHeaders(HttpServletRequest request) {
    List<String> headerStrings = Lists.newArrayList();
    Enumeration headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String name = headerNames.nextElement().toString();
      Object value = request.getAttribute(name);
      String attribute = String.format("%s=%s", name, value);
      headerStrings.add(attribute);
    }
    return headerStrings;
  }

  private static List<String> formatCookies(Cookie[] cookies) {
    if (cookies == null) {
      return null;
    }
    List<String> cookieStrings = Lists.newArrayListWithCapacity(cookies.length);
    for (Cookie cookie : cookies) {
      cookieStrings.add(MoreObjects.toStringHelper(Cookie.class)
          .add("Comment", cookie.getComment())
          .add("Domain", cookie.getDomain())
          .add("MaxAge", cookie.getMaxAge())
          .add("Path", cookie.getPath())
          .add("Secure", cookie.getSecure())
          .add("Name", cookie.getName())
          .add("Value", cookie.getValue())
          .add("Version", cookie.getVersion())
          .toString());
    }
    return cookieStrings;
  }

  /**
   * Query all request fields and return a string containing all values.
   *
   * @param request a request
   * @return the descriptive string
   */
  public static String dump(HttpServletRequest request) {
    return MoreObjects.toStringHelper(HttpServletRequest.class)

        // ServletRequest methods
        .add("CharacterEncoding", request.getCharacterEncoding())
        .add("ContentLength", request.getContentLength())
        .add("ContentType", request.getContentType())
        .add("ParameterMap", formatParameters(request))
        .add("Protocol", request.getProtocol())
        .add("Scheme", request.getScheme())
        .add("ServerName", request.getServerName())
        .add("ServerPort", request.getServerPort())
        .add("RemoteAddr", request.getRemoteAddr())
        .add("RemoteHost", request.getRemoteHost())
        .add("Locale", enumerationToString(request.getLocales()))
        .add("isSecure", request.isSecure())
        .add("RemotePort", request.getRemotePort())
        .add("LocalName", request.getLocalName())
        .add("LocalAddr", request.getLocalAddr())
        .add("LocalPort", request.getLocalPort())

            // HttpServletRequest methods
        .add("AuthType", request.getAuthType())
        .add("Cookies", formatCookies(request.getCookies()))
        .add("Headers", formatHeaders(request))
        .add("Method", request.getMethod())
        .add("PathInfo", request.getPathInfo())
        .add("PathTranslated", request.getPathTranslated())
        .add("ContextPath", request.getContextPath())
        .add("QueryString", request.getQueryString())
        .add("RemoteUser", request.getRemoteUser())
        .add("UserPrincipal", request.getUserPrincipal())
        .add("RequestedSessionId", request.getRequestedSessionId())
        .add("RequestURI", request.getRequestURI())
        .add("RequestURL", request.getRequestURL())
        .add("ServletPath", request.getServletPath())
        .add("Session", request.getSession(false))
        .add("isRequestedSessionIdValid", request.isRequestedSessionIdValid())
        .add("isRequestedSessionIdFromCookie", request.isRequestedSessionIdFromCookie())
        .add("isRequestedSessionIdFromURL", request.isRequestedSessionIdFromURL())

        .toString();
  }

}
