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
import org.ambraproject.wombat.config.theme.Theme;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.service.remote.EditorialContentService;
import org.ambraproject.wombat.util.CacheParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.Map;

/**
 * Controller intended to serve static pages
 */
@Controller
public class StaticContentController extends WombatController {

  @Autowired
  private EditorialContentService editorialContentService;

  @RequestMapping(value = {"/s/{staticPage}", "/{site}/s/{staticPage}"})
  public String renderStaticContent(Model model, @SiteParam Site site, @PathVariable String staticPage)
          throws IOException {

    Theme theme = site.getTheme();
    Map<String, Object> pageConfig = theme.getConfigMap("staticContent");

    String repoKeyPrefix = (String) pageConfig.get("contentRepoKeyPrefix");
    if (repoKeyPrefix == null) {
      throw new RuntimeConfigurationException("Content repo prefix not configured for theme " + theme.toString());
    }
    String repoKey = repoKeyPrefix.concat(".").concat(staticPage);

    String cacheKeyPrefix = (String) pageConfig.get("cacheKeyPrefix");
    if (cacheKeyPrefix == null) {
      throw new RuntimeConfigurationException("No cache key prefix configured for page type: staticContent");
    }
    String cacheKey = cacheKeyPrefix.concat("_meta:").concat(repoKey);

    Optional<Integer> version = Optional.absent();
    try {
      // Check for validity of the content repo key prior to rendering page. Return a 404 if no object found.
      Map<String, Object> pageMetadata = editorialContentService.requestMetadata(CacheParams.create(cacheKey),
              repoKey, version);
    } catch (EntityNotFoundException e) {
      throw new NotFoundException();
    }
    model.addAttribute("staticContentRepoKey", repoKey);
    return site + "/ftl/static/container";
  }

}
