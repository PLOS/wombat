package org.ambraproject.wombat.config.site;

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

/**
 * Bean that resolves requests to the sites that the application hosts.
 * <p/>
 * The sites themselves, and the definitions of how to resolve requests to individual sites, are encapsulated in the
 * {@link SiteSet} bean. This object exposes them to the view layer.
 */
public class SiteResolver implements HandlerMethodArgumentResolver {
  private static final Logger log = LoggerFactory.getLogger(SiteResolver.class);

  @Autowired
  private SiteSet siteSet;

  /**
   * Apply to every controller parameter of type {@link Site}.
   */
  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return Site.class.isAssignableFrom(parameter.getParameterType());
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
