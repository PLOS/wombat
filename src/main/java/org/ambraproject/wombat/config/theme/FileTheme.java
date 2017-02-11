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

package org.ambraproject.wombat.config.theme;

import com.google.common.collect.Sets;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.TemplateLoader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Set;

public class FileTheme extends Theme {

  private final File root;
  private final FileTemplateLoader templateLoader;

  FileTheme(String key, List<Theme> parents, File root) {
    super(key, parents);
    this.root = root;
    try {
      this.templateLoader = new FileTemplateLoader(root);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public TemplateLoader getTemplateLoader() {
    return templateLoader;
  }

  @Override
  protected InputStream fetchStaticResource(String path) throws IOException {
    File file = new File(root, path);
    return file.exists() ? new BufferedInputStream(new FileInputStream(file)) : null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected ResourceAttributes fetchResourceAttributes(String path) {
    File file = new File(root, path);
    return file.exists() ? new FileResourceAttributes(file) : null;
  }

  private static class FileResourceAttributes implements ResourceAttributes {
    private final long lastModified;
    private final long contentLength;

    protected FileResourceAttributes(File file) {
      contentLength = file.length();
      lastModified = file.lastModified();
    }

    @Override
    public long getLastModified() {
      return lastModified;
    }

    @Override
    public long getContentLength() {
      return contentLength;
    }
  }

  protected Collection<String> fetchStaticResourcePaths(String searchRoot) throws IOException {
    File searchRootFile = new File(root, searchRoot);
    Set<String> filePaths = Sets.newTreeSet();
    Deque<File> queue = new ArrayDeque<>();
    queue.add(searchRootFile);
    while (!queue.isEmpty()) {
      File file = queue.removeFirst();
      if (file.isDirectory()) {
        queue.addAll(Arrays.asList(file.listFiles()));
      } else if (file.exists()) {
        String relativePath = file.getAbsolutePath().substring(searchRootFile.getAbsolutePath().length() + 1);
        filePaths.add(relativePath);
      }
    }
    return filePaths;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    return root.equals(((FileTheme) o).root);
  }

  @Override
  public int hashCode() {
    return 31 * super.hashCode() + root.hashCode();
  }

}
