package org.ambraproject.wombat.config.theme;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import freemarker.cache.TemplateLoader;
import freemarker.cache.WebappTemplateLoader;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Set;

/**
 * A theme defined within the webapp.
 */
public class InternalTheme extends Theme {

  private final ServletContext servletContext;
  private final String resourceRoot;
  private final WebappTemplateLoader templateLoader;

  public InternalTheme(String key, List<? extends Theme> parents, ServletContext servletContext, String resourcePath) {
    super(key, parents);
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
    URL resource = servletContext.getResource(resourceRoot + path);
    if (resource == null) {
      return null;
    }
    URLConnection conn = resource.openConnection();
    return conn.getContentLengthLong() > 0 ? new UrlConnFileResourceAttributes(conn) : null;
  }

  private static class UrlConnFileResourceAttributes implements ResourceAttributes {
    private final long lastModified;
    private final long contentLength;

    protected UrlConnFileResourceAttributes(URLConnection conn) {
      contentLength = conn.getContentLengthLong();
      lastModified = conn.getLastModified();
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
