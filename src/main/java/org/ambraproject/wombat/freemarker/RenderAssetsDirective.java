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

package org.ambraproject.wombat.freemarker;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import freemarker.core.Environment;
import freemarker.ext.servlet.HttpRequestHashModel;
import freemarker.template.TemplateException;
import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.ambraproject.wombat.service.AssetService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract superclass of freemarker custom directives that render links to compiled assets.
 * See {@link AssetDirective}.
 */
public abstract class RenderAssetsDirective {

  @Autowired
  private RuntimeConfiguration runtimeConfiguration;

  @Autowired
  private AssetService assetService;

  /**
   * Renders HTML that includes a compiled asset.
   *
   * @param assetType defines the type of asset (.js or .css)
   * @param requestVariableName the name of the request variable that uncompiled assets
   *     have been stored in by calls to a subclass of {@link AssetDirective}
   * @param environment freemarker execution environment
   * @throws TemplateException
   * @throws IOException
   */
  protected void renderAssets(AssetService.AssetType assetType, String requestVariableName, Environment environment)
      throws TemplateException, IOException {
    if (!runtimeConfiguration.devModeAssets()) {
      HttpServletRequest request = ((HttpRequestHashModel) environment.getDataModel().get("Request")).getRequest();
      List<String> assetPaths = (List<String>) request.getAttribute(requestVariableName);
      if (assetPaths != null && !assetPaths.isEmpty()) {

        // Links to asset files are relative, to account for the different sites/journals.
        // However, AssetService needs more absolute paths (since it deals with the Themes
        // directly).  The following logic is needed to determine if we're in a subdirectory
        // of the site/journal dir, and the asset paths begin with "../".
        GetPathDepthsResult result = getPathDepths(assetPaths);
        assetPaths = result.depthlessPaths;
        StringBuilder compiledPath = new StringBuilder();
        compiledPath.append(getDepthPrefix(result.depth));

        // This is a bit of a hack to get relative links from asset files to work.  We replicate
        // the number of levels in the uncompiled paths.  For example, if the uncompiled link
        // points at "static/css/foo.css", the compiled one will be "static/compiled/asset_3947213.css"
        // or something.  There's corresponding code in org.ambraproject.wombat.controller.StaticFileController
        // as well.
        compiledPath.append("static/");
        compiledPath.append(assetService.getCompiledAssetLink(assetType, assetPaths, getSite(request),
            request.getServletPath()));
        environment.getOut().write(getHtml(compiledPath.toString()));
      }

    }  // else nothing to do, since in dev mode we already rendered the links.
  }

  /**
   * Returns the HTML that renders a link to an asset.  This will vary depending
   * on the subclass (and the type of the asset).
   *
   * @param assetPath path to the asset file
   * @return HTMl snippet linking to the asset file
   */
  protected abstract String getHtml(String assetPath);

  // We normally do this in Spring Controllers with @PathVariable annotations,
  // but we have to do it "by hand" since we're in a TemplateDirectiveModel.
  private String getSite(HttpServletRequest request) {
    return request.getServletPath().split("/")[1];
  }

  /**
   * Simple class that wraps the multiple values returned by getPathDepths.
   */
  @VisibleForTesting
  public static final class GetPathDepthsResult {

    /**
     * The "depth" of the asset links; that is, the number of occurrences of "../"
     * at the start of the paths.
     */
    int depth;

    /**
     * List of paths with the "up-directory" portions removed.  That is, "../../foo"
     * becomes "foo".
     */
    List<String> depthlessPaths;

    GetPathDepthsResult(int depth, List<String> depthlessPaths) {
      this.depth = depth;
      this.depthlessPaths = depthlessPaths;
    }
  }

  /**
   * Calculates the number of "up-directory" levels in the relative paths passed in,
   * and removes these up-directory markers.
   *
   * @param assetPaths relative paths to assets
   * @return see comments for {@link GetPathDepthsResult}
   */
  @VisibleForTesting
  static GetPathDepthsResult getPathDepths(List<String> assetPaths) {
    int globalDepth = -1;
    List<String> depthlessPaths = new ArrayList<>();
    for (String path : assetPaths) {
      int localDepth = 0;
      while (path.startsWith("../")) {
        path = path.substring(3);
        localDepth += 1;
      }
      if (globalDepth >= 0 && globalDepth != localDepth) {

        // TODO: will we ever need to support this?
        String message = "RenderAssetsDirective does not support asset paths with mixes of depths: ";
        message += Joiner.on(", ").join(assetPaths);
        throw new IllegalArgumentException(message);
      }
      globalDepth = localDepth;
      depthlessPaths.add(path);
    }
    return new GetPathDepthsResult(globalDepth, depthlessPaths);
  }

  /**
   * Returns a string representing a unix-like relative path going up the specified number
   * of levels.  For example, an argument of 3 will return "../../../", while 0 will
   * return the empty string.
   *
   * @param depth number of directory levels deep
   * @return string going up the specified number of directories
   */
  @VisibleForTesting
  static String getDepthPrefix(int depth) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < depth; i++) {
      result.append("../");
    }
    return result.toString();
  }
}
