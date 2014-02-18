package org.ambraproject.wombat.config;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.io.Files;
import freemarker.cache.TemplateLoader;
import org.apache.commons.io.IOUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public abstract class Theme {

  private final String key;
  private final Optional<Theme> parent;

  protected Theme(String key, Theme parent) {
    Preconditions.checkArgument(!key.isEmpty());
    Preconditions.checkArgument(key.startsWith(".") == (this instanceof InternalTheme));
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

  static final Function<Theme, String> GET_KEY = new Function<Theme, String>() {
    @Override
    public String apply(Theme input) {
      return input.getKey();
    }
  };

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
   * Return a collection of all static resources available from a root path in this theme and its parents. The returned
   * paths are relative to the provided root, and should be concatenated to it before passing to {@link
   * #getStaticResource}. (See the method body of {@link #dumpToTemporaryDirectory} for an example usage.)
   *
   * @param root the root path
   * @return the static resource paths
   * @throws IOException
   */
  public final ImmutableSet<String> getStaticResourcePaths(String root) throws IOException {
    if (!root.endsWith("/")) {
      root += "/";
    }
    Set<String> paths = Sets.newTreeSet();
    for (Theme theme : getChain()) {
      Collection<String> themePaths = theme.fetchStaticResourcePaths(root);
      if (themePaths != null) {
        paths.addAll(themePaths);
      }
    }
    return ImmutableSet.copyOf(paths);
  }

  /**
   * Return a collection of all static resources available from a root path in this theme. Return values must follow the
   * same contract as in {@link #getStaticResourcePaths}.
   *
   * @param root the root path
   * @return the static resource paths
   * @throws IOException
   */
  protected abstract Collection<String> fetchStaticResourcePaths(String root) throws IOException;

  /**
   * Dump all static resources in this theme and its parents into a single temporary directory.
   *
   * @return the new temporary directory
   * @throws IOException
   */
  public final File dumpToTemporaryDirectory(String root) throws IOException {
    File tempDir = Files.createTempDir();
    for (String path : getStaticResourcePaths(root)) {
      String readLocation = root + path;
      File writeLocation = new File(tempDir, path);
      try (InputStream inputStream = getStaticResource(readLocation);
           OutputStream outputStream = new FileOutputStream(writeLocation)) {
        IOUtils.copy(inputStream, outputStream);
      }
    }
    return tempDir;
  }


  /**
   * Read a set of configuration values from YAML (or JSON), overriding individual values from parent themes if
   * applicable. The path (plus a *.yaml or *.json extension) points to a file containing an object (i.e., key-value
   * map) in the special {@code config/} theme path.
   * <p/>
   * This is distinct from the other kinds of theme inheritance ({@link #getTemplateLoader} and {@link
   * #getStaticResource}), which override on a file-by-file basis. This method reads a map (if any) at the given path
   * from every theme in the inheritance chain, and builds the result map by overriding individual members.
   *
   * @param path a path within the theme's {@code config/} directory
   * @return a map of overridden values
   */
  public final Map<String, Object> getConfigMap(String path) throws IOException {
    String configPath = "config/" + path;
    Map<String, Object> values = Maps.newLinkedHashMap();
    for (Theme theme : getChain()) {
      Map<?, ?> valuesFromTheme = readYamlConfigValues(theme, configPath);
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

  private Map<?, ?> readYamlConfigValues(Theme theme, String configPath) throws IOException {
    // Allow either *.yaml or *.json for the filename, but complain if both appear in the same theme.
    // Allow mixing and matching of *.yaml and *.json across different (inheriting) themes.
    // We can get away with parsing both as YAML (because JSON is a subset of YAML),
    // but this needs more abstract handling by format if we ever support more than two formats.
    // We don't actually validate whether a *.json file is valid JSON or just YAML.
    try (InputStream yamlStream = theme.fetchStaticResource(configPath + ".yaml");
         InputStream jsonStream = theme.fetchStaticResource(configPath + ".json")) {
      if (yamlStream == null && jsonStream == null) {
        return null;
      } else if (yamlStream != null && jsonStream != null) {
        String message = String.format("Redundant files at %s.yaml and %s.json in theme \"%s\"",
            configPath, configPath, theme.getKey());
        throw new IllegalStateException(message);
      }
      InputStream streamToUse = (yamlStream != null) ? yamlStream : jsonStream;

      Yaml yaml = new Yaml(); // don't cache; it isn't threadsafe
      return yaml.loadAs(new InputStreamReader(streamToUse), Map.class);
    }
  }


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
