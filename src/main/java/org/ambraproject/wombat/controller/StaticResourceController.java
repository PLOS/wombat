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

package org.ambraproject.wombat.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.common.net.HttpHeaders;

import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.config.theme.Theme;
import org.ambraproject.wombat.service.AssetService.AssetUrls;
import org.ambraproject.wombat.util.HttpMessageUtil;
import org.ambraproject.wombat.util.PathUtil;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class StaticResourceController extends WombatController {
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

  @RequestMapping(name = "staticResource", value = "/" + AssetUrls.RESOURCE_NAMESPACE + "/**")
  public void serveResource(HttpServletRequest request, HttpServletResponse response,
                            HttpSession session, @SiteParam Site site)
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
    serveFile(filePath, request, response, theme);
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
}
