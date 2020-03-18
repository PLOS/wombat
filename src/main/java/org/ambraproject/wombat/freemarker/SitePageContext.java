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

package org.ambraproject.wombat.freemarker;

import freemarker.core.Environment;
import freemarker.ext.beans.BeanModel;
import freemarker.ext.servlet.HttpRequestHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.ambraproject.wombat.config.site.url.Link;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteResolver;
import org.ambraproject.wombat.config.site.SiteSet;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.servlet.http.HttpServletRequest;

/**
 * Encapsulates FreeMarker environment info in order to represent site-specific information.
 */
public class SitePageContext {

  private static final Logger log = LogManager.getLogger(SitePageContext.class);

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

  public HttpServletRequest getRequest() {
    return request;
  }

  // Convenience method
  public String buildLink(String path) {
    return Link.toLocalSite(getSite()).toPath(path).get(getRequest());
  }

  // Convenience method
  public String buildLink(SiteSet siteSet, String journalKey, String path) {
    return Link.toForeignSite(getSite(), journalKey, siteSet).toPath(path).get(getRequest());
  }

}
