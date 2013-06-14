package org.ambraproject.wombat.config;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Closer;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * A Spring bean that encapsulates all of the inherited themes as they are "built" on disk.
 * <p/>
 * The current strategy for doing inheritance logic in themes is simple and stupid: build a snapshot of the leaf node in
 * an actual directory on disk, with all files inherited from the parent themes copied into place. Then a {@link
 * freemarker.cache.TemplateLoader} for that theme can just point to the directory. This has costs and should be
 * reconsidered later in development.
 * <p/>
 * The point of this class is to do the necessary copying to disk at the time the object is constructed, and then expose
 * the results through its public method. Therefore, Spring will kick off the copying as a side effect of constructing
 * the bean, during application start-up.
 * <p/>
 * This should be obviated with more sophisticated use of {@code TemplateLoader}s if possible. In the meantime, look
 * into how much of a Bad Thing it is to have an expensive side effect to bean construction in general. Also need to
 * watch out for concurrent modification to files at the {@code buildPath}.
 */
class ThemeAccessor {

  // Keys are theme keys. Values are the root paths to where the themes were "built" on disk.
  private final ImmutableMap<String, File> themeBuildLocations;

  private ThemeAccessor(Map<String, File> themeBuildLocations) {
    this.themeBuildLocations = ImmutableMap.copyOf(themeBuildLocations);
  }

  public File getThemeBuildLocation(String key) {
    File location = themeBuildLocations.get(Preconditions.checkNotNull(key));
    if (location == null) {
      throw new IllegalArgumentException("No theme configured with the theme key: " + key);
    }
    return location;
  }


  private static class ThemeFile {
    private final File themeRoot; // the absolute root path of the theme to which the file belongs (e.g., /var/themes/my_theme/)
    private final File filePath; // an absolute path to an actual file (e.g., /var/themes/my_theme/foo/home.ftl)
    private final String fileName; // the name of the file within this theme (e.g., "foo/home.ftl")

    private ThemeFile(File themeRoot, File filePath) {
      Preconditions.checkArgument(filePath.toString().startsWith(themeRoot.toString()));
      this.themeRoot = themeRoot;
      this.filePath = filePath;
      this.fileName = filePath.toString().substring(themeRoot.toString().length());
    }

    @Override
    public String toString() {
      return filePath.toString();
    }
  }

  static ThemeAccessor buildOnDisk(File buildPath, ThemeTree themeTree) throws IOException {
    Collection<ThemeTree.Node> themes = themeTree.getNodes();
    Map<String, Collection<ThemeFile>> themeFiles = Maps.newHashMapWithExpectedSize(themes.size());
    for (ThemeTree.Node theme : themes) {
      Collection<ThemeFile> files = readFileTree(theme.getDefinitionLocation());
      themeFiles.put(theme.getKey(), files);
    }

    Map<String, File> themeBuildLocations = Maps.newTreeMap();
    for (ThemeTree.Node theme : themes) {
      File buildLocation = buildTheme(buildPath, theme, themeFiles);
      themeBuildLocations.put(theme.getKey(), buildLocation);
    }
    return new ThemeAccessor(themeBuildLocations);
  }

  private static Collection<ThemeFile> readFileTree(File themeRoot) {
    return readFileTree(Lists.<ThemeFile>newArrayList(), themeRoot, themeRoot);
  }

  private static Collection<ThemeFile> readFileTree(Collection<ThemeFile> toModify, File themeRoot, File file) {
    toModify.add(new ThemeFile(themeRoot, file));
    File[] contents = file.listFiles();
    if (contents != null) {
      // It's a directory; need to recur
      for (File child : contents) {
        readFileTree(toModify, themeRoot, child);
      }
    }
    return toModify;
  }

  private static File buildTheme(File buildPath, ThemeTree.Node theme, Map<String, Collection<ThemeFile>> themeFiles)
      throws IOException {
    if (!theme.getParent().isPresent()) {
      // Root themes don't need to be built; we can just return the input location
      return theme.getDefinitionLocation();
    }

    // Else, need to do inheritance logic by building the full theme on disk.
    // Build up a map of which files to write, to avoid actually overwriting on disk.
    Map<String, File> fileSources = Maps.newHashMap();
    ThemeTree.Node cursor = theme;
    while (cursor != null) {
      Collection<ThemeFile> fileSet = themeFiles.get(cursor.getKey());
      for (ThemeFile file : fileSet) {
        if (!fileSources.containsKey(file.fileName)) {
          fileSources.put(file.fileName, file.filePath);
        } // else, a child's file that overrides this one is already in place
      }
      cursor = cursor.getParent().orNull();
    }

    File buildRoot = new File(buildPath, theme.getKey());
    for (Map.Entry<String, File> entry : fileSources.entrySet()) {
      File source = entry.getValue();
      if (!source.isDirectory()) {
        String fileName = entry.getKey();
        File destination = new File(buildRoot, fileName);
        copyFile(source, destination);
      }
    }
    return buildRoot;
  }

  private static void copyFile(File source, File destination) throws IOException {
    destination.getParentFile().mkdirs();

    Closer closer = Closer.create();
    try {
      FileInputStream input = closer.register(new FileInputStream(source));
      FileOutputStream output = closer.register(new FileOutputStream(destination));
      IOUtils.copy(input, output); // buffered
    } catch (Throwable t) {
      throw closer.rethrow(t);
    } finally {
      closer.close();
    }
  }

}
