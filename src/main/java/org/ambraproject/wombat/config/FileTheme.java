package org.ambraproject.wombat.config;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.TemplateLoader;

import java.io.File;
import java.io.IOException;

public class FileTheme extends Theme {

  private final FileTemplateLoader templateLoader;

  protected FileTheme(String key, Theme parent, File root) throws IOException {
    super(key, parent);
    this.templateLoader = new FileTemplateLoader(root);
  }

  @Override
  public TemplateLoader getTemplateLoader() {
    return templateLoader;
  }

}
