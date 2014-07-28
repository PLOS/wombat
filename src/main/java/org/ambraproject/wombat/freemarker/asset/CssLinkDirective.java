/*
 * Copyright (c) 2006-2013 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.wombat.freemarker.asset;

/**
 * Custom freemarker directive that should be used to insert a CSS link element. If we are running in dev mode, this
 * will just render the link; otherwise the CSS file specified will be minified and served along with all other CSS in
 * the app.
 */
public class CssLinkDirective extends AssetDirective {

  static final String REQUEST_VARIABLE_NAME = "cssFiles";

  @Override
  protected String getParameterName() {
    return "target";
  }

  @Override
  protected String getRequestVariableName() {
    return REQUEST_VARIABLE_NAME;
  }

}
