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

import org.ambraproject.wombat.service.SearchService;
import org.ambraproject.wombat.service.SoaService;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Interface that allows controller classes to add site-specific content.
 * <p/>
 * The wombat codebase is intended to be reusable by any organization that wants to
 * publish scientific articles.  To keep organization-specific formatting separate
 * from the main source distribution, we have the notion of "overlayed" themes for
 * freemarker templates.  However, often individual sites will also have to customize
 * the model objects that get passed to these templates.  This interface is intended
 * to solve this problem.
 * <p/>
 * In typical usage, a controller should get a reference to an implementation of this
 * interface from the {@link org.ambraproject.wombat.config.RuntimeConfiguration} object,
 * and then call the populateCustomModelAttributes method.
 */
public interface ControllerHook {

  /**
   * Adds any necessary data that will be passed to the view layer.
   *
   * @param request the current request
   * @param model model to be passed to the view
   */
  void populateCustomModelAttributes(HttpServletRequest request, Model model) throws IOException;

  void setSoaService(SoaService soaService);

  void setSearchService(SearchService searchService);
}
