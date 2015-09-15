package org.ambraproject.wombat.controller;

import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteResolver;
import org.ambraproject.wombat.config.site.SiteSet;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Handles exceptions for all controllers
 */
@ControllerAdvice
class ExceptionHandlerAdvisor {

  private static final Logger log = LoggerFactory.getLogger(WombatController.class);

  @Autowired
  private SiteSet siteSet;
  @Autowired
  private SiteResolver siteResolver;
  @Autowired
  private ServletContext servletContext;

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
    String viewName = (site == null) ? "//error" : (site.getKey() + "/ftl/error");
    ModelAndView mav = new ModelAndView(viewName);

    StringWriter stackTrace = new StringWriter();
    exception.printStackTrace(new PrintWriter(stackTrace));
    mav.addObject("stackTrace", stackTrace.toString());

    return mav;
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
      return serveAppRoot();
    }
    response.setStatus(HttpStatus.NOT_FOUND.value());
    String viewName = (site == null) ? "//notFound" : (site.getKey() + "/ftl/notFound");
    return new ModelAndView(viewName);
  }

  /**
   * Show a page in response to the application root.
   * <p/>
   * This is here only for development/debugging: if you browse to the application root while you're setting up, this
   * page is more useful than an error message. But all end-user-facing pages should belong to one of the sites in
   * {@code siteSet}.
   */
  private ModelAndView serveAppRoot() {
    ModelAndView mav = new ModelAndView("//approot");
    mav.addObject("siteKeys", siteSet.getSiteKeys());
    try {
      mav.addObject("imageCode", getResourceAsBase64("/WEB-INF/themes/root/app/wombat.jpg"));
    } catch (IOException e) {
      log.error("Error displaying root page image", e);
    }
    return mav;
  }

  private String getResourceAsBase64(String path) throws IOException {
    byte[] bytes;
    try (InputStream stream = servletContext.getResourceAsStream(path)) {
      bytes = IOUtils.toByteArray(stream);
    }
    return Base64.encodeBase64String(bytes);
  }
}
