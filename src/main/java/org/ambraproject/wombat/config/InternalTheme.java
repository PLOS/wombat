package org.ambraproject.wombat.config;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import freemarker.cache.TemplateLoader;
import freemarker.cache.WebappTemplateLoader;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Set;

/**
 * A theme defined within the webapp.
 */
public class InternalTheme extends Theme {

  private final ServletContext servletContext;
  private final String resourceRoot;
  private final WebappTemplateLoader templateLoader;

  public InternalTheme(String key, Theme parent, ServletContext servletContext, String resourcePath) {
    super(key, parent);
    this.servletContext = Preconditions.checkNotNull(servletContext);
    this.resourceRoot = Preconditions.checkNotNull(resourcePath);
    this.templateLoader = new WebappTemplateLoader(servletContext, resourcePath);
  }

  @Override
  public TemplateLoader getTemplateLoader() throws IOException {
    return templateLoader;
  }

  @Override
  protected InputStream fetchStaticResource(String path) throws IOException {
    return servletContext.getResourceAsStream(resourceRoot + path);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected ResourceAttributes fetchResourceAttributes(String path) throws IOException {
    URLConnection conn = servletContext.getResource(resourceRoot + path).openConnection();
    return conn.getContentLengthLong() > 0 ? new ResourceAttributes(conn) : null;
  }

  @Override
  protected Collection<String> fetchStaticResourcePaths(String root) throws IOException {
    String contextRoot = resourceRoot + root;

    Set<String> relativePaths = Sets.newTreeSet();
    Deque<String> queue = new ArrayDeque<>();
    queue.add(contextRoot);
    while (!queue.isEmpty()) {
      String path = queue.removeFirst();
      if (path.endsWith("/")) {
        Set<String> childPaths = servletContext.getResourcePaths(path);
        if (childPaths != null) {
          for (String childPath : childPaths) {
            queue.add(childPath);
          }
        }
      } else {
        relativePaths.add(path.substring(contextRoot.length()));
      }
    }
    return relativePaths;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    return resourceRoot.equals(((InternalTheme) o).resourceRoot);
  }

  @Override
  public int hashCode() {
    return 31 * super.hashCode() + resourceRoot.hashCode();
  }

}
