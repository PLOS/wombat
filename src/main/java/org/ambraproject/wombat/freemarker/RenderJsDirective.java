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

package org.ambraproject.wombat.freemarker;

import freemarker.core.Environment;
import freemarker.ext.servlet.HttpRequestHashModel;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.ambraproject.wombat.service.AssetService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Freemarker custom directive that renders any <script> tags added via calls to
 * {@link JsDirective}.  A single instance of this directive should be added at
 * the end of the body element on the page.  It will do nothing if we are running
 * in dev assets mode (since the tags were already rendered).
 */
public class RenderJsDirective implements TemplateDirectiveModel {

  @Autowired
  private RuntimeConfiguration runtimeConfiguration;

  @Autowired
  private AssetService assetService;

  /**
   * {@inheritDoc}
   */
  @Override
  public void execute(Environment environment, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
      throws TemplateException, IOException {
    if (!runtimeConfiguration.devModeAssets()) {
      HttpServletRequest request = ((HttpRequestHashModel) environment.getDataModel().get("Request")).getRequest();
      List<String> jsFiles = (List<String>) request.getAttribute(JsDirective.REQUEST_VARIABLE_NAME);
      if (jsFiles != null && !jsFiles.isEmpty()) {

        // This is a bit of a hack to get relative links from asset files to work.  We replicate
        // the number of levels in the uncompiled paths.  For example, if the uncompiled link
        // points at "static/js/foo.js", the compiled one will be "static/compiled/asset_3947213.js"
        // or something.  There's corresponding code in org.ambraproject.wombat.controller.StaticFileController
        // as well.
        String assetPath = "static/" + assetService.getCompiledJavascriptLink(jsFiles, getSite(request),
            request.getServletPath());
        environment.getOut().write(String.format("<script src=\"%s\"></script>\n", assetPath));
      }

    }  // else nothing to do, since in dev mode we already rendered the links.
  }

  // We normally do this in Spring Controllers with @PathVariable annotations,
  // but we have to do it "by hand" since we're in a TemplateDirectiveModel.
  private String getSite(HttpServletRequest request) {
    return request.getServletPath().split("/")[1];
  }
}
