package org.ambraproject.wombat.controller;

import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.util.HttpDebug;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

public class SiteResolver implements HandlerMethodArgumentResolver {
  private static final Logger log = LoggerFactory.getLogger(SiteResolver.class);

  @Autowired
  private SiteSet siteSet;

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.getParameterAnnotation(SiteParam.class) != null;
  }

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

    Site resolution = null;
    for (Site site : siteSet.getSites()) {
      if (site.isFor(request)) {
        if (resolution != null) {
          String message = String.format("Multiple sites (%s, %s) matched for request: %s",
              resolution, site, HttpDebug.dump(request));
          throw new RuntimeException(message);
        }
        resolution = site;
      }
    }
    if (resolution == null) {
      throw new NotFoundException("No site matched for request");
    }
    return resolution;
  }

}
