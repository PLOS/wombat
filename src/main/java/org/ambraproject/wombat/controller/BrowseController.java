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

import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.service.remote.SoaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.Map;

/**
 * Controller for the browse page.
 */
@Controller
public class BrowseController extends WombatController {

  @Autowired
  private SoaService soaService;

  @RequestMapping(name = "browse", value = "/browse")
  public String browse(Model model, @SiteParam Site site) {
    model.addAttribute("journalKey", site.getKey());
    return site.getKey() + "/ftl/browse";
  }

  @RequestMapping(name = "browseVolumes", value = "/browse/volume")
  public String browseVolume(Model model, @SiteParam Site site) throws IOException {
    String journalMetaUrl = "journals/" + site.getJournalKey();
    Map<String, Object> journalMetadata = soaService.requestObject(journalMetaUrl, Map.class);
    model.addAttribute("journal", journalMetadata);
    return site.getKey() + "/ftl/article/browseVolumes";
  }

  @RequestMapping(name = "browseIssues", value = "/browse/issue")
  public String browseIssue(Model model, @SiteParam Site site,
                            @RequestParam("id") String issueId) throws IOException {
    //TODO: implement this method. Stubbed out here to provide a linkable endpoint for the browseVolumes template
    return site.getKey() + "/ftl/article/browseIssues";
  }
}
