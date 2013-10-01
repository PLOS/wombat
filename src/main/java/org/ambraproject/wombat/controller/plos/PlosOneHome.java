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

package org.ambraproject.wombat.controller.plos;

import org.ambraproject.wombat.controller.ControllerHook;
import org.ambraproject.wombat.service.SoaService;
import org.springframework.ui.Model;

import java.io.IOException;
import java.util.List;

/**
 * ControllerHook that adds additional model data needed to render the PLOS ONE homepage.
 */
public class PlosOneHome implements ControllerHook {

  private SoaService soaService;

  /**
   * {@inheritDoc}
   */
  @Override
  public void populateCustomModelAttributes(Model model) throws IOException {
    model.addAttribute("inTheNewsArticles", soaService.requestObject("journals/PLoSONE?inTheNewsArticles", List.class));
    
  }

  public void setSoaService(SoaService soaService) {
    this.soaService = soaService;
  }
}
