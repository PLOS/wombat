package org.ambraproject.wombat.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.cache.WebappTemplateLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

class JournalTemplateLoader extends DelegatingTemplateLoader {
  private static final Logger log = LoggerFactory.getLogger(JournalTemplateLoader.class);

  private final TemplateLoader internalResource;
  private final ImmutableMap<String, TemplateLoader> loaders; // keyed by journal

  JournalTemplateLoader(ServletContext servletContext, Map<String, ThemeTree.Node> journals) throws IOException {
    this.internalResource = new WebappTemplateLoader(servletContext, "/WEB-INF/views/");
    this.loaders = buildLoaders(journals);
  }

  private ImmutableMap<String, TemplateLoader> buildLoaders(Map<String, ThemeTree.Node> journals) throws IOException {
    ImmutableMap.Builder<String, TemplateLoader> builder = ImmutableMap.builder();
    for (Map.Entry<String, ThemeTree.Node> entry : journals.entrySet()) {
      String key = entry.getKey();
      ThemeTree.Node theme = entry.getValue();

      List<TemplateLoader> loaders = Lists.newArrayList();
      loaders.add(internalResource);
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
    if (loader == null) {
      log.warn("Key not matched: {}", key);
      return internalResource;
    }
    return loader;
  }

}
