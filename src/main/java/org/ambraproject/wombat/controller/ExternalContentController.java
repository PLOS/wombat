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

package org.ambraproject.wombat.controller;

import org.ambraproject.wombat.config.site.MappingSiteScope;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteScope;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.service.remote.EditorialContentApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

/**
 * Controller serves a container page for externally generated content
 */
@Controller
public class ExternalContentController extends WombatController {

  @Autowired
  private EditorialContentApi editorialContentApi;

  private final String REPO_KEY_PREFIX = "c";

  @MappingSiteScope(SiteScope.JOURNAL_SPECIFIC)
  @RequestMapping(name = "externalContent", value = "/external/{pageName}")
  public String renderExternalContent(Model model, Site site, @PathVariable String pageName)
      throws IOException {

    String repoKey = REPO_KEY_PREFIX.concat(".").concat(pageName);
    model.addAttribute("externalServiceName", "ember"); // may not need, but kept for prototype

    try {
      model.addAttribute("externalData", editorialContentApi.getJson("externalContent", repoKey));
    } catch (EntityNotFoundException e) {
      // Return a 404 if no object found.
      throw new NotFoundException(e);
    }

    return site + "/ftl/externalContent/container";
  }

}
