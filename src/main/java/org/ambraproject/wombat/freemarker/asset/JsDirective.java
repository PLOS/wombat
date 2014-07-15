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
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import java.io.IOException;
import java.util.Map;

/**
 * Custom freemarker directive that should be used to insert a <script> element. If we are running in dev mode, this
 * will just render the link; otherwise the javascript file specified will be minified and served along with all other
 * .js in the app.
 */
public class JsDirective extends AssetDirective {

  static final String REQUEST_VARIABLE_NAME = "jsFiles";

  /**
   * {@inheritDoc}
   */
  @Override
  public void execute(Environment environment, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
      throws TemplateException, IOException {
    if (params.get("src") == null) {
      throw new TemplateModelException("src parameter is required");
    }
    String target = params.get("src").toString();
    addAsset(target, REQUEST_VARIABLE_NAME, environment);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getHtml(String assetPath) {
    return String.format("<script src=\"%s\"></script>\n", assetPath);
  }
}
