/*
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
import org.ambraproject.wombat.controller.SiteResolver;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for Freemarker custom directives used to insert compiled versions of asset files (javascript and CSS).
 */
public abstract class AssetDirective implements TemplateDirectiveModel {

  @Autowired
  private RuntimeConfiguration runtimeConfiguration;
  @Autowired
  private SiteResolver siteResolver;

  /**
   * Called when we are adding a new asset file to the page.  If we're in dev mode, this will just render HTML that
   * links to the asset; if not, no HTML will be rendered, and instead the asset file will be queued for compilation.
   *
   * @param assetPath           path to the asset file being added
   * @param requestVariableName the name of the request-scoped variable that stores assets awaiting compilation.
   *                            Typically, there will be one of these per asset type (javascript or CSS).
   * @param environment         freemarker execution environment
   * @throws TemplateException
   * @throws IOException
   */
  protected void addAsset(String assetPath, String requestVariableName, Environment environment)
      throws TemplateException, IOException {
    assetPath = assetPath.trim();

    // Add the asset file to a list that's scoped to the current request. We'll render the asset link(s) later.
    // If not in dev mode, we will minify, concatenate, and render them as a single link then.
    HttpServletRequest request = ((HttpRequestHashModel) environment.getDataModel().get("Request")).getRequest();
    List<String> assetFiles = (List<String>) request.getAttribute(requestVariableName);
    if (assetFiles == null) {
      assetFiles = new ArrayList<>();
    }
    assetFiles.add(assetPath);
    request.setAttribute(requestVariableName, assetFiles);
  }

}
