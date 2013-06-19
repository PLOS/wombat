package org.ambraproject.wombat.config;

import freemarker.cache.TemplateLoader;
import freemarker.cache.WebappTemplateLoader;

import javax.servlet.ServletContext;
import java.io.IOException;

/**
 * A theme defined within the webapp.
 */
public class InternalTheme extends Theme {

  private final WebappTemplateLoader templateLoader;

  public InternalTheme(String key, Theme parent, ServletContext servletContext, String resourcePath) {
    super(key, parent);
    this.templateLoader = new WebappTemplateLoader(servletContext, resourcePath);
  }

  @Override
  public TemplateLoader getTemplateLoader() throws IOException {
    return templateLoader;
  }

}
