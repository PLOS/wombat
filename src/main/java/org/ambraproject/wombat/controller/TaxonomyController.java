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
import org.ambraproject.wombat.util.HttpMessageUtil;
import org.ambraproject.wombat.util.UriUtil;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
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

  @RequestMapping(name = "taxonomy", value = "" + TAXONOMY_TEMPLATE, method = RequestMethod.GET)
  public void read(@SiteParam Site site, HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    Map<String, Object> taxonomyBrowserConfig = site.getTheme().getConfigMap("taxonomyBrowser");
    boolean hasTaxonomyBrowser = (boolean) taxonomyBrowserConfig.get("hasTaxonomyBrowser");
    if (!hasTaxonomyBrowser) {
      throw new NotFoundException();
    }

    // All we do here is forward the request and response to and from rhino.  Another
    // approach would be to have the taxonomy browser issue JSONP requests directly
    // to rhino, but we've decided that rhino should be internal-only for now.

    String req = UriUtil.stripUrlPrefix(request.getRequestURI(), TAXONOMY_NAMESPACE);
    req += "?";
    String query = request.getQueryString();
    if (query != null) {
      req += query + "&";
    }
    req += "journal=" + site.getJournalKey();
    response.setContentType("application/json");
    try (InputStream input = soaService.requestStream(req);
         OutputStream output = response.getOutputStream()) {
      IOUtils.copy(input, output);
    }
  }

  @RequestMapping(name = "taxonomyCategoryFlag", value = "" + TAXONOMY_NAMESPACE + "flag/{action:add|remove}", method = RequestMethod.POST)
  @ResponseBody
  public void setFlag(HttpServletRequest request, HttpServletResponse responseToClient,
                      @RequestParam(value = "categoryTerm", required = true) String categoryTerm,
                      @RequestParam(value = "articleDoi", required = true) String articleDoi)
      throws IOException {
    // pass through any article category flagging ajax traffic to/from rhino
    URI forwardedUrl = UriUtil.concatenate(soaService.getServerUrl(), UriUtil.stripUrlPrefix(request.getRequestURI(), TAXONOMY_NAMESPACE));
    HttpUriRequest req = HttpMessageUtil.buildRequest(forwardedUrl, "POST",
            HttpMessageUtil.getRequestParameters(request), new BasicNameValuePair("authId", request.getRemoteUser()));
    soaService.forwardResponse(req, responseToClient);
  }

}
