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

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Map;

/**
 * Custom freemarker directive that should be used to insert a CSS link element.
 * If we are running in dev mode, this will just render the link; otherwise the
 * CSS file specified will be minified and served along with all other CSS in
 * the app.
 */
public class CssLinkDirective implements TemplateDirectiveModel {

  @Autowired
  private RuntimeConfiguration runtimeConfiguration;

  /**
   * {@inheritDoc}
   */
  @Override
  public void execute(Environment environment, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
      throws TemplateException, IOException {
    if (params.get("target") == null) {
      throw new TemplateModelException("target parameter is required");
    }
    String target = params.get("target").toString();
    target = target.trim();

    // The @pathUp macro, which we often use to generate the target, adds quotes.
    // Strip them off if present.
    if (target.charAt(0) == '"') {
      target = target.substring(1);
    }
    if (target.charAt(target.length() - 1) == '"') {
      target = target.substring(0, target.length() - 1);
    }
    if (runtimeConfiguration.devModeAssets()) {
      environment.getOut().write(String.format("<link rel=\"stylesheet\" href=\"%s\" />\n", target));
    } else {
      throw new RuntimeException("TODO: implement asset pipeline!");
    }
  }
}
