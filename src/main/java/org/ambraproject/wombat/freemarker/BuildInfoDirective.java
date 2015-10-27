/*
 * Copyright (c) 2006-2014 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
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
