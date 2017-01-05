package org.ambraproject.wombat.controller;

import com.google.common.net.HttpHeaders;
import org.ambraproject.wombat.config.site.MappingSiteScope;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteScope;
import org.ambraproject.wombat.config.theme.Theme;
import org.ambraproject.wombat.service.AssetService;
import org.ambraproject.wombat.service.AssetService.AssetUrls;
import org.ambraproject.wombat.util.HttpMessageUtil;
import org.ambraproject.wombat.util.PathUtil;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class StaticResourceController extends WombatController {

  /**
   * Path prefix for compiled assets (.js and .css).
   */
  private static final String COMPILED_NAMESPACE = AssetUrls.RESOURCE_NAMESPACE + '/' + AssetUrls.COMPILED_PATH_PREFIX;

  @Autowired
  private AssetService assetService;

  /**
   * Return a portion of a path from a given token forward.
   *
   * @param path  a path containing slash-separated tokens
   * @param token the token to look for
   * @return a slash-separated substring of path containing {@code token} and everything after it
   * @throws IllegalArgumentException if {@code token} is not in {@code path}
   */
  private static String pathFrom(String path, String token) {
    List<String> pathTokens = PathUtil.SPLITTER.splitToList(path);
    int index = pathTokens.indexOf(token);
    if (index < 0) {
      throw new IllegalArgumentException(String.format("\"%s\" not found in %s", token, pathTokens));
    }
    List<String> targetTokens = pathTokens.subList(index, pathTokens.size());
    return PathUtil.JOINER.join(targetTokens);
  }

  @MappingSiteScope(value = {SiteScope.JOURNAL_SPECIFIC, SiteScope.JOURNAL_NEUTRAL})
  @RequestMapping(name = "staticResource", value = "/" + AssetUrls.RESOURCE_NAMESPACE + "/**")
  public void serveResource(HttpServletRequest request, HttpServletResponse response,
                            HttpSession session, Site site)
      throws IOException {
    Theme theme = site.getTheme();

    // Kludge to get "resource/**"
    String servletPath = request.getRequestURI();
    String filePath = pathFrom(servletPath, AssetUrls.RESOURCE_NAMESPACE);
    if (filePath.length() <= AssetUrls.RESOURCE_NAMESPACE.length() + 1) {
      throw new NotFoundException(); // in case of a request to "resource/" root
    }

    if (corsEnabled(site, filePath)) {
      response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
    }

    response.setContentType(session.getServletContext().getMimeType(servletPath));
    if (filePath.startsWith(COMPILED_NAMESPACE)) {
      serveCompiledAsset(filePath, request, response);
    } else {
      serveFile(filePath, request, response, theme);
    }
  }

  private static boolean corsEnabled(Site site, String filePath) throws IOException {
    Map<String, Object> resourceConfig = site.getTheme().getConfigMap("resource");
    List<String> corsPrefixes = (List<String>) resourceConfig.get("cors");
    if (corsPrefixes == null) return false;

    String resourceName = filePath.substring(AssetUrls.RESOURCE_NAMESPACE.length() + 1);
    for (String prefix : corsPrefixes) {
      if (resourceName.startsWith(prefix)) {
        return true;
      }
    }
    return false;
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
    try (InputStream inputStream = theme.getStaticResource(filePath)) {
      if (inputStream == null) {
        throw new NotFoundException();
      } else {
        Theme.ResourceAttributes attributes = theme.getResourceAttributes(filePath);

        // We use a "weak" etag, that is, one prepended by "W/".  This means that the resource should be
        // considered semantically-equivalent, but not byte-identical, if the etags match.  It's probably
        // splitting hairs, but this is most appropriate since we don't use a fingerprint of the contents
        // here (instead concatenating length and mtime).  This is what the legacy ambra does for all
        // resources.
        String etag = String.format("W/\"%d-%d\"", attributes.getContentLength(), attributes.getLastModified());
        if (HttpMessageUtil.checkIfModifiedSince(request, attributes.getLastModified(), etag)) {
          response.setHeader("Etag", etag);
          response.setDateHeader(HttpHeaders.LAST_MODIFIED, attributes.getLastModified());
          try (OutputStream outputStream = response.getOutputStream()) {
            IOUtils.copy(inputStream, outputStream);
          }
        } else {
          response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
          response.setHeader("Etag", etag);
        }
      }
    } catch (FileNotFoundException e) {
      // In case filePath refers to a directory
      throw new NotFoundException(e);
    }
  }

  private static final Pattern COMPILED_ASSET_PATTERN = Pattern.compile(""
      + COMPILED_NAMESPACE + AssetUrls.COMPILED_NAME_PREFIX
      + "(\\w+)" // The asset hash in base 32
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
    if (HttpMessageUtil.checkIfModifiedSince(request, lastModified, etag)) {
      response.setHeader("Etag", etag);
      response.setDateHeader(HttpHeaders.LAST_MODIFIED, lastModified);
      assetService.serveCompiledAsset(basename, response.getOutputStream());
    } else {
      response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
      response.setHeader("Etag", etag);
    }
  }

}
