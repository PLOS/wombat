/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2014 by Public Library of Science http://plos.org http://ambraproject.org
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
import org.ambraproject.wombat.config.Site;
import org.ambraproject.wombat.config.SiteSet;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

/**
 * Freemarker custom directive that writes out the Google Analytics code for the given site.
 */
// TODO: consider generalizing this if we find ourselves needing more journal-specific values directly in
// templates.
public class SiteGoogleAnalyticsDirective implements TemplateDirectiveModel {

  @Autowired
  protected SiteSet siteSet;

  @Override
  public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
      throws TemplateException, IOException {
    HttpServletRequest request = ((HttpRequestHashModel) env.getDataModel().get("Request")).getRequest();
    Site site = siteSet.getSite(DirectiveUtil.getSitePathParam(request));
    env.getOut().write(site.getGoogleAnalyticsCode());
  }
}
