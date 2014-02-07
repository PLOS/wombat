package org.ambraproject.wombat.config;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.io.Closer;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import freemarker.cache.TemplateLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
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


  /**
   * Read a set of configuration values from JSON, overriding individual values from parent themes if applicable. The
   * path must point to a JSON file containing an object (i.e., key-value map) in the special {@code config/} theme
   * path.
   * <p/>
   * This is distinct from the other kinds of theme inheritance ({@link #getTemplateLoader} and {@link
   * #getStaticResource}), which override on a file-by-file basis. This method reads a JSON map (if any) at the given
   * path from every theme in the inheritance chain, and builds the result map by overriding individual members.
   *
   * @param path a path within the theme's {@code config/} directory
   * @return a map of overridden values
   */
  public final Map<String, Object> getConfigMap(String path) {
    String configPath = "config/" + path;
    Map<String, Object> values = Maps.newLinkedHashMap();
    for (Theme theme : getChain()) {
      Map<?, ?> valuesFromTheme;
      try {
        valuesFromTheme = readJsonConfigValues(theme, configPath);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      if (valuesFromTheme == null) {
        continue; // no overrides present in this theme
      }

      for (Map.Entry<?, ?> entry : valuesFromTheme.entrySet()) {
        String entryKey = (String) entry.getKey();
        if (!values.containsKey(entryKey)) {
          values.put(entryKey, entry.getValue());
        } // else, do not overwrite, because the value was put there by a theme that overrides the current one
      }
    }
    return values;
  }

  private static Map<?, ?> readJsonConfigValues(Theme theme, String configPath) throws IOException {
    Closer closer = Closer.create();
    try {
      InputStream inputStream = theme.fetchStaticResource(configPath);
      if (inputStream == null) {
        return null;
      }
      closer.register(inputStream);
      return GSON.fromJson(new JsonReader(new InputStreamReader(inputStream)), Map.class);
    } catch (Throwable t) {
      throw closer.rethrow(t);
    } finally {
      closer.close();
    }
  }

  private static final Gson GSON = new Gson();


  @Override
  public String toString() {
    return key;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    return key.equals(((Theme) o).key);
  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }

}
