package org.ambraproject.wombat.util;

import com.google.common.base.Joiner;
import org.apache.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.List;

/**
 * Utilities for applying Perlbal's reproxying protocol to HTTP requests.
 */
// TODO: Move to Rhombat; unify with Rhino's AssetFileCrudController
public final class ReproxyUtil {
  private ReproxyUtil() {
    throw new AssertionError("Not instantiable");
  }

  // TODO: Pull in code that depends on these values and make them private
  // See: WombatController.ASSET_RESPONSE_HEADER_FILTER, FigureImageController.serveAssetFile, IndirectFileController.serve
  public static final String X_PROXY_CAPABILITIES = "X-Proxy-Capabilities";
  public static final String REPROXY_FILE = "reproxy-file";
  public static final String X_REPROXY_URL = "X-Reproxy-URL";
  public static final String X_REPROXY_CACHE_FOR = "X-Reproxy-Cache-For";

  private static final Joiner REPROXY_URL_JOINER = Joiner.on(' ');

  /**
   * Check whether a request supports reproxying.
   *
   * @param request a request
   * @return {@code true} if it supports reproxying
   */
  private static boolean supportsReproxy(HttpServletRequest request) {
    Enumeration headers = request.getHeaders(X_PROXY_CAPABILITIES);
    if (headers != null) {
      while (headers.hasMoreElements()) {
        if (REPROXY_FILE.equals(headers.nextElement())) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Apply reproxying information for a requested asset to a response.
   * <p>
   * The request may not support the reproxying protocol, and there may not be any reproxy URLs for the asset. In either
   * case, the response is not touched. The return value indicates this.
   * <p>
   * Note that both {@code request} and {@code response} are for the client. Neither is for the service that reproxies
   * the asset. It is assumed that the caller has already queried the service that reproxies the asset, which provides
   * the {@code reproxyUrls} argument.
   *
   * @param request     a request from the client
   * @param response    a response to the client
   * @param reproxyUrls a list of available reproxy URLs for the requested asset (empty and {@code null} are allowed,
   *                    indicating none are available)
   * @param cacheFor    number of seconds to instruct the client to cache the reproxy information
   * @return {@code true} if the response was filled with reproxy information, in which case nothing should be written
   * to the response body; {@code false} if not, in which case the response body should be written
   */
  public static boolean applyReproxy(HttpServletRequest request,
                                     HttpServletResponse response,
                                     List<String> reproxyUrls,
                                     int cacheFor) {
    if (reproxyUrls != null && !reproxyUrls.isEmpty() && supportsReproxy(request)) {
      response.setStatus(HttpStatus.SC_OK);
      response.setHeader(X_REPROXY_URL, REPROXY_URL_JOINER.join(reproxyUrls));
      response.setHeader(X_REPROXY_CACHE_FOR, cacheFor + "; Last-Modified Content-Type Content-Disposition");
      return true;
    }
    return false;
  }

}
