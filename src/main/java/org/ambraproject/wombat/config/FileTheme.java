package org.ambraproject.wombat.config;

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

  protected FileTheme(String key, List<Theme> parents, File root) throws IOException {
    super(key, parents);
    this.root = root;
    this.templateLoader = new FileTemplateLoader(root);
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
