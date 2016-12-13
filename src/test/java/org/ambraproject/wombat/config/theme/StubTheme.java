package org.ambraproject.wombat.config.theme;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import freemarker.cache.TemplateLoader;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.url.SiteRequestScheme;
import org.apache.commons.io.input.ReaderInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StubTheme extends Theme {

  private final ImmutableMap<String, byte[]> staticResources;
  private final ImmutableMap<String, String> templates;
  private final ImmutableMap<String, String> configMaps;

  private StubTheme(Builder builder) {
    super(builder.themeKey, ImmutableList.of());
    this.staticResources = ImmutableMap.copyOf(builder.staticResources);
    this.templates = ImmutableMap.copyOf(builder.templates);
    this.configMaps = ImmutableMap.copyOf(Maps.transformValues(builder.configMaps, (new Gson())::toJson));
  }

  @Override
  public TemplateLoader getTemplateLoader() throws IOException {
    return new TemplateLoader() {
      @Override
      public Object findTemplateSource(String name) throws IOException {
        return templates.get(name);
      }

      @Override
      public long getLastModified(Object templateSource) {
        return -1;
      }

      @Override
      public Reader getReader(Object templateSource, String encoding) throws IOException {
        return new StringReader((String) templateSource);
      }

      @Override
      public void closeTemplateSource(Object templateSource) throws IOException {
      }
    };
  }

  private static final Pattern CONFIG_PATH_PATTERN = Pattern.compile("config/(.*)\\.json");

  @Override
  protected InputStream fetchStaticResource(String path) throws IOException {
    Matcher configPathMatcher = CONFIG_PATH_PATTERN.matcher(path);
    if (configPathMatcher.matches()) {
      String configName = configPathMatcher.group(1);
      String configMap = configMaps.get(configName);
      if (configMap != null) {
        return new ReaderInputStream(new StringReader(configMap), YAML_CONFIG_CHARSET);
      }
    }

    byte[] resource = staticResources.get(path);
    return (resource == null) ? null : new ByteArrayInputStream(resource);
  }

  @Override
  protected ResourceAttributes fetchResourceAttributes(String path) throws IOException {
    byte[] resource = staticResources.get(path);
    return (resource == null) ? null : new ResourceAttributes() {
      @Override
      public long getLastModified() {
        return 0;
      }

      @Override
      public long getContentLength() {
        return resource.length;
      }
    };
  }

  @Override
  protected Collection<String> fetchStaticResourcePaths(String root) throws IOException {
    return staticResources.keySet();
  }


  public Site wrapInStubSite(String siteKey) {
    return new Site(siteKey, this, SiteRequestScheme.builder().build());
  }

  public static class Builder {
    private final String themeKey;

    private final Map<String, byte[]> staticResources = new HashMap<>();
    private final Map<String, String> templates = new HashMap<>();
    private final Map<String, Map<String, Object>> configMaps = new HashMap<>();

    public Builder(String themeKey, String journalKey, String journalName) {
      this.themeKey = themeKey;
      initializeJournalConfig(journalKey, journalName);
    }

    private void initializeJournalConfig(String journalKey, String journalName) {
      addConfigValue(Site.JOURNAL_KEY_PATH, Site.CONFIG_KEY_FOR_JOURNAL, journalKey);
      addConfigValue(Site.JOURNAL_KEY_PATH, Site.JOURNAL_NAME, journalName);
    }

    public Builder addStaticResource(String name, byte[] content) {
      staticResources.put(name, content.clone());
      return this;
    }

    public Builder addTemplate(String templateName, String templateContent) {
      templates.put(templateName, templateContent);
      return this;
    }

    public Builder addConfigValue(String configMapName, String configKey, Object value) {
      Map<String, Object> configMap = configMaps.computeIfAbsent(configMapName, n -> new HashMap<>());
      configMap.put(configKey, value);
      return this;
    }

    public StubTheme build() {
      return new StubTheme(this);
    }
  }

}
