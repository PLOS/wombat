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

package org.ambraproject.wombat.freemarker;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.ambraproject.wombat.service.BuildInfoService;
import org.ambraproject.wombat.util.BuildInfo;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Map;

public class BuildInfoDirective extends VariableLookupDirective<Object> {

  @Autowired
  private BuildInfoService buildInfoService;

  @Override
  protected Object getValue(Environment env, Map params) throws TemplateException, IOException {
    String component = params.get("component").toString();
    BuildInfo info;
    switch (component) {
      case "webapp":
        info = buildInfoService.getWebappBuildInfo();
        break;
      case "service":
        info = buildInfoService.getServiceBuildInfo();
        break;
      default:
        throw new TemplateModelException("component required");
    }

    final Object value;
    if (info == null) {
      value = null;
    } else {
      String field = params.get("field").toString();
      switch (field) {
        case "version":
          value = info.getVersion();
          break;
        case "date":
          value = info.getDate();
          break;
        case "user":
          value = info.getUser();
          break;
        case "commitIdAbbrev":
          value = info.getGitCommitIdAbbrev();
          break;
        case "enabledDevFeatures":
          value = info.getEnabledDevFeatures();
          break;
        default:
          throw new TemplateModelException("field required");
      }
    }

    return (value != null) ? value : "?";
  }

}
