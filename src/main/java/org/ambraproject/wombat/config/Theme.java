package org.ambraproject.wombat.config;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.UnmodifiableIterator;
import freemarker.cache.TemplateLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class Theme {

  private final String key;
  private final Optional<Theme> parent;

  protected Theme(String key, Theme parent) {
    this.key = key;
    this.parent = Optional.fromNullable(parent);
  }

  /**
   * Get the theme's key. This is the name assigned in the webapp configuration.
   *
   * @return the key
   */
  public String getKey() {
    return key;
  }

  /**
   * Return the inheritance chain of themes, from leaf to root. The first element is guaranteed to be this object. Each
   * element is followed by its parent until a root theme is reached.
   *
   * @return the chain of themes
   */
  public final Iterable<Theme> getChain() {
    return new Iterable<Theme>() {
      @Override
      public Iterator<Theme> iterator() {
        return new UnmodifiableIterator<Theme>() {
          private Theme cursor = Theme.this;

          @Override
          public boolean hasNext() {
            return (cursor != null);
          }

          @Override
          public Theme next() {
            if (!hasNext()) {
              throw new NoSuchElementException();
            }
            Theme next = cursor;
            cursor = cursor.parent.orNull();
            return next;
          }
        };
      }
    };
  }


  /**
   * Return a loader for this theme's templates.
   *
   * @return the loader
   * @throws IOException if an error occurs accessing the template files
   */
  public abstract TemplateLoader getTemplateLoader() throws IOException;

  /**
   * Open a stream to a static resource defined in this theme or the nearest possible parent. Return {@code null} if the
   * resource is not available at all.
   * <p/>
   * It is the caller's responsibility to close the returned stream.
   *
   * @param path the path to the static resource
   * @return the stream to a static resource
   * @throws IOException if an error occurs accessing the resource
   */
  public final InputStream getStaticResource(String path) throws IOException {
    Preconditions.checkNotNull(path);
    for (Theme theme : getChain()) {
      InputStream stream = theme.fetchStaticResource(path);
      if (stream != null) {
        return stream;
      }
    }
    return null; // TODO: IOException better here?
  }

  /**
   * Open a stream to a static resource, or return {@code null} if that resource is not defined in this theme. A return
   * value of {@code null} indicates that the resource may still be defined in this theme's parent, so the caller should
   * look there next.
   *
   * @param path the path to the static resource
   * @return a stream to a static resource, or return {@code null} if that resource is not defined in this theme
   * @throws IOException if an error occurs accessing the resource
   */
  protected abstract InputStream fetchStaticResource(String path) throws IOException;

}
