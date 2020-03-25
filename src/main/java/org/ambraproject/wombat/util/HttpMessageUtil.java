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

import javax.servlet.http.HttpServletRequest;

/**
 * A utility class for creation and management of HTTP messages
 */
public class HttpMessageUtil {
  /**
   * Checks to see if we should serve the contents of the requested object, or just return a 304 response with no body,
   * based on cache-related request headers.
   *
   * @param request      HttpServletRequest we will check for cache headers
   * @param lastModified last modified timestamp of the actual resource on the server
   * @param etag         etag generated from the actual resource on the server
   * @return true if we should serve the bytes of the resource, false if we should return 304.
   */
  public static boolean checkIfModifiedSince(HttpServletRequest request, Long lastModified, String etag) {

    // Let the Etag-based header take precedence over If-Modified-Since.
    String etagFromRequest = request.getHeader("If-None-Match");
    if (etag != null && etagFromRequest != null) {
      return !etagFromRequest.equals(etag);
    } else {
      return lastModified == null || lastModified > request.getDateHeader("If-Modified-Since");
    }
  }

}
