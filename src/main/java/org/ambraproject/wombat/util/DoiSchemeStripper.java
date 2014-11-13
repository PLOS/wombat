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

package org.ambraproject.wombat.util;

import java.util.Map;

/**
 * Utility class for removing the URI scheme ("info:doi/") from DOIs.  Most of the wombat code accepts URL parameters
 * that are DOIs either with or without the scheme, but for consistency, we render URLs without the scheme.
 * <p/>
 * This code duplicates logic from the Rhino org.ambraproject.rhino.identity.ArticleIdentity, but we don't use that here
 * because doing so would introduce a dependency on ambra-models, which we don't want wombat to know about.
 */
public final class DoiSchemeStripper {

  private static final String DOI_URI_SCHEME = "info:doi/";

  private DoiSchemeStripper() {
  }

  /**
   * Removes the URI scheme, if present, from any entries in the input map that have a key of "doi".
   *
   * @param map map which will be modified
   * @return reference to map
   */
  public static Map<String, Object> strip(Map<String, Object> map) {
    return strip(map, "doi");
  }

  /**
   * Removes the URI scheme, if present, from any entries in the input map that have a given key.
   *
   * @param map          map which will be modified
   * @param doiFieldName key whose value may be modified in the map
   * @return reference to map
   */
  public static Map<String, Object> strip(Map<String, Object> map, String doiFieldName) {
    Object doiObj = map.get(doiFieldName);
    if (doiObj != null) {
      String doi = (String) doiObj;
      String stripped = strip(doi);
      if (!stripped.equals(doi)) {
        map.put(doiFieldName, stripped);
      }
    }
    return map;
  }

  /**
   * Removes the URI scheme, if present, from a DOI.
   *
   * @param doi a DOI in URI format or a DOI name
   * @return a DOI name with no URI scheme
   */
  public static String strip(String doi) {
    return doi.startsWith(DOI_URI_SCHEME) ? doi.substring(DOI_URI_SCHEME.length()) : doi;
  }

}
