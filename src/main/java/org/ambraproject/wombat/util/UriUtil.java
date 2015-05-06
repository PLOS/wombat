package org.ambraproject.wombat.util;

import com.google.common.base.Preconditions;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class UriUtil {
  private UriUtil() {
    throw new AssertionError("Not instantiable");
  }

  /**
   * Builds a complete URI given a URL that specifies the server and a string that is the remainder of the query.
   *
   * @param server  host, port, and optionally part of the path.  For example "http://www.example.com/" or
   *                "https://plos.org/api/".
   * @param address the remainder of the path and query string.  For example "articles/foo.pone.1234567?comments=true"
   * @return a URI to the complete path and query string
   */
  public static URI concatenate(URL server, String address) {
    try {
      return new URL(server, Preconditions.checkNotNull(address)).toURI();
    } catch (MalformedURLException | URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
  }


  public static String stripUrlPrefix(String requestUrl, String namespace) {
    // Strip the url of any  to prepare for forwarding a request
    // The string manipulation is a little ugly here, but the alternative would be have wombat
    // share a bunch of code with rhino in order to extract portions of the servlet path in
    // a way that plays nicely with spring
    // (specifically org.ambraproject.rhino.rest.controller.abstr.RestController).

    return requestUrl.substring(requestUrl.indexOf(namespace) +
            (namespace.startsWith("/") ? 1 : 0));  // Remove first slash where present
  }

}
