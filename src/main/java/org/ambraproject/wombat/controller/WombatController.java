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

import org.ambraproject.wombat.config.Site;
import org.ambraproject.wombat.config.SiteSet;
import org.ambraproject.wombat.service.ArticleNotFoundException;
import org.ambraproject.wombat.service.UnmatchedSiteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Base class with common functionality for all controllers in the application.
 */
public abstract class WombatController {

  private static final Logger log = LoggerFactory.getLogger(WombatController.class);

  @Autowired
  protected SiteSet siteSet;

  /**
   * Handler invoked for all uncaught exceptions.  Renders a "nice" 500 page.
   *
   * @param e uncaught exception
   * @param request HttpServletRequest
   * @param response HttpServletResponse
   * @return ModelAndView specifying the view
   * @throws IOException
   */
  @ExceptionHandler(Exception.class)
  protected ModelAndView handleException(Exception e, HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    log.error("handleException", e);
    response.setStatus(500);
    Site site = getSiteFromRequest(request);

    // For some reason, methods decorated with @ExceptionHandler cannot accept Model parameters,
    // unlike @RequestMapping methods.  So this is a little different...
    ModelAndView mav = new ModelAndView(site.getKey() + "/ftl/error");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    e.printStackTrace(new PrintStream(baos));

    // No need to close stream since it's a ByteArrayOutputStream.

    mav.addObject("stackTrace", baos.toString("utf-8"));
    return mav;
  }

  /**
   * Directs unhandled ArticleNotFoundExceptions to a 404 page.
   *
   * @param request HttpServletRequest
   * @param response HttpServletResponse
   * @return ModelAndView specifying the view
   */
  @ExceptionHandler(ArticleNotFoundException.class)
  protected ModelAndView handleArticleNotFound(HttpServletRequest request, HttpServletResponse response) {
    response.setStatus(404);
    Site site = getSiteFromRequest(request);

    // TODO: do we want an "article not found" page separate from the generic 404 page?
    ModelAndView mav = new ModelAndView(site.getKey() + "/ftl/notFound");
    return mav;
  }

  /**
   * Attempts to extract the site from the request.  Note that controllers should
   * usually get the site using a @PathVariable("site") annotation on a @RequestMapping
   * method; this method is provided for the rare cases when this is not possible.
   *
   * @param request HttpServletRequest
   * @return the site key, or null if none was found in the request path
   */
  protected Site getSiteFromRequest(HttpServletRequest request) {
    String possibleSite = request.getServletPath().split("/")[1];
    try {
      return siteSet.getSite(possibleSite);
    } catch (UnmatchedSiteException use) {
      return null;
    }
  }
}
