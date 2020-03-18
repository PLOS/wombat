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

package org.ambraproject.wombat.config.site;

import org.ambraproject.wombat.util.HttpDebug;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

/**
 * Bean that resolves requests to the sites that the application hosts.
 * <p/>
 * The sites themselves, and the definitions of how to resolve requests to individual sites, are encapsulated in the
 * {@link SiteSet} bean. This object exposes them to the view layer.
 */
public class SiteResolver implements HandlerMethodArgumentResolver {
  private static final Logger log = LogManager.getLogger(SiteResolver.class);

  @Autowired
  private SiteSet siteSet;

  /**
   * Apply to every controller parameter annotated with {@code @SiteParam}, which should be of type {@link Site}.
   */
  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.getParameterAnnotation(SiteParam.class) != null;
  }

  /**
   * Resolve the site from the request and store it in the model as {@code "site"}.
   *
   * @see {@link #resolveSite}
   */
  @Override
  public Site resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                              NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
    HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
    if (request == null) {
      throw new RuntimeException("Could not get an HttpServletRequest object");
    }
    if (log.isDebugEnabled()) {
      log.debug(HttpDebug.dump(request));
    }
    Site site = resolveSite(request);
    if (site == null) {
      throw new UnresolvedSiteException(request);
    }
    mavContainer.addAttribute("site", site);
    return site;
  }

  /**
   * Determine which site a request is for. Applies the {@link org.ambraproject.wombat.config.site.url.SiteRequestScheme}s
   * defined in the {@link SiteSet} bean. Returns {@code null} if no site is matched.
   *
   * @param request a request from the web
   * @return the site for the request, or {@code null}
   * @throws java.lang.RuntimeException if more than one {@code SiteRequestScheme} matches the request
   */
  public Site resolveSite(HttpServletRequest request) {
    Site resolution = null;
    for (Site site : siteSet.getSites()) {
      if (site.getRequestScheme().isForSite(request)) {
        if (resolution != null) {
          String message = String.format("Multiple sites (%s, %s) matched for request: %s",
              resolution, site, HttpDebug.dump(request));
          throw new RuntimeException(message);
        }
        resolution = site;
      }
    }
    return resolution;
  }

}
