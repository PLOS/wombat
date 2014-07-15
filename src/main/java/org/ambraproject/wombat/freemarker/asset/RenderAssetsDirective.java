/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2013 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.wombat.freemarker.asset;

import freemarker.core.Environment;
import freemarker.ext.servlet.HttpRequestHashModel;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.controller.SiteResolver;
import org.ambraproject.wombat.controller.StaticResourceController;
import org.ambraproject.wombat.freemarker.SitePageContext;
import org.ambraproject.wombat.service.AssetService;
import org.ambraproject.wombat.util.PathUtil;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

/**
 * Abstract superclass of freemarker custom directives that render links to compiled assets. See {@link
 * AssetDirective}.
 */
public abstract class RenderAssetsDirective implements TemplateDirectiveModel {

  @Autowired
  private RuntimeConfiguration runtimeConfiguration;
  @Autowired
  private AssetService assetService;
  @Autowired
  private SiteResolver siteResolver;

  /**
   * Renders HTML that includes a compiled asset.
   *
   * @param assetType           defines the type of asset (.js or .css)
   * @param requestVariableName the name of the request variable that uncompiled assets have been stored in by calls to
   *                            a subclass of {@link AssetDirective}
   * @param environment         freemarker execution environment
   * @throws TemplateException
   * @throws IOException
   */
  protected void renderAssets(AssetService.AssetType assetType, String requestVariableName, Environment environment)
      throws TemplateException, IOException {
    if (!runtimeConfiguration.devModeAssets()) {
      HttpServletRequest request = ((HttpRequestHashModel) environment.getDataModel().get("Request")).getRequest();
      List<String> assetPaths = (List<String>) request.getAttribute(requestVariableName);
      if (assetPaths != null && !assetPaths.isEmpty()) {
        Site site = new SitePageContext(siteResolver, environment).getSite();
        String assetLink = assetService.getCompiledAssetLink(assetType, assetPaths, site, request.getServletPath());
        String assetAddress = site.getRequestScheme().buildLink(request,
            PathUtil.JOINER.join(StaticResourceController.RESOURCE_NAMESPACE, assetLink));
        environment.getOut().write(getHtml(assetAddress));
      }

    }  // else nothing to do, since in dev mode we already rendered the links.
  }

  /**
   * Returns the HTML that renders a link to an asset.  This will vary depending on the subclass (and the type of the
   * asset).
   *
   * @param assetPath path to the asset file
   * @return HTMl snippet linking to the asset file
   */
  protected abstract String getHtml(String assetPath);

}
