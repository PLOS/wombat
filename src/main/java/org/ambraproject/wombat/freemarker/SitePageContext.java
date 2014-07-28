package org.ambraproject.wombat.freemarker;

import freemarker.core.Environment;
import freemarker.ext.beans.BeanModel;
import freemarker.ext.servlet.HttpRequestHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.controller.SiteResolver;

import javax.servlet.http.HttpServletRequest;

/**
 * Encapsulates FreeMarker environment info in order to represent site-specific information.
 */
public class SitePageContext {

  private static HttpServletRequest extractRequest(Environment environment) throws TemplateModelException {
    return ((HttpRequestHashModel) environment.getDataModel().get("Request")).getRequest();
  }

  private static Site findSite(SiteResolver siteResolver, Environment environment, HttpServletRequest request)
      throws TemplateModelException {
    // Recover it from the model if possible
    TemplateModel site = environment.getDataModel().get("site");
    if (site instanceof BeanModel) {
      Object wrappedObject = ((BeanModel) site).getWrappedObject();
      if (wrappedObject instanceof Site) {
        return (Site) wrappedObject;
      }
    }

    // Else, delegate to the resolver
    return siteResolver.resolveSite(request);
  }

  private final Site site;
  private final HttpServletRequest request;

  public SitePageContext(SiteResolver siteResolver, Environment environment) {
    try {
      this.request = extractRequest(environment);
      this.site = findSite(siteResolver, environment, request);
    } catch (TemplateModelException e) {
      throw new RuntimeException(e);
    }
  }

  public Site getSite() {
    return site;
  }

  public String buildLink(String path) {
    return site.getRequestScheme().buildLink(request, path);
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SitePageContext that = (SitePageContext) o;

    if (!request.equals(that.request)) return false;
    if (!site.equals(that.site)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = site.hashCode();
    result = 31 * result + request.hashCode();
    return result;
  }

}
