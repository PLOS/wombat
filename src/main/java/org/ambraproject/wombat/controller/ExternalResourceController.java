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

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.service.remote.EditorialContentApi;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Forwards requests for files to the content repository.
 */
@Controller
public class ExternalResourceController extends WombatController {

  public static final String EXTERNAL_RESOURCE_NAMESPACE = "indirect";

  private static final Logger log = LogManager.getLogger(ExternalResourceController.class);

  @Autowired
  private EditorialContentApi editorialContentApi;

  @RequestMapping(name = "repoObject", value = "/" + EXTERNAL_RESOURCE_NAMESPACE + "/{key}")
  public void serve(HttpServletResponse response,
                    HttpServletRequest request,
                    @SiteParam Site site,
                    @PathVariable("key") String key)
      throws IOException {
    serve(response, request, key);
  }

  @RequestMapping(name = "repoObjectUsingPublicUrl", value = "/s/file")
  public void serveWithPublicUrl(HttpServletResponse response,
                                 HttpServletRequest request,
                                 @SiteParam Site site,
                                 @RequestParam(value = "id", required = true) String key)
          throws IOException {
    serve(response, request, key);
  }

  private void serve(HttpServletResponse responseToClient, HttpServletRequest requestFromClient,
                     String key)
      throws IOException {
    
    responseToClient.sendRedirect(editorialContentApi.getPublicUrl(key).toString());
  }
}
