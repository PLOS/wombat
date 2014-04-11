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

import org.ambraproject.wombat.config.Site;
import org.ambraproject.wombat.config.SiteSet;
import org.ambraproject.wombat.service.SoaService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
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

  private static final String TAXONOMY_NAMESPACE = "/taxonomy/";
  private static final String TAXONOMY_TEMPLATE = TAXONOMY_NAMESPACE + "**";

  @Autowired
  private SoaService soaService;

  @Autowired
  private SiteSet siteSet;

  @RequestMapping("/{site}" + TAXONOMY_TEMPLATE)
  public void read(@PathVariable("site") String siteParam, HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    Map<String, Object> taxonomyBrowserConfig = siteSet.getSite(siteParam).getTheme().getConfigMap("taxonomyBrowser");
    boolean hasTaxonomyBrowser = (boolean) taxonomyBrowserConfig.get("hasTaxonomyBrowser");
    if (!hasTaxonomyBrowser) {
      throw new NotFoundException();
    }

    // All we do here is forward the request and response to and from rhino.  Another
    // approach would be to have the taxonomy browser issue JSONP requests directly
    // to rhino, but we've decided that rhino should be internal-only for now.

    // The string manipulation is a little ugly here, but the alternative would be have wombat
    // share a bunch of code with rhino in order to extract portions of the servlet path in
    // a way that plays nicely with spring
    // (specifically org.ambraproject.rhino.rest.controller.abstr.RestController).

    Site site = siteSet.getSite(siteParam);
    String uri = request.getRequestURI();
    String req = uri.substring(uri.indexOf(TAXONOMY_NAMESPACE) + 1);  // Remove first slash
    req += "?";
    String query = request.getQueryString();
    if (query != null) {
      req += query + "&";
    }
    req += "journal=" + site.getJournalKey();
    response.setContentType("application/json");
    try (OutputStream output = response.getOutputStream();
         InputStream input = soaService.requestStream(req)) {
      IOUtils.copy(input, output);
    }
  }
}
