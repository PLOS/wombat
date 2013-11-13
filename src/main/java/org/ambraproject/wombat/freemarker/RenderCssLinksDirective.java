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
import freemarker.ext.servlet.HttpRequestHashModel;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Freemarker custom directive that renders any CSS links added via calls to {@link CssLinkDirective}.
 * A single instance of this directive should be added at the end of the head element on the page.
 * It will do nothing if we are running in dev assets mode (since the links were already rendered).
 */
public class RenderCssLinksDirective implements TemplateDirectiveModel {

  @Autowired
  private RuntimeConfiguration runtimeConfiguration;

  /**
   * {@inheritDoc}
   */
  @Override
  public void execute(Environment environment, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
      throws TemplateException, IOException {
    if (!runtimeConfiguration.devModeAssets()) {
      HttpServletRequest request = ((HttpRequestHashModel) environment.getDataModel().get("Request")).getRequest();
      List<String> cssFiles = (List<String>) request.getAttribute("cssFiles");
      if (cssFiles != null) {

        // TODO: concatenate and minify here, instead of just writing out all the links.
        for (String css : cssFiles) {
          environment.getOut().write(String.format("<link rel=\"stylesheet\" href=\"%s\" />\n", css));
        }
      }

    }  // else nothing to do, since in dev mode we already rendered the links.
  }
}
