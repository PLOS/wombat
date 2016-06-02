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

import com.google.common.base.Optional;
import org.ambraproject.wombat.config.RuntimeConfigurationException;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.config.theme.Theme;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.service.remote.EditorialContentApi;
import org.ambraproject.wombat.util.CacheKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.Map;

/**
 * Controller intended to serve static pages
 */
@Controller
public class SiteContentController extends WombatController {

  @Autowired
  private EditorialContentApi editorialContentApi;

  @RequestMapping(name="siteContent", value="/s/{pageName}")
  public String renderSiteContent(Model model, @SiteParam Site site, @PathVariable String pageName)
          throws IOException {

    Theme theme = site.getTheme();
    Map<String, Object> pageConfig = theme.getConfigMap("siteContent");

    String repoKeyPrefix = (String) pageConfig.get("contentRepoKeyPrefix");
    if (repoKeyPrefix == null) {
      throw new RuntimeConfigurationException("Content repo prefix not configured for theme " + theme.toString());
    }
    String repoKey = repoKeyPrefix.concat(".").concat(pageName);

    CacheKey cacheKey = CacheKey.create("siteContent_meta", repoKey);

    Optional<Integer> version = Optional.absent(); // versioning is not supported for site content
    try {
      // Check for validity of the content repo key prior to rendering page. Return a 404 if no object found.
      editorialContentApi.requestMetadata(cacheKey, repoKey, version);
    } catch (EntityNotFoundException e) {
      throw new NotFoundException(e);
    }
    model.addAttribute("siteContentRepoKey", repoKey);
    return site + "/ftl/siteContent/container";
  }

  /**
   * controller for site content home pages
   */
  @RequestMapping(name = "siteContentHome", value = "/s", method = RequestMethod.GET)
  public String siteContentHomePage(Model model, @SiteParam Site site) throws IOException {

    Theme theme = site.getTheme();
    Map<String, Object> pageConfig = theme.getConfigMap("siteContent");

    String homeRepoKey = (String) pageConfig.get("homeRepoKey");
    if (homeRepoKey == null) {
      throw new RuntimeConfigurationException("Content repo key for site content home page not configured for theme " +
              theme.toString());
    }
    return renderSiteContent(model, site, homeRepoKey);
  }

}
