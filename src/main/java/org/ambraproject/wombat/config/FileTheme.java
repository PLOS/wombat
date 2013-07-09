package org.ambraproject.wombat.config;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.TemplateLoader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileTheme extends Theme {

  private final File root;
  private final FileTemplateLoader templateLoader;

  protected FileTheme(String key, Theme parent, File root) throws IOException {
    super(key, parent);
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

}
