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

import com.google.common.base.Preconditions;
import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;
import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.theme.Theme;
import org.ambraproject.wombat.util.CacheKey;
import org.apache.commons.io.IOUtils;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.cache.Cache;
import javax.cache.CacheManager;
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
import java.util.List;

public class AssetServiceImpl implements AssetService {

  private static final Logger logger = LoggerFactory.getLogger(AssetServiceImpl.class);

  private static final int BUFFER_SIZE = 8192;

  /**
   * We only store the contents of compiled assets in memcache if they are smaller than this value.  Otherwise, the
   * controller that serves them will need to read them from disk.
   */
  private static final int MAX_ASSET_SIZE_TO_CACHE = 1024 * 1024;

  @Autowired
  private RuntimeConfiguration runtimeConfiguration;

  @Autowired
  private Cache<String, String> assetFilenameCache;

  @Autowired
  private Cache<String, Object> assetContentCache;

  private static final Object ASSET_COMPILATION_LOCK = new Object();

  /*
   * We cache data at two steps in the process of compiling assets:
   * (1) mapping source filenames onto compiled content, so that we don't have to compile them more than once; and
   * (2) mapping compiled filenames onto their content, so that we can read them from Memcached instead of from disk.
   *
   * The two classes below are used to make this distinction explicit.
   */

  /*
   *
   * A hash representing a list of asset source filenames (and the site they belong to).
   * Cached in order to tell whether we need to compile them.
   */
  private static class SourceFilenamesDigest {
    private final AssetType assetType;
    private final Site site;
    private final List<String> filenames;

    private SourceFilenamesDigest(AssetType assetType, Site site, List<String> filenames) {
      this.assetType = assetType;
      this.site = site;
      this.filenames = filenames;
    }

    private String generateCacheKey() {
      String filenameDigest = CacheKey.createKeyHash(filenames);

      return String.format("%sFile:%s:%s", assetType.name().toLowerCase(), site, filenameDigest);
    }
  }

  /*
   * A hash representing the content of a compiled file. Used in two ways:
   * (1) as a filename to write the content to the local disk; and
   * (2) as a cache key to read the content from Memcached (which is faster than reading from disk).
   *
   * The actual hashing is done by the compileAsset method. This class may be instantiated either there (writing the
   * asset) or in response to a user request (reading the asset).
   */
  private class CompiledDigest {
    private final String name;

    private CompiledDigest(String name) {
      Preconditions.checkArgument(name.startsWith(AssetUrls.COMPILED_NAME_PREFIX));
      this.name = name;
    }

    /**
     * Returns the absolute, filesystem path to where a compiled asset should be. Note that this only indicates the
     * path, and does not validate that the file actually exists.
     *
     * @return absolute path to the asset file
     */
    private File getFile() {
      return new File(runtimeConfiguration.getCompiledAssetDir(), name);
    }

    /**
     * @return cache key where we can store/retrieve the contents of the compiled asset
     */
    private String getCacheKey() {
      int dotIndex = name.lastIndexOf('.');
      String assetType = name.substring(dotIndex + 1).toLowerCase();
      return String.format("%sContents:%s", assetType, name);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getCompiledAssetLink(AssetType assetType, List<String> filenames, Site site)
      throws IOException {

    // Keep a global lock on asset compilation.  We do this because we've had a problem when we
    // bring up wombat, and all the requests attempt to compile assets simultaneously, leading
    // to high load.
    // It might be possible to lock in a more granular fashion here (such as by assetType or
    // sourceCacheKey), but that might be prone to deadlock.
    synchronized (ASSET_COMPILATION_LOCK) {
      SourceFilenamesDigest sourceFilenamesDigest = new SourceFilenamesDigest(assetType, site, filenames);
      String sourceCacheKey = sourceFilenamesDigest.generateCacheKey();

      String compiledFilename = assetFilenameCache.get(sourceCacheKey);
      if (compiledFilename == null) {
        File concatenated = concatenateFiles(filenames, site, assetType.getExtension());
        CompiledAsset compiled = compileAsset(assetType, concatenated);
        concatenated.delete();
        compiledFilename = compiled.digest.getFile().getName();

        // We cache both the file name and the file contents (if it is small enough) in memcache.
        // In an ideal world, we would use the filename (including the hash of its contents) as
        // the only cache key.  However, it's potentially expensive to calculate that key since
        // you need the contents of the compiled file, which is why we do it this way.
        assetFilenameCache.put(sourceCacheKey, compiledFilename);
        if (compiled.contents.length < MAX_ASSET_SIZE_TO_CACHE) {
          String contentsCacheKey = compiled.digest.getCacheKey();
          assetContentCache.put(contentsCacheKey, compiled.contents);
        }
      }
      return AssetUrls.COMPILED_PATH_PREFIX + compiledFilename;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void serveCompiledAsset(String assetFilename, OutputStream outputStream) throws IOException {
    try {
      CompiledDigest digest = new CompiledDigest(assetFilename);
      byte[] cached = (byte[]) assetContentCache.get(digest.getCacheKey());
      try (InputStream is =
               (cached == null)
                   ? new FileInputStream(digest.getFile())
                   : new ByteArrayInputStream(cached)) {
        IOUtils.copy(is, outputStream);
      }
    } finally {
      outputStream.close();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long getLastModifiedTime(String assetFilename) {
    return new CompiledDigest(assetFilename).getFile().lastModified();
  }

  /**
   * Retrieves the given files and concatenates them into a single file.
   *
   * @param filenames list of servlet paths corresponding to the files to concatenate
   * @param site      specifies the journal/site
   * @param extension specifies the type of file created (e.g. ".css", ".js")
   * @return File with all filenames concatenated.  It is the responsibility of the caller to delete this file (since it
   * will end up being minified eventually).
   * @throws IOException
   */

  private File concatenateFiles(List<String> filenames, Site site, String extension) throws IOException {
    File result = File.createTempFile("concatenated_", extension);
    Theme theme = site.getTheme();
    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(result))) {
      for (String filename : filenames) {
        try (InputStream is = theme.getStaticResource(filename)) {
          if (is == null) {
            throw new RuntimeException(String.format("Static resource missing from %s: %s", site, filename));
          }
          IOUtils.copy(is, bos);
          bos.write('\n');  // just in case
        }
      }
    }
    return result;
  }

  /**
   * Simple class representing a File and its contents.  Useful since compile has to load the file into memory anyway,
   * and the caller also needs the contents.
   */
  private static final class CompiledAsset {
    private final CompiledDigest digest;
    private final byte[] contents;

    public CompiledAsset(CompiledDigest digest, byte[] contents) {
      this.digest = Preconditions.checkNotNull(digest);
      this.contents = Preconditions.checkNotNull(contents);
    }
  }

  /**
   * Instance of {@link ErrorReporter} passed to the javascript compiler.
   */
  private static class JsErrorReporter implements ErrorReporter {

    @Override
    public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
      throw new AssetCompilationException(getErrorMessage(sourceName, line, message, "error", lineSource));
    }

    @Override
    public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
      logger.warn(getErrorMessage(sourceName, line, message, "warning", lineSource));
    }

    @Override
    public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource,
                                           int lineOffset) {
      return new EvaluatorException(getErrorMessage(sourceName, line, message, "runtime error", lineSource));
    }

    private String getErrorMessage(String sourceFile, int line, String message, String errorType, String lineSource) {
      return String.format("Javascript %s in %s: %d: %s. Line source: %s", errorType, sourceFile, line, message, lineSource);
    }
  }

  /**
   * Minifies the given asset file and returns the file and its contents.
   *
   * @param uncompiled uncompiled asset file
   * @return File that has been minified.  The file name will contain a hash reflecting the file's contents.
   * @throws IOException
   */
  private CompiledAsset compileAsset(AssetType assetType, File uncompiled) throws IOException {

    // Compile to memory instead of a file directly, since we will need the raw bytes
    // in order to generate a hash (which appears in the filename).
    ByteArrayOutputStream baos = new ByteArrayOutputStream(BUFFER_SIZE);
    try (InputStreamReader isr = new InputStreamReader(new FileInputStream(uncompiled));
         OutputStreamWriter osw = new OutputStreamWriter(baos)) {
      if (assetType == AssetType.CSS) {
        CssCompressor compressor = new CssCompressor(isr);
        compressor.compress(osw, -1);
      } else if (assetType == AssetType.JS) {
        JavaScriptCompressor compressor = new JavaScriptCompressor(isr, new JsErrorReporter());
        compressor.compress(osw, -1, true, false, true, false);
      } else {
        throw new IllegalArgumentException("Unexpected asset type " + assetType);
      }
    }
    byte[] contents = baos.toByteArray();

    String contentHash = CacheKey.createContentHash(contents);
    CompiledDigest digest = new CompiledDigest(AssetUrls.COMPILED_NAME_PREFIX
        + contentHash + assetType.getExtension());
    File file = digest.getFile();

    // Overwrite if the file already exists.
    if (file.exists()) {
      file.delete();
    }
    try (OutputStream os = new FileOutputStream(file)) {
      IOUtils.write(contents, os);
    }
    return new CompiledAsset(digest, contents);
  }

}
