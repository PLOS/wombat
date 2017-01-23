/*
 * Copyright (c) 2017 Public Library of Science
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package org.ambraproject.wombat.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.OptionalInt;

/**
 * The server endpoint as seen from the client side.
 */
public class ClientEndpoint {

  private final String hostname;
  private final OptionalInt port;

  private ClientEndpoint(String hostname, OptionalInt port) {
    Preconditions.checkArgument(!hostname.isEmpty());
    this.hostname = hostname;

    Preconditions.checkArgument(!port.isPresent() || (port.getAsInt() >= 0 && port.getAsInt() < 0x10000));
    this.port = port;
  }

  /**
   * @return the client-side hostname
   */
  public String getHostname() {
    return hostname;
  }

  /**
   * Get the client-side port. The return value is present only if the port is generally needed to build a link.
   * <p/>
   * If the original request had a {@code "X-Forwarded-Host"} header, the return value is present only if header
   * provided a port number after the hostname.
   * <p/>
   * If the original request did not have a {@code "X-Forwarded-Host"} header, the return value is absent if the port
   * number was the default (80 for HTTP; 443 for HTTPS).
   *
   * @return the port number if not default
   */
  public OptionalInt getPort() {
    return port;
  }


  /**
   * Recover a request's client-side URL.
   * <p>
   * If the request was forwarded by a proxy that supports the {@code "X-Forwarded-Host"} header, substitute the
   * hostname and port provided by that header value. Else, return the raw request URL.
   *
   * @param request a request
   * @return the client-side view of the URL, according to best available information
   */
  public static String getRequestUrl(HttpServletRequest request) {
    ClientEndpoint endpoint = get(request);
    URL requestUrl;
    try {
      requestUrl = new URL(request.getRequestURL().toString()); // does not have query string
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }

    String protocol = requestUrl.getProtocol();
    String file = requestUrl.getFile();
    String host = endpoint.getHostname();
    OptionalInt port = endpoint.getPort();

    URL clientUrl;
    try {
      clientUrl = port.isPresent()
          ? new URL(protocol, host, port.getAsInt(), file)
          : new URL(protocol, host, file);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }

    String requestQuery = request.getQueryString();
    return (requestQuery == null) ? clientUrl.toString() : clientUrl + "?" + requestQuery;
  }

  /**
   * Recover a request's client-side hostname and port.
   * <p/>
   * If the request was forwarded by a proxy that supports the {@code "X-Forwarded-Host"} header, return the hostname
   * and port provided by that header value. Else, return the values stored in the request object.
   *
   * @param request a request
   * @return the client-side endpoint, according to best available information
   */
  public static ClientEndpoint get(HttpServletRequest request) {
    String forwardedHost = request.getHeader("X-Forwarded-Host");
    return (forwardedHost != null) ? parse(forwardedHost) : extract(request);
  }

  private static final Splitter ON_COLON = Splitter.on(':');

  private static ClientEndpoint parse(String forwardedHost) {
    List<String> parts = ON_COLON.splitToList(forwardedHost);
    switch (parts.size()) {
      case 1:
        return new ClientEndpoint(parts.get(0), OptionalInt.empty());
      case 2:
        return new ClientEndpoint(parts.get(0), OptionalInt.of(Integer.parseInt(parts.get(1))));
      default:
        throw new IllegalArgumentException();
    }
  }

  private static int getDefaultPort(HttpServletRequest request) {
    return request.isSecure() ? 443 : 80;
  }

  private static ClientEndpoint extract(HttpServletRequest request) {
    int requestPort = request.getServerPort();
    OptionalInt port = (requestPort == getDefaultPort(request)) ? OptionalInt.empty() : OptionalInt.of(requestPort);
    return new ClientEndpoint(request.getServerName(), port);
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ClientEndpoint that = (ClientEndpoint) o;

    if (!hostname.equals(that.hostname)) return false;
    if (!port.equals(that.port)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = hostname.hashCode();
    result = 31 * result + port.hashCode();
    return result;
  }

}
