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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

/**
 * Controller for client-side redirect page
 */
@Controller
public class RedirectController extends WombatController {

  @MappingSiteScope(SiteScope.JOURNAL_SPECIFIC)
  @RequestMapping(name = "redirect", value = "/redirect/{sourcePage}")
  public String render(Model model, Site site,
                       @PathVariable String sourcePage,
                       @RequestParam(value="defaultTarget", required = false) String defaultTarget)
          throws IOException {

    model.addAttribute("sourcePage", sourcePage);
    model.addAttribute("defaultTarget", defaultTarget != null ? defaultTarget : sourcePage);
    return site + "/ftl/redirect";
  }

}
