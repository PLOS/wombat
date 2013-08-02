package org.ambraproject.wombat.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

class JournalTemplateLoader extends DelegatingTemplateLoader {
  private static final Logger log = LoggerFactory.getLogger(JournalTemplateLoader.class);

  private final ImmutableMap<String, TemplateLoader> loaders; // keyed by journal

  JournalTemplateLoader(JournalThemeMap journalThemeMap) throws IOException {
    this.loaders = buildLoaders(journalThemeMap);
  }

  private static ImmutableMap<String, TemplateLoader> buildLoaders(JournalThemeMap journalThemeMap)
      throws IOException {
    ImmutableMap.Builder<String, TemplateLoader> builder = ImmutableMap.builder();
    for (Map.Entry<String, Theme> entry : journalThemeMap.asEntrySet()) {
      String key = entry.getKey();
      Theme leaf = entry.getValue();

      List<TemplateLoader> loaders = Lists.newArrayList();
      for (Theme theme : leaf.getChain()) {
        loaders.add(theme.getTemplateLoader());
      }

      MultiTemplateLoader multiLoader = new MultiTemplateLoader(loaders.toArray(new TemplateLoader[loaders.size()]));
      builder.put(key, multiLoader);
    }
    return builder.build();
  }

  @Override
  protected TemplateLoader delegate(String key) {
    TemplateLoader loader = loaders.get(key);
    if (loader == null) {
      throw new RuntimeException("Key not matched: " + key);
    }
    return loader;
  }

}
