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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.Closer;
import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;
import org.ambraproject.rhombat.cache.Cache;
import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.ambraproject.wombat.config.SiteSet;
import org.ambraproject.wombat.config.Theme;
import org.apache.commons.io.IOUtils;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import sun.misc.BASE64Encoder;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 *
 */
public class AssetServiceImpl implements AssetService {

  private static final Logger logger = LoggerFactory.getLogger(AssetServiceImpl.class);

  private static final int BUFFER_SIZE = 8192;

  /**
   * We only store the contents of compiled assets in memcache if they are smaller than this
   * value.  Otherwise, the controller that serves them will need to read them from disk.
   */
  private static final int MAX_ASSET_SIZE_TO_CACHE = 1024 * 1024;

  /**
   * We use a shorter cache TTL than the global default (1 hour), because it's theoretically
   * possible that the uncompiled asset files might change in the themes directory.  And since
   * the cache key can only be calculated by loading and hashing all the corresponding files
   * (an expensive operation), we have to accept that we'll serve stale assets for this period.
   */
  private static final int CACHE_TTL = 15 * 60;

  @Autowired
  private SiteSet siteSet;

  @Autowired
  private RuntimeConfiguration runtimeConfiguration;

  @Autowired
  private Cache cache;

  /**
   * Represents the types of asset files processed by this service.
   */
  private static enum AssetType {

    CSS,
    JS;

    String getExtension() {
      return "." + name().toLowerCase();
    }

    /**
     * Builds a cache key to store the filename of a compiled asset.  (We cache these
     * since their name includes a hash, which can be potentially expensive to compute.)
     *
     * @param cacheKey cache key for the overall request
     * @return cache key where we can store/retrieve the compiled filename
     */
    String getFileCacheKey(String cacheKey) {
      return String.format("%sFile:%s", name().toLowerCase(), cacheKey);
    }

    /**
     * Builds a cache key to store the contents of a compiled asset.
     *
     * @param filename base filename of the compiled asset
     * @return cache key where we can store/retrieve the contents of the compiled asset
     */
    String getContentsCacheKey(String filename) {
      return String.format("%sContents:%s", name().toLowerCase(), filename);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getCompiledCssLink(List<String> cssFilenames, String site, String cacheKey) throws IOException {
    return getCompiledAssetLink(AssetType.CSS, cssFilenames, site, cacheKey);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getCompiledJavascriptLink(List<String> cssFilenames, String site, String cacheKey) throws IOException {
    return getCompiledAssetLink(AssetType.JS, cssFilenames, site, cacheKey);
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
  private String getCompiledAssetLink(AssetType assetType, List<String> filenames, String site, String cacheKey)
      throws IOException {
    String fileCacheKey = assetType.getFileCacheKey(cacheKey);
    String filename = cache.get(fileCacheKey);
    if (filename == null) {
      File concatenated = concatenateFiles(filenames, site, assetType.getExtension());
      FileAndContents compiled = compileAsset(assetType, concatenated);
      concatenated.delete();
      filename = compiled.file.getName();

      // We cache both the file name and the file contents (if it is small enough) in memcache.
      // In an ideal world, we would use the filename (including the hash of its contents) as
      // the only cache key.  However, it's potentially expensive to calculate that key since
      // you need the contents of the compiled file, which is why we do it this way.
      cache.put(fileCacheKey, filename, CACHE_TTL);
      if (compiled.contents.length < MAX_ASSET_SIZE_TO_CACHE) {
        cache.put(assetType.getContentsCacheKey(filename), compiled.contents, CACHE_TTL);
      }
    }
    return COMPILED_PATH_PREFIX + filename;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void serveCompiledAsset(String assetFilename, OutputStream outputStream) throws IOException {

    // Determine AssetType based on file extension.
    String[] fields = assetFilename.split("/");
    String basename = fields[fields.length - 1];
    AssetType assetType = AssetType.valueOf(AssetType.class,
        basename.substring(basename.lastIndexOf('.') + 1).toUpperCase());
    byte[] cached = cache.get(assetType.getContentsCacheKey(basename));
    InputStream is;
    if (cached == null) {
      is = new FileInputStream(new File(getCompiledFilePath(assetFilename)));
    } else {
      is = new ByteArrayInputStream(cached);
    }
    Closer closer = Closer.create();
    try {
      closer.register(is);
      closer.register(outputStream);
      IOUtils.copy(is, outputStream);
    } catch (Throwable t) {
      throw closer.rethrow(t);
    } finally {
      closer.close();
    }
  }

  /**
   * Retrieves the given files and concatenates them into a single file.
   *
   * @param filenames list of servlet paths corresponding to the files to concatenate
   * @param site specifies the journal/site
   * @param extension specifies the type of file created (e.g. ".css", ".js")
   * @return File with all filenames concatenated.  It is the responsibility of the caller
   *     to delete this file (since it will end up being minified eventually).
   * @throws IOException
   */
  private File concatenateFiles(List<String> filenames, String site, String extension) throws IOException {
    File result = File.createTempFile("concatenated_", extension);
    Theme theme = siteSet.getSite(site).getTheme();
    Closer closer = Closer.create();
    try {
      BufferedOutputStream bos = closer.register(new BufferedOutputStream(new FileOutputStream(result)));
      for (String filename : filenames) {
        InputStream is = theme.getStaticResource(filename);
        closer.register(is);
        IOUtils.copy(is, bos);
        bos.write('\n');  // just in case
      }
    } catch (Throwable t) {
      throw closer.rethrow(t);
    } finally {
      closer.close();
    }
    return result;
  }

  /**
   * Simple class representing a File and its contents.  Useful since compile has to load
   * the file into memory anyway, and the caller also needs the contents.
   */
  @VisibleForTesting
  static final class FileAndContents {
    final File file;
    final byte[] contents;

    FileAndContents(File file, byte[] contents) {
      this.file = file;
      this.contents = contents;
    }
  }

  /**
   * Instance of {@link ErrorReporter} passed to the javascript compiler.
   */
  private static class JsErrorReporter implements ErrorReporter {

    @Override
    public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
      throw new AssetCompilationException(getErrorMessage(sourceName, line, message, "error"));
    }

    @Override
    public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
      logger.warn(getErrorMessage(sourceName, line, message, "warning"));
    }

    @Override
    public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource,
        int lineOffset) {
      return new EvaluatorException(getErrorMessage(sourceName, line, message, "runtime error"));
    }

    private String getErrorMessage(String sourceFile, int line, String message, String errorType) {
      return String.format("Javascript %s in %s: %d: %s", errorType, sourceFile, line, message);
    }
  }

  /**
   * Minifies the given .css file and returns the file and its contents.
   *
   * @param uncompiled uncompiled .css file
   * @return File that has been minified.  The file name will contain a hash reflecting
   *     the file's contents.
   * @throws IOException
   */
  private FileAndContents compileAsset(AssetType assetType, File uncompiled) throws IOException {

    // Compile to memory instead of a file directly, since we will need the raw bytes
    // in order to generate a hash (which appears in the filename).
    ByteArrayOutputStream baos = new ByteArrayOutputStream(BUFFER_SIZE);
    Closer closer = Closer.create();
    try {
      InputStreamReader isr = closer.register(new InputStreamReader(new FileInputStream(uncompiled)));
      OutputStreamWriter osw = closer.register(new OutputStreamWriter(baos));
      if (assetType == AssetType.CSS) {
        CssCompressor compressor = new CssCompressor(isr);
        compressor.compress(osw, -1);
      } else if (assetType == AssetType.JS) {
        JavaScriptCompressor compressor = new JavaScriptCompressor(isr, new JsErrorReporter());
        compressor.compress(osw, -1, true, false, true, false);
      } else {
        throw new IllegalArgumentException("Unexpected asset type " + assetType);
      }
    } catch (Throwable t) {
      throw closer.rethrow(t);
    } finally {
      closer.close();
    }
    byte[] contents = baos.toByteArray();

    String name = String.format("asset_%s%s", getFingerprint(contents), assetType.getExtension());
    File result = new File(getCompiledFilePath(name));

    // Overwrite if the file already exists.
    if (result.exists()) {
      result.delete();
    }
    closer = Closer.create();
    try {
      OutputStream os = new FileOutputStream(result);
      IOUtils.write(contents, os);
    } catch (Throwable t) {
      throw closer.rethrow(t);
    } finally {
      closer.close();
    }
    return new FileAndContents(result, contents);
  }

  /**
   * Generates a fingerprint based on the data passed in that is suitable for use in
   * a filename or servlet path.
   *
   * @param bytes data to fingerprint
   * @return fingerprint
   */
  private String getFingerprint(byte[] bytes) {
    try {
      MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
      messageDigest.update(bytes);
      bytes = messageDigest.digest();
    } catch (NoSuchAlgorithmException ex) {
      throw new RuntimeException(ex);
    }
    BASE64Encoder encoder = new BASE64Encoder();
    String result = encoder.encodeBuffer(bytes);

    // Replace slashes with underscores and removing the trailing "=".
    result = result.replace('/', '_').trim();
    return result.substring(0, result.length() - 1);
  }

  /**
   * Returns the absolute, filesystem path to where a compiled asset should be.
   * Note that this only calculates the path, and does not validate that the
   * file actually exists.
   *
   * @param basename filename of the asset
   * @return absolute path to the asset file
   */
  private String getCompiledFilePath(String basename) {
    return runtimeConfiguration.getCompiledAssetDir() + File.separator + basename;
  }
}
