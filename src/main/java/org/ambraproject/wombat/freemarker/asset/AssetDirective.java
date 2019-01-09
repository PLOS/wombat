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

package org.ambraproject.wombat.freemarker.asset;

import java.io.IOException;
import java.util.Map;

import org.ambraproject.wombat.config.site.SiteResolver;
import org.ambraproject.wombat.config.site.url.Link;
import org.ambraproject.wombat.freemarker.SitePageContext;
import org.springframework.beans.factory.annotation.Autowired;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * Base class for Freemarker custom directives used to insert compiled versions of asset files (javascript and CSS).
 * <p/>
 * Allows the invoker to specify "dependencies" for an asset, which means other assets that must precede it when
 * rendered on the page.
 */
abstract class AssetDirective implements TemplateDirectiveModel {
  @Autowired
  private SiteResolver siteResolver;

  @Override
  public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
      throws TemplateException, IOException {
    Object targetObj = params.get("target");
    if (targetObj == null) {
      throw new TemplateModelException("target parameter is required");
    }
    String target = targetObj.toString();
    SitePageContext sitePageContext = new SitePageContext(siteResolver, env);
    String assetAddress = Link.toLocalSite(sitePageContext.getSite()).toPath(target).get(sitePageContext.getRequest());
    env.getOut().write(getHtml(assetAddress));
  }

  abstract String getHtml(String assetAddress);
}
