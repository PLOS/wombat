package org.ambraproject.wombat.config.theme;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import freemarker.cache.TemplateLoader;
import org.apache.commons.io.input.ReaderInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class SimpleStubTheme extends Theme {

  private final String journalKey;

  public SimpleStubTheme(String key, String journalKey, Theme... parents) {
    super(key, ImmutableList.copyOf(parents));
    this.journalKey = Preconditions.checkNotNull(journalKey);
  }

  @Override
  public TemplateLoader getTemplateLoader() throws IOException {
    return null;
  }

  @Override
  protected InputStream fetchStaticResource(String path) throws IOException {
    if (path.equals("config/journal.json")) {
      Map<String, Object> journalConfigMap = getJournalConfigMap();
      return new ReaderInputStream(new StringReader(new Gson().toJson(journalConfigMap)));
    }
    return null;
  }

  protected Map<String, Object> getJournalConfigMap() {
    Map<String, Object> configMap = new LinkedHashMap<>();
    configMap.put("journalKey", journalKey);
    configMap.put("journalName", journalKey);
    return configMap;
  }

  @Override
  protected ResourceAttributes fetchResourceAttributes(String path) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  protected Collection<String> fetchStaticResourcePaths(String root) throws IOException {
    throw new UnsupportedOperationException();
  }

}
