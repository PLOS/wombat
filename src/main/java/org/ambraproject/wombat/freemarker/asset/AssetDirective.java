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

import com.google.common.base.Splitter;
import freemarker.core.Environment;
import freemarker.ext.servlet.HttpRequestHashModel;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for Freemarker custom directives used to insert compiled versions of asset files (javascript and CSS).
 * <p/>
 * Allows the invoker to specify "dependencies" for an asset, which means other assets that must precede it when
 * rendered on the page.
 */
abstract class AssetDirective implements TemplateDirectiveModel {

  private static final Splitter DEPENDENCY_SPLITTER = Splitter.on(';');

  @Override
  public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
      throws TemplateException, IOException {
    Object targetObj = params.get("target");
    if (targetObj == null) {
      throw new TemplateModelException("target parameter is required");
    }
    String target = targetObj.toString();

    Object dependencyObj = params.get("dependsOn");
    List<String> dependencies = (dependencyObj == null) ? null
        : DEPENDENCY_SPLITTER.splitToList(dependencyObj.toString());

    addAsset(target, dependencies, env);
  }


  /**
   * @return the constant name of the request-scoped variable in which to store links of this asset type
   */
  protected abstract String getRequestVariableName();

  /**
   * Called when we are adding a new asset file to the page. The asset file will be queued to render by {@link
   * RenderAssetsDirective#renderAssets}.
   *
   * @param assetPath    path to the asset file being added
   * @param dependencies paths to other assets which this asset must come after (may be null for none)
   * @param environment  freemarker execution environment
   * @throws TemplateException
   * @throws IOException
   */
  protected void addAsset(String assetPath, Collection<String> dependencies, Environment environment)
      throws TemplateException, IOException {
    assetPath = assetPath.trim();
    String requestVariableName = getRequestVariableName();

    // Add the asset file to a list that's scoped to the current request. We'll render the asset link(s) later.
    // If not in dev mode, we will minify, concatenate, and render them as a single link then.
    HttpServletRequest request = ((HttpRequestHashModel) environment.getDataModel().get("Request")).getRequest();
    Map<String, AssetNode> assetNodes = (Map<String, AssetNode>) request.getAttribute(requestVariableName);
    if (assetNodes == null) {
      assetNodes = new LinkedHashMap<>(); // order is significant
    }

    AssetNode node = assetNodes.get(assetPath);
    if (node == null) {
      node = new AssetNode(assetPath, dependencies);
      assetNodes.put(assetPath, node);
    } else if (dependencies != null) {
      node.getDependencies().addAll(dependencies);
    }

    request.setAttribute(requestVariableName, assetNodes);
  }

}
