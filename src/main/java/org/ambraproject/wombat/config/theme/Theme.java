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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import freemarker.cache.TemplateLoader;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.service.UnmatchedSiteException;
import org.apache.commons.io.IOUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class Theme {

  private final String key;
  private final ImmutableList<Theme> parents;

  protected Theme(String key, List<? extends Theme> parents) {
    Preconditions.checkArgument(!key.isEmpty());
    Preconditions.checkArgument(key.startsWith(".") == (this instanceof InternalTheme));
    this.key = key;
    this.parents = ImmutableList.copyOf(parents);
  }

  /**
   * Get the theme's key. This is the name assigned in the webapp configuration.
   *
   * @return the key
   */
  public String getKey() {
    return key;
  }


  private transient Iterable<Theme> iterableView;

  /**
   * Return the inheritance chain of themes, from leaf to root. The first element is guaranteed to be this object. Each
   * element is followed by its parent until a root theme is reached.
   *
   * @return the chain of themes
   */
  public final Iterable<Theme> getChain() {
    return (iterableView != null) ? iterableView :
        (iterableView = ImmutableList.copyOf(new InheritanceChain()));
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
   * Result class for fetchResourceAttributes.
   */
  public static interface ResourceAttributes {
    /**
     * @return last modified time of the given resource
     */
    long getLastModified();

    /**
     * @return length of the requested resource
     */
    long getContentLength();
  }

  /**
   * Returns the last modified time and the content length for a given resource returned by this theme.
   *
   * @param path the static resource path
   * @return see {@link ResourceAttributes}
   * @throws IOException
   */
  public ResourceAttributes getResourceAttributes(String path) throws IOException {
    Preconditions.checkNotNull(path);
    for (Theme theme : getChain()) {
      ResourceAttributes result = theme.fetchResourceAttributes(path);
      if (result != null) {
        return result;
      }
    }
    return null; // TODO: IOException better here?
  }

  protected abstract ResourceAttributes fetchResourceAttributes(String path) throws IOException;

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
  public final Map<String, Object> getConfigMap(String path) {
    String configPath = "config/" + path;
    Map<String, Object> values = Maps.newLinkedHashMap();
    for (Theme theme : getChain()) {
      Map<?, ?> valuesFromTheme;
      try {
        valuesFromTheme = readYamlConfigValues(theme, configPath);
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


  /**
   * Resolve a journal key for another site into that site. Uses theme-specific config hooks to define links between
   * sites (for example, mobile themes link to mobile sites) before defaulting to searching the site set for a site with
   * that journal key.
   *
   * @param siteSet    the system's set of all sites
   * @param journalKey a journal key belonging to another site
   * @return a site for the journal key, using preferences defined for this theme if any
   */
  public Site resolveForeignJournalKey(SiteSet siteSet, String journalKey) throws UnmatchedSiteException {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(journalKey));
    Map<?, ?> otherJournals = (Map<?, ?>) getConfigMap("journal").get("otherJournals");
    if (otherJournals != null) {
      String otherSiteKey = (String) otherJournals.get(journalKey);
      if (otherSiteKey != null) {
        return siteSet.getSite(otherSiteKey);
      } // else, fall through and try the other way
    }

    // No site name was explicitly given for the journal key, so just search siteSet for it.
    for (Site candidateSite : siteSet.getSites()) {
      if (candidateSite.getJournalKey().equals(journalKey)) {
        return candidateSite;
      }
    }
    throw new UnmatchedSiteException("Journal key not matched to site: " + journalKey);
  }


  /**
   * Iterate over this theme and its parents in topological sort order. This means, if two paths lead to a common
   * parent, explore each path fully before hitting the parent. Uses Kahn's algorithm.
   */
  private class InheritanceChain extends AbstractIterator<Theme> {
    private final SetMultimap<Theme, Theme> childMap; // map from parents to their children
    private final Set<Theme> yielded; // themes already returned by this iterator
    private final Deque<Theme> stack; // candidates for the next iteration

    private InheritanceChain() {
      childMap = buildChildMap(Theme.this);
      yielded = Sets.newHashSet();
      stack = Lists.newLinkedList(); // we will remove from the middle occasionally
      stack.add(Theme.this);
    }

    /*
     * Set up links from parents to children, since Theme objects don't natively keep track of their children. Note that
     * this causes initialization of the iterator to take O(n) time, though the full trip is still only O(n).
     */
    private SetMultimap<Theme, Theme> buildChildMap(Theme root) {
      SetMultimap<Theme, Theme> childMap = HashMultimap.create();
      Deque<Theme> tempStack = new ArrayDeque<>();
      tempStack.push(root);
      while (!tempStack.isEmpty()) {
        Theme child = tempStack.pop();
        for (Theme parent : child.parents) {
          childMap.put(parent, child);
          tempStack.push(parent);
        }
      }
      return childMap;
    }

    @Override
    protected Theme computeNext() {
      for (Iterator<Theme> iterator = stack.iterator(); iterator.hasNext(); ) {
        Theme candidate = iterator.next();
        if (yielded.contains(candidate)) {
          iterator.remove();
        } else if (!childMap.containsKey(candidate)) {
          // The candidate has no children that haven't been yielded yet, so we will yield this one.

          iterator.remove(); // Do this first. The iterator will be clobbered once we add to the stack.

          // Push all parents onto the stack, and remove the child from the parents' child sets.
          // Push in an order such that we do depth-first search, using the same order as in the parents list.
          for (Theme parent : candidate.parents.reverse()) {
            stack.addFirst(parent);
            childMap.get(parent).remove(candidate);
          }

          yielded.add(candidate);
          return candidate;
        } // Else, continue down the stack until we find a node with no outstanding children.
      }
      if (stack.isEmpty()) {
        return endOfData();
      }
      throw new RuntimeException("Invalid graph (expected to throw ThemeConfigException when built)");
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
