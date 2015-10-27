package org.ambraproject.wombat.util;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * The server endpoint as seen from the client side.
 */
public class ClientEndpoint {

  private final String hostname;
  private final Optional<Integer> port;

  private ClientEndpoint(String hostname, Optional<Integer> port) {
    Preconditions.checkArgument(!hostname.isEmpty());
    this.hostname = hostname;

    Preconditions.checkArgument(!port.isPresent() || (port.get() >= 0 && port.get() < 0x10000));
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
   * <p>
   * If the original request had a {@code "X-Forwarded-Host"} header, the return value is present only if header
   * provided a port number after the hostname.
   * <p>
   * If the original request did not have a {@code "X-Forwarded-Host"} header, the return value is absent if the port
   * number was the default (80 for HTTP; 443 for HTTPS).
   *
   * @return the port number if not default
   */
  public Optional<Integer> getPort() {
    return port;
  }


  /**
   * Recover a request's client-side hostname and port.
   * <p>
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
        return new ClientEndpoint(parts.get(0), Optional.<Integer>absent());
      case 2:
        return new ClientEndpoint(parts.get(0), Optional.of(Integer.valueOf(parts.get(1))));
      default:
        throw new IllegalArgumentException();
    }
  }

  private static int getDefaultPort(HttpServletRequest request) {
    return request.isSecure() ? 443 : 80;
  }

  private static ClientEndpoint extract(HttpServletRequest request) {
    int requestPort = request.getServerPort();
    Optional<Integer> port = (requestPort == getDefaultPort(request)) ? Optional.<Integer>absent() : Optional.of(requestPort);
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
