package org.ambraproject.wombat.controller;

import com.google.common.base.Splitter;
import org.ambraproject.wombat.config.Site;
import org.ambraproject.wombat.config.SiteSet;
import org.ambraproject.wombat.service.UnmatchedSiteException;
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
import java.util.Iterator;

public class SiteResolver implements HandlerMethodArgumentResolver {
  private static final Logger log = LoggerFactory.getLogger(SiteResolver.class);

  @Autowired
  private SiteSet siteSet;

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.getParameterAnnotation(SiteParam.class) != null;
  }

  private static final Splitter PATH_SPLITTER = Splitter.on('/').omitEmptyStrings();

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

    Iterator<String> path = PATH_SPLITTER.split(request.getServletPath()).iterator();
    if (path.hasNext()) {
      try {
        return siteSet.getSite(path.next());
      } catch (UnmatchedSiteException e) {
        // Fall through and keep trying
      }
    }

    throw new UnmatchedSiteException(null); // Not yet implemented
  }

}
