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
   * Represents the types of asset files processed by this service.
   */
  public static enum AssetType {

    CSS,
    JS;

    public String getExtension() {
      return "." + name().toLowerCase();
    }

    /**
     * Builds a cache key to store the filename of a compiled asset.  (We cache these
     * since their name includes a hash, which can be potentially expensive to compute.)
     *
     * @param cacheKey cache key for the overall request
     * @return cache key where we can store/retrieve the compiled filename
     */
    public String getFileCacheKey(String cacheKey) {
      return String.format("%sFile:%s", name().toLowerCase(), cacheKey);
    }

    /**
     * Builds a cache key to store the contents of a compiled asset.
     *
     * @param filename base filename of the compiled asset
     * @return cache key where we can store/retrieve the contents of the compiled asset
     */
    public String getContentsCacheKey(String filename) {
      return String.format("%sContents:%s", name().toLowerCase(), filename);
    }
  }

  /**
   * Concatenates a group of assets, compiles them, and returns the path where the compiled
   * file is served.  The compiled filename and the contents of the compiled file will
   * be cached.
   *
   * @param assetType specifies whether the asset is javascript or CSS
   * @param filenames list of servlet paths that correspond to asset files to compile
   * @param site specifies the journal/site
   * @param cacheKey key that will be used to cache the results
   * @return servlet path to the single, compiled asset file
   * @throws IOException
   */
  String getCompiledAssetLink(AssetType assetType, List<String> filenames, String site, String cacheKey)
      throws IOException;

  /**
   * Writes an asset that was previously compiled with a call to getCompiledCssLink to the stream.
   *
   * @param assetFilename the filename, as returned by getCompiledCssLink
   * @param outputStream output; this method will close the stream as well
   * @throws IOException
   */
  void serveCompiledAsset(String assetFilename, OutputStream outputStream) throws IOException;
}
