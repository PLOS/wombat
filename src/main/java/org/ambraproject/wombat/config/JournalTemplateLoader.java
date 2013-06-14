package org.ambraproject.wombat.config;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.TemplateLoader;

import java.io.File;
import java.io.IOException;
import java.util.Map;

class JournalTemplateLoader extends DelegatingTemplateLoader {

  /**
   * Map journal keys to theme build locations.
   */
  private final ImmutableMap<String, TemplateLoader> loaders;

  JournalTemplateLoader(ThemeAccessor accessor, Map<String, ThemeTree.Node> journals) throws IOException {
    this.loaders = buildLoaders(accessor, journals);
  }

  private static ImmutableMap<String, TemplateLoader> buildLoaders(ThemeAccessor accessor,
                                                                   Map<String, ThemeTree.Node> journals)
      throws IOException {
    ImmutableMap.Builder<String, TemplateLoader> builder = ImmutableMap.builder();
    for (Map.Entry<String, ThemeTree.Node> entry : journals.entrySet()) {
      String key = entry.getKey();
      ThemeTree.Node theme = entry.getValue();
      File themeBuildLocation = accessor.getThemeBuildLocation(theme.getKey());
      TemplateLoader loader = new FileTemplateLoader(themeBuildLocation);
      builder.put(key, loader);
    }
    return builder.build();
  }

  @Override
  protected TemplateLoader delegate(String key) {
    TemplateLoader loader = loaders.get(key);
    Preconditions.checkArgument(loader != null);
    return loader;
  }

}
