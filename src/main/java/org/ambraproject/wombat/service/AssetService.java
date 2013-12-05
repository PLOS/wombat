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

package org.ambraproject.wombat.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Service that deals with static assets, such as javascript and CSS.  Contains
 * functionality for concatenating and minifying (aka "compiling") these assets,
 * as well as serving them.
 */
public interface AssetService {

  /**
   * The logical (servlet-relative) directory in which compiled asset files are served from.
   */
  static final String COMPILED_PATH_PREFIX = "compiled/";

  /**
   * Concatenates a group of css files into a single file, minifies it, and
   * returns the path where the compiled file is served.  Implementations may
   * choose to cache the results, in which case cacheKey is used as the cache
   * key.
   *
   * @param cssFilenames list of servlet paths that correspond to CSS files to compile
   * @param site specifies the journal/site
   * @param cacheKey key that will be used to cache the results
   * @return servlet path to the single, compiled CSS file
   * @throws IOException
   */
  String getCompiledCssLink(List<String> cssFilenames, String site, String cacheKey) throws IOException;

  /**
   * Concatenates a group of javascript files into a single file, minifies it, and
   * returns the path where the compiled file is served.  Implementations may
   * choose to cache the results, in which case cacheKey is used as the cache
   * key.
   *
   * @param jsFilenames list of servlet paths that correspond to javascript files to compile
   * @param site specifies the journal/site
   * @param cacheKey key that will be used to cache the results
   * @return servlet path to the single, compiled CSS file
   * @throws IOException
   */
  String getCompiledJavascriptLink(List<String> jsFilenames, String site, String cacheKey) throws IOException;

  /**
   * Writes an asset that was previously compiled with a call to getCompiledCssLink to the stream.
   *
   * @param assetFilename the filename, as returned by getCompiledCssLink
   * @param outputStream output; this method will close the stream as well
   * @throws IOException
   */
  void serveCompiledAsset(String assetFilename, OutputStream outputStream) throws IOException;
}
