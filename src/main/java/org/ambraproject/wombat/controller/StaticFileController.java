package org.ambraproject.wombat.controller;

import com.google.common.io.Closer;
import org.ambraproject.wombat.config.Theme;
import org.ambraproject.wombat.service.AssetService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class StaticFileController extends WombatController {

  private static final String STATIC_NAMESPACE = "static/";

  /**
   * Path prefix for compiled assets (.js and .css).
   */
  private static final String COMPILED_NAMESPACE = STATIC_NAMESPACE + AssetService.COMPILED_PATH_PREFIX;

  @Autowired
  private AssetService assetService;

  @RequestMapping("/{site}/static/**")
  public void serveStaticContent(HttpServletRequest request, HttpServletResponse response,
                                 HttpSession session, @PathVariable("site") String site)
      throws IOException {
    Theme theme = siteSet.getSite(site).getTheme();

    // Kludge to get "static/**"
    String servletPath = request.getServletPath();
    String filePath = servletPath.substring(site.length() + 2);
    response.setContentType(session.getServletContext().getMimeType(servletPath));
    if (filePath.startsWith(COMPILED_NAMESPACE)) {
      serveCompiledAsset(filePath, request, response);
    } else {
      serveFile(filePath, request, response, theme);
    }
  }

  /**
   * Serves a file provided by a theme.
   *
   * @param filePath the path to the file (relative to the theme)
   * @param response response object
   * @param theme    specifies the theme from which we are loading the file
   * @throws IOException
   */
  private void serveFile(String filePath, HttpServletRequest request, HttpServletResponse response, Theme theme)
      throws IOException {
    Closer closer = Closer.create();
    try {
      InputStream inputStream = theme.getStaticResource(filePath);
      if (inputStream == null) {
        // TODO: Forward to user-friendly 404 page
        response.setStatus(HttpStatus.NOT_FOUND.value());

        // Just for debugging
        closer.register(response.getOutputStream()).write("Not found!".getBytes());
      } else {
        Theme.ResourceAttributes attributes = theme.getResourceAttributes(filePath);

        // We use a "weak" etag, that is, one prepended by "W/".  This means that the resource should be
        // considered semantically-equivalent, but not byte-identical, if the etags match.  It's probably
        // splitting hairs, but this is most appropriate since we don't use a fingerprint of the contents
        // here (instead concatenating length and mtime).  This is what the legacy ambra does for all
        // resources.
        String etag = String.format("W/\"%d-%d\"", attributes.contentLength, attributes.lastModified);
        if (checkIfModifiedSince(request, attributes.lastModified, etag)) {
          response.setHeader("Etag", etag);
          setLastModified(response, attributes.lastModified);
          closer.register(inputStream);
          OutputStream outputStream = closer.register(response.getOutputStream());
          IOUtils.copy(inputStream, outputStream);
        } else {
          response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
          response.setHeader("Etag", etag);
        }
      }
    } catch (Throwable t) {
      throw closer.rethrow(t);
    } finally {
      closer.close();
    }
  }

  private static final Pattern COMPILED_ASSET_PATTERN = Pattern.compile(""
      + "static/compiled/asset_"
      + "([+\\w]+)" // The asset hash in modified base 64. May contain '_' and '+' chars. ('_' replaces '/'; see getFingerprint)
      + "\\.\\w+"); // The file extension.

  /**
   * Serves a .js or .css asset that has already been concatenated and minified. See {@link AssetService} for details on
   * this process.
   *
   * @param filePath the path to the file (relative to the theme)
   * @param response response object
   * @throws IOException
   */
  private void serveCompiledAsset(String filePath, HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    // The hash is already included in the compiled asset's filename, so we take advantage
    // of that here and use it as the etag.
    Matcher matcher = COMPILED_ASSET_PATTERN.matcher(filePath);
    if (!matcher.matches()) {
      throw new IllegalArgumentException(filePath + " is not a valid compiled asset path");
    }
    String basename = filePath.substring(COMPILED_NAMESPACE.length());

    // This is a "strong" etag since it's based on a fingerprint of the contents.
    String etag = String.format("\"%s\"", matcher.group(1));
    long lastModified = assetService.getLastModifiedTime(basename);
    if (checkIfModifiedSince(request, lastModified, etag)) {
      response.setHeader("Etag", etag);
      setLastModified(response, lastModified);
      assetService.serveCompiledAsset(basename, response.getOutputStream());
    } else {
      response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
      response.setHeader("Etag", etag);
    }
  }

  /**
   * Sets the "Last-Modified" header in the response.
   *
   * @param response     HttpServletResponse
   * @param lastModified timestamp to set the header to
   */
  private void setLastModified(HttpServletResponse response, long lastModified) {

    // RFC 1123 date. eg. Tue, 20 May 2008 13:45:26 GMT and always in English.
    SimpleDateFormat fmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
    fmt.setTimeZone(TimeZone.getTimeZone("GMT"));
    response.setHeader("Last-Modified", fmt.format(new Date(lastModified)));
  }

  /**
   * Checks to see if we should serve the contents of the static resource, or just return a 304 response with no body,
   * based on cache-related request headers.
   *
   * @param request      HttpServletRequest we will check for cache headers
   * @param lastModified last modified timestamp of the actual resource on the server
   * @param etag         etag generated from the actual resource on the server
   * @return true if we should serve the bytes of the resource, false if we should return 304.
   */
  private boolean checkIfModifiedSince(HttpServletRequest request, long lastModified, String etag) {

    // Let the Etag-based header take precedence over If-Modified-Since.  This is copied from legacy ambra.
    String etagFromRequest = request.getHeader("If-None-Match");
    if (etagFromRequest != null) {
      return !etagFromRequest.equals(etag);
    } else {
      return lastModified > request.getDateHeader("If-Modified-Since");
    }
  }
}
