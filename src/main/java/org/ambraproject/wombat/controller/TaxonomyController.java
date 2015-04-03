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
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.service.remote.SoaRequest;
import org.ambraproject.wombat.service.remote.SoaService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Controller that handles JSON requests from the taxonomy browser.
 */
@Controller
public class TaxonomyController {

  private static final String TAXONOMY_ROOT = "/taxonomy";
  private static final String TAXONOMY_NAMESPACE = TAXONOMY_ROOT + "/";

  @Autowired
  private SoaService soaService;

  @Autowired
  private SiteSet siteSet;

  @RequestMapping(value = {TAXONOMY_ROOT, "/{site}" + TAXONOMY_ROOT}, method = RequestMethod.GET)
  public void read(@SiteParam Site site,
                   HttpServletResponse response,
                   @RequestParam("parent") String parent)
      throws IOException {
    Map<String, Object> taxonomyBrowserConfig = site.getTheme().getConfigMap("taxonomyBrowser");
    boolean hasTaxonomyBrowser = (boolean) taxonomyBrowserConfig.get("hasTaxonomyBrowser");
    if (!hasTaxonomyBrowser) {
      throw new NotFoundException();
    }

    // All we do here is forward the request and response to and from rhino.  Another
    // approach would be to have the taxonomy browser issue JSONP requests directly
    // to rhino, but we've decided that rhino should be internal-only for now.

    SoaRequest req = SoaRequest.request("taxonomy")
        .addParameter("parent", parent)
        .addParameter("journal", site.getJournalKey())
        .build();
    response.setContentType("application/json");
    try (OutputStream output = response.getOutputStream();
         InputStream input = soaService.requestStream(req)) {
      IOUtils.copy(input, output);
    }
  }

  @RequestMapping(value = {TAXONOMY_NAMESPACE + "flag/{action:add|remove}",
      "/{site}" + TAXONOMY_NAMESPACE + "flag/{action:add|remove}"}, method = {RequestMethod.POST})
  @ResponseBody
  public void setFlag(HttpServletResponse responseToClient,
                      @RequestParam(value = "categoryTerm", required = true) String categoryTerm,
                      @RequestParam(value = "articleDoi", required = true) String articleDoi,
                      @RequestParam(value = "authId", required = true) String authId,
                      @PathVariable("action") String action)
      throws IOException {
    // pass through any article category flagging ajax traffic to/from rhino
    SoaRequest req = SoaRequest.request("taxonomy/flag")
        .addPathToken(action)
        .addParameter("categoryTerm", categoryTerm)
        .addParameter("articleDoi", articleDoi)
        .addParameter("authId", authId)
        .build();
    soaService.forwardResponse(req, HttpMethod.POST, responseToClient);
  }

}
