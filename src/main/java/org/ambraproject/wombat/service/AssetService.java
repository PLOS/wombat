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

package org.ambraproject.wombat.service;

import org.ambraproject.wombat.config.site.Site;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Service that deals with static assets, such as javascript and CSS.  Contains functionality for concatenating and
 * minifying (aka "compiling") these assets, as well as serving them.
 */
public interface AssetService {

  /**
   * The service's scope includes emitting URLs for the assets that it compiles. These strings define the scheme for
   * those URLs, which are used both here are in the corresponding controller.
   */
  public static interface AssetUrls {

    /**
     * The URL namespace for webpage resources.
     * <p/>
     * The name "resource" is more general than "asset", as it encompasses fonts (as well as, potentially, other things
     * such as images). This value belongs in the service layer, but {@code AssetService} logically excludes resources
     * other than assets ("assets" meaning JS and CSS in this context). May want to move this string elsewhere if there
     * is ever a ResourceService or such.
     */
    public static final String RESOURCE_NAMESPACE = "resource";
    public static final String RESOURCE_TEMPLATE = "/" + RESOURCE_NAMESPACE + "/**";

    /**
     * The logical (servlet-relative) directory in which compiled asset files are served from.
     */
    public static final String COMPILED_PATH_PREFIX = "compiled/";

    /**
     * The prefix for file names of compiled assets.
     */
    public static final String COMPILED_NAME_PREFIX = "asset_";

  }

  /**
   * Represents the types of asset files processed by this service.
   */
  public static enum AssetType {

    CSS,
    JS;

    public String getExtension() {
      return "." + name().toLowerCase();
    }
  }

  /**
   * Concatenates a group of assets, compiles them, and returns the path where the compiled file is served.  The
   * compiled filename and the contents of the compiled file will be cached.
   *
   * @param assetType specifies whether the asset is javascript or CSS
   * @param filenames list of servlet paths that correspond to asset files to compile
   * @param site      the journal/site
   * @return servlet path to the single, compiled asset file
   * @throws IOException
   */
  String getCompiledAssetLink(AssetType assetType, List<String> filenames, Site site)
      throws IOException;

  /**
   * Writes an asset that was previously compiled with a call to getCompiledCssLink to the stream.
   *
   * @param assetFilename the filename, as returned by getCompiledAssetLink
   * @param outputStream  output; this method will close the stream as well
   * @throws IOException
   */
  void serveCompiledAsset(String assetFilename, OutputStream outputStream) throws IOException;

  /**
   * Returns the mtime of the given asset file.
   *
   * @param assetFilename the filename, as returned by getCompiledAssetLink
   * @return last modified time of the given asset file
   */
  long getLastModifiedTime(String assetFilename);
}
