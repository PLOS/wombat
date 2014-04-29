/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2014 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.wombat.freemarker;

import javax.servlet.http.HttpServletRequest;

/**
 * Utility class for code shared across custom freemarker directives.
 */
public final class DirectiveUtil {

  private DirectiveUtil() {}

  // We normally do this in Spring Controllers with @PathVariable annotations,
  // but we have to do it "by hand" since we're in a TemplateDirectiveModel.

  /**
   * Returns the site value from the URL path.  We normally do this in Spring Controllers with
   * @PathVariable annotations, but we have to do it "by hand" when we're in a TemplateDirectiveModel.
   *
   * @param request HttpServletRequest
   * @return the site embedded in the URL path
   */
  public static String getSitePathParam(HttpServletRequest request) {
    return request.getServletPath().split("/")[1];
  }
}
