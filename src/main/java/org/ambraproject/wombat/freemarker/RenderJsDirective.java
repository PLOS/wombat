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
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import org.ambraproject.wombat.service.AssetService;

import java.io.IOException;
import java.util.Map;

/**
 * Freemarker custom directive that renders any <script> tags added via calls to
 * {@link JsDirective}.  A single instance of this directive should be added at
 * the end of the body element on the page.  It will do nothing if we are running
 * in dev assets mode (since the tags were already rendered).
 */
public class RenderJsDirective extends RenderAssetsDirective implements TemplateDirectiveModel {

  /**
   * {@inheritDoc}
   */
  @Override
  public void execute(Environment environment, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
      throws TemplateException, IOException {
    renderAssets(AssetService.AssetType.JS, JsDirective.REQUEST_VARIABLE_NAME, environment);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getHtml(String assetPath) {
    return String.format("<script src=\"%s\"></script>\n", assetPath);
  }
}
