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

package org.ambraproject.wombat.controller;

import org.ambraproject.wombat.config.RuntimeConfigurationException;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.config.theme.Theme;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.service.remote.ContentKey;
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
    String repoKey = repoKeyPrefix + "." + pageName;

    ContentKey version = ContentKey.createForLatestVersion(repoKey); // versioning is not supported for site content
    try {
      // Check for validity of the content repo key prior to rendering page. Return a 404 if no object found.
      editorialContentApi.requestMetadata(version);
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
