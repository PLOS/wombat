package org.ambraproject.wombat.controller;

import freemarker.core.Environment;
import freemarker.ext.beans.BeanModel;
import freemarker.ext.servlet.HttpRequestHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.site.UnresolvedSiteException;
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
    Site site = resolveSite(request);
    if (site == null) {
      throw new UnresolvedSiteException(request);
    }
    mavContainer.addAttribute("site", site);
    return site;
  }

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

  private static HttpServletRequest extractRequest(Environment environment) throws TemplateModelException {
    return ((HttpRequestHashModel) environment.getDataModel().get("Request")).getRequest();
  }

  public Site getSite(Environment environment) throws TemplateModelException {
    TemplateModel site = environment.getDataModel().get("site");
    if (site instanceof BeanModel) {
      Object wrappedObject = ((BeanModel) site).getWrappedObject();
      if (wrappedObject instanceof Site) {
        return (Site) wrappedObject;
      }
    }
    return resolveSite(extractRequest(environment));
  }

  /**
   * Convenience method for {@link org.ambraproject.wombat.config.site.url.SiteRequestScheme#buildLink}.
   */
  public String buildLink(Environment environment, String path) throws TemplateModelException {
    return getSite(environment).getRequestScheme().buildLink(extractRequest(environment), path);
  }

}
