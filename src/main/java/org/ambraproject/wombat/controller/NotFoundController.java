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

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller intended to serve "nice" 404 pages, using the styling of the site, if possible.
 * <p/>
 * An instance of this controller is set as the defaultHandler property in the Spring config, in order to pick up any
 * pages that don't match the RequestMapping of any controller. Compare {@link WombatController#handleNotFound}, which
 * is the handler for {@link NotFoundException}s thrown by controllers.
 */
@Controller
public class NotFoundController extends WombatController {

  @RequestMapping
  public String handle404(HttpServletRequest request, HttpServletResponse response) {
    return handleNotFound(request, response);
  }

}
