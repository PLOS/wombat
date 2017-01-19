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

import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteResolver;
import org.ambraproject.wombat.service.remote.UserApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Handles exceptions for all controllers
 */
@ControllerAdvice
class ExceptionHandlerAdvisor {

  private static final Logger log = LoggerFactory.getLogger(WombatController.class);

  @Autowired
  private SiteResolver siteResolver;
  @Autowired
  private AppRootPage appRootPage;

  /**
   * Handler invoked for all uncaught exceptions.  Renders a "nice" 500 page.
   *
   * @param exception uncaught exception
   * @param request   HttpServletRequest
   * @param response  HttpServletResponse
   * @return ModelAndView specifying the view
   * @throws java.io.IOException
   */
  @ExceptionHandler(Exception.class)
  protected ModelAndView handleException(Exception exception, HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    log.error("handleException", exception);
    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    Site site = siteResolver.resolveSite(request);

    // For some reason, methods decorated with @ExceptionHandler cannot accept Model parameters,
    // unlike @RequestMapping methods.  So this is a little different...
    String viewName = chooseExceptionView(site, exception);
    ModelAndView mav = new ModelAndView(viewName);

    StringWriter stackTrace = new StringWriter();
    exception.printStackTrace(new PrintWriter(stackTrace));
    mav.addObject("stackTrace", stackTrace.toString());

    return mav;
  }

  private String chooseExceptionView(Site site, Exception exception) {
    if (site == null) {
      return "//error";
    } else if (exception instanceof UserApi.UserApiException) {
      log.error("UserApiException", exception);
      return site.getKey() + "/ftl/error/userApiError";
    } else {
      return site.getKey() + "/ftl/error/error";
    }
  }

  /**
   * Directs unhandled exceptions that indicate an invalid URL to a 404 page.
   *
   * @param request  HttpServletRequest
   * @param response HttpServletResponse
   * @return ModelAndView specifying the view
   */
  @ExceptionHandler({MissingServletRequestParameterException.class, NotFoundException.class, NotVisibleException.class,
      NoHandlerFoundException.class})
  protected ModelAndView handleNotFound(HttpServletRequest request, HttpServletResponse response) {
    Site site = siteResolver.resolveSite(request);
    if (site == null && request.getServletPath().equals("/")) {
      response.setHeader("Content-Type", MediaType.TEXT_HTML.toString());
      return appRootPage.serveAppRoot();
    }
    response.setStatus(HttpStatus.NOT_FOUND.value());
    String viewName = (site == null) ? "//notFound" : (site.getKey() + "/ftl/error/notFound");
    return new ModelAndView(viewName);
  }

  @ExceptionHandler(InternalRedirectException.class)
  protected ModelAndView handleRedirectToSite(InternalRedirectException exception, HttpServletRequest request) {
    return new ModelAndView(exception.getLink().getRedirect(request));
  }

}
