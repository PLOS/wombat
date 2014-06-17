/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2013 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.wombat.controller;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;
import com.google.common.net.HttpHeaders;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.site.UnresolvedSiteException;
import org.ambraproject.wombat.service.remote.AssetServiceResponse;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base class with common functionality for all controllers in the application.
 */
public abstract class WombatController {

  private static final Logger log = LoggerFactory.getLogger(WombatController.class);

  @Autowired
  protected SiteSet siteSet;
  @Autowired
  private SiteResolver siteResolver;

  /**
   * Handler invoked for all uncaught exceptions.  Renders a "nice" 500 page.
   *
   * @param exception uncaught exception
   * @param request   HttpServletRequest
   * @param response  HttpServletResponse
   * @return ModelAndView specifying the view
   * @throws IOException
   */
  @ExceptionHandler(Exception.class)
  protected ModelAndView handleException(Exception exception, HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    log.error("handleException", exception);
    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    Site site = siteResolver.resolveSite(request);

    // For some reason, methods decorated with @ExceptionHandler cannot accept Model parameters,
    // unlike @RequestMapping methods.  So this is a little different...
    String viewName = (site == null) ? "//error" : (site.getKey() + "/ftl/error");
    ModelAndView mav = new ModelAndView(viewName);

    StringWriter stackTrace = new StringWriter();
    exception.printStackTrace(new PrintWriter(stackTrace));
    mav.addObject("stackTrace", stackTrace.toString());

    return mav;
  }

  @ExceptionHandler(UnresolvedSiteException.class)
  protected String handleUnresolvedSite(HttpServletResponse response) {
    response.setStatus(HttpStatus.NOT_FOUND.value());
    return "//notFound";
  }

  /**
   * Directs unhandled exceptions that indicate an invalid URL to a 404 page.
   *
   * @param request  HttpServletRequest
   * @param response HttpServletResponse
   * @return ModelAndView specifying the view
   */
  @ExceptionHandler({MissingServletRequestParameterException.class, NotFoundException.class, NotVisibleException.class})
  protected String handleNotFound(HttpServletRequest request, HttpServletResponse response) {
    response.setStatus(HttpStatus.NOT_FOUND.value());
    Site site = siteResolver.resolveSite(request);
    return (site == null) ? "//notFound" : (site.getKey() + "/ftl/notFound");
  }

  /**
   * Validate that an article ought to be visible to the user. If not, throw an exception indicating that the user
   * should see a 404.
   * <p/>
   * An article may be invisible if it is not in a published state, or if it has not been published in a journal
   * corresponding to the site.
   *
   * @param site            the site on which the article was queried
   * @param articleMetadata the article metadata, or a subset containing the {@code state} and {@code journals} fields
   * @throws NotVisibleException if the article is not visible on the site
   */
  protected void validateArticleVisibility(Site site, Map<?, ?> articleMetadata) {
    String state = (String) articleMetadata.get("state");
    if (!"published".equals(state)) {
      throw new NotVisibleException("Article is in unpublished state: " + state);
    }

    Set<String> articleJournalKeys = ((Map<String, ?>) articleMetadata.get("journals")).keySet();
    String siteJournalKey = site.getJournalKey();
    if (!articleJournalKeys.contains(siteJournalKey)) {
      throw new NotVisibleException("Article is not published in: " + site);
    }
  }

  /**
   * Check that a request parameter is not empty.
   * <p/>
   * This is useful for validating that the user didn't supply an empty string as a URL parameter, such as by typing
   * ".../article?doi" into the browser bar when ".../article?id=10.0/foo" is expected. The {@code required} argument on
   * {@code RequestParam} merely guarantees the parameter to be non-null, not non-empty.
   *
   * @param parameter a non-null value supplied as a {@code RequestParam}
   * @throws NotFoundException    if the parameter is empty
   * @throws NullPointerException if the parameter is null
   */
  protected static void requireNonemptyParameter(String parameter) {
    if (parameter.isEmpty()) {
      throw new NotFoundException("Required parameter not supplied");
    }
  }

  /**
   * Interpret a URL parameter as a boolean. In general, interpret {@code null} as false and all non-null strings,
   * including the empty string, as true. But the string {@code "false"} is (case-insensitively) false.
   * <p/>
   * The empty string is true because it represents a URL parameter as being present but with no value, e.g. {@code
   * http://example.com/page?foo}. Contrast {@link Boolean#valueOf(String)}, which returns false for the empty string.
   *
   * @param parameterValue a URL parameter value
   * @return the boolean value
   */
  protected static boolean booleanParameter(String parameterValue) {
    return (parameterValue != null) && !Boolean.toString(false).equalsIgnoreCase(parameterValue);
  }


  // Inconsistent with equals. See Javadoc for java.util.SortedSet.
  private static ImmutableSortedSet<String> caseInsensitiveImmutableSet(String... strings) {
    return ImmutableSortedSet.copyOf(String.CASE_INSENSITIVE_ORDER, Arrays.asList(strings));
  }

  /**
   * Names of headers that, on an asset request from the client, should be passed through on our request to the service
   * tier (Rhino or Content Repo).
   */
  private static final ImmutableSet<String> ASSET_REQUEST_HEADER_WHITELIST = caseInsensitiveImmutableSet("X-Proxy-Capabilities");

  /**
   * Names of headers that, in an asset response from the service tier (Rhino or Content Repo), should be passed through
   * to the client.
   */
  private static final ImmutableSet<String> ASSET_RESPONSE_HEADER_WHITELIST = caseInsensitiveImmutableSet(
      HttpHeaders.CONTENT_TYPE, HttpHeaders.CONTENT_DISPOSITION, "X-Reproxy-URL", "X-Reproxy-Cache-For");

  /**
   * Copy headers from an asset request if they are whitelisted. This effectively translates from the javax.servlet
   * model (used by Spring model) into the Apache model.
   *
   * @param request a request
   * @return its headers
   */
  protected static Header[] copyAssetRequestHeaders(HttpServletRequest request) {
    Enumeration headerNames = request.getHeaderNames();
    List<Header> headers = Lists.newArrayList();
    while (headerNames.hasMoreElements()) {
      String headerName = (String) headerNames.nextElement();
      if (ASSET_REQUEST_HEADER_WHITELIST.contains(headerName)) {
        String headerValue = request.getHeader(headerName);
        headers.add(new BasicHeader(headerName, headerValue));
      }
    }
    return headers.toArray(new Header[headers.size()]);
  }

  /**
   * Copy headers, if their names are whitelisted, from a response from a service that provides an asset.
   *
   * @param responseFromService
   * @param responseToClient
   */
  protected static void copyAssetServiceResponse(AssetServiceResponse responseFromService, HttpServletResponse responseToClient)
      throws IOException {
    for (Header header : responseFromService.getAllHeaders()) {
      if (ASSET_RESPONSE_HEADER_WHITELIST.contains(header.getName())) {
        responseToClient.setHeader(header.getName(), header.getValue());
      }
    }

    try (InputStream streamFromService = responseFromService.openStream();
         OutputStream streamToClient = responseToClient.getOutputStream()) {
      IOUtils.copy(streamFromService, streamToClient);
    }
  }

}
