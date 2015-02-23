package org.ambraproject.wombat.freemarker;

import freemarker.core.Environment;
import freemarker.ext.beans.BeanModel;
import freemarker.ext.servlet.HttpRequestHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.controller.SiteResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.http.HttpServletRequest;

/**
 * Encapsulates FreeMarker environment info in order to represent site-specific information.
 */
public class SitePageContext {

  private static final Logger log = LoggerFactory.getLogger(SitePageContext.class);

  private HttpServletRequest extractRequest() throws TemplateModelException {
    return ((HttpRequestHashModel) environment.getDataModel().get("Request")).getRequest();
  }

  private Site findSite(SiteResolver siteResolver)
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
  private final Environment environment;

  public SitePageContext(SiteResolver siteResolver, Environment environment) {
    try {
      this.environment = environment;
      this.request = extractRequest();
      this.site = findSite(siteResolver);
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

  public String buildLink(SiteSet siteSet, String journalKey, String path) {
    try {
      Site targetSite = site.getTheme().resolveForeignJournalKey(siteSet, journalKey);
      return targetSite.getRequestScheme().buildLink(extractRequest(), path);
    } catch (Exception e) {
      log.error("Error building link for path={}. Error detail:{}", path, e.getMessage());
      return buildLink(path);
    }
  }


}
