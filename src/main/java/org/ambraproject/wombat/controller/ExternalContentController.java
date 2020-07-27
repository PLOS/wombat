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

import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
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

  @RequestMapping(name = "externalContent", value = "/external/{pageName}")
  public String renderExternalContent(Model model, @SiteParam Site site, @PathVariable String pageName)
          throws IOException {

    String repoKey = REPO_KEY_PREFIX.concat(".").concat(pageName);
    model.addAttribute("externalServiceName", "ember"); // may not need, but kept for prototype

    try {
      model.addAttribute("externalData", editorialContentApi.getJson(repoKey));
    } catch (EntityNotFoundException e) {
      // Return a 404 if no object found.
      throw new NotFoundException(e);
    }

    return site + "/ftl/externalContent/container";
  }

}
