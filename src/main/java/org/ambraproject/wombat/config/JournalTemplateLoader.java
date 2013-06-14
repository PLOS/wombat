package org.ambraproject.wombat.config;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

class JournalTemplateLoader extends DelegatingTemplateLoader {

  /**
   * Map journal keys to theme build locations.
   */
  private final ImmutableMap<String, TemplateLoader> loaders;

  JournalTemplateLoader(Map<String, ThemeTree.Node> journals) throws IOException {
    this.loaders = buildLoaders(journals);
  }

  private static ImmutableMap<String, TemplateLoader> buildLoaders(Map<String, ThemeTree.Node> journals)
      throws IOException {
    ImmutableMap.Builder<String, TemplateLoader> builder = ImmutableMap.builder();
    for (Map.Entry<String, ThemeTree.Node> entry : journals.entrySet()) {
      String key = entry.getKey();
      ThemeTree.Node theme = entry.getValue();

      List<TemplateLoader> loaders = Lists.newArrayList();
      for (File themeLocation : theme.getLocations()) {
        loaders.add(new FileTemplateLoader(themeLocation));
      }

      MultiTemplateLoader multiLoader = new MultiTemplateLoader(loaders.toArray(new TemplateLoader[loaders.size()]));
      builder.put(key, multiLoader);
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
