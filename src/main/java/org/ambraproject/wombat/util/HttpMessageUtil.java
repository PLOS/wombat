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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHeader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

/**
 * A utility class for creation and management of HTTP messages
 */
public class HttpMessageUtil {

  /**
   * Describes how to filter or modify a header while copying a response.
   */
  @FunctionalInterface
  public static interface HeaderFilter {
    /**
     * Return the header value to copy into the outgoing response, under the same header name as the incoming response.
     * Return {@code null} to copy no header with this name.
     * <p/>
     * To copy the header value unchanged, do {@code return header.getValue();}.
     *
     * @param header a header for an incoming response
     * @return the header value to copy into the outgoing response, or {@code null} to not copy this header
     */
    public String getValue(Header header);
  }

  /**
   * Copy content with whitelisted headers between responses. Headers already written to {@code responseTo} are
   * <em>not</em> overwritten if {@code responseFrom} has a header with the same name.
   *
   * @param responseFrom an incoming response to copy from
   * @param responseTo   an outgoing response to copy into
   * @param headerFilter describes whether and how to copy headers
   * @throws IOException
   */
  public static void copyResponseWithHeaders(HttpResponse responseFrom, HttpServletResponse responseTo,
                                             HeaderFilter headerFilter)
      throws IOException {
    for (Header header : responseFrom.getAllHeaders()) {
      String headerName = header.getName();
      if (!responseTo.containsHeader(headerName)) {
        String newValue = headerFilter.getValue(header);
        if (newValue != null) {
          responseTo.setHeader(headerName, newValue);
        }
      }
    }
    copyResponse(responseFrom, responseTo);
  }

  /**
   * Copy content between responses
   *
   * @param responseFrom
   * @param responseTo
   * @throws IOException
   */
  public static void copyResponse(HttpResponse responseFrom, HttpServletResponse responseTo)
      throws IOException {

    try (InputStream streamFromService = responseFrom.getEntity().getContent();
         OutputStream streamToClient = responseTo.getOutputStream()) {
      IOUtils.copy(streamFromService, streamToClient);
    }
  }

  /**
   * Return a list of headers from a request, using an optional whitelist
   *
   * @param request a request
   * @return its headers
   */
  public static Collection<Header> getRequestHeaders(HttpServletRequest request, ImmutableSet<String> headerWhitelist) {
    Enumeration headerNames = request.getHeaderNames();
    List<Header> headers = Lists.newArrayList();
    while (headerNames.hasMoreElements()) {
      String headerName = (String) headerNames.nextElement();
      if (headerWhitelist.contains(headerName)) {
        String headerValue = request.getHeader(headerName);
        headers.add(new BasicHeader(headerName, headerValue));
      }
    }
    return headers;
  }


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
