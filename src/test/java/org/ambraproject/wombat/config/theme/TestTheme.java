package org.ambraproject.wombat.config.theme;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import freemarker.cache.TemplateLoader;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.site.url.SiteRequestScheme;
import org.apache.commons.io.input.ReaderInputStream;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Collection;
import java.util.Map;

import static org.testng.Assert.assertEquals;

public class TestTheme {

  private static abstract class StubTheme extends Theme {
    public StubTheme(String key) {
      super(key, ImmutableList.<Theme>of());
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

    protected abstract Map<String, Object> getJournalConfigMap();

    @Override
    protected ResourceAttributes fetchResourceAttributes(String path) throws IOException {
      return null;
    }

    @Override
    protected Collection<String> fetchStaticResourcePaths(String root) throws IOException {
      return null;
    }
  }

  @DataProvider
  public Object[][] booleans() {
    return new Object[][]{{false}, {true}};
  }

  @Test(dataProvider = "booleans")
  public void testResolveForeignJournalKey(final boolean useJournalKeyMap) throws Exception {
    SiteRequestScheme dummyScheme = SiteRequestScheme.builder().build();

    Theme homeTheme = new StubTheme("homeTheme") {
      @Override
      protected Map<String, Object> getJournalConfigMap() {
        ImmutableMap.Builder<String, Object> map = ImmutableMap.builder();
        map.put("journalKey", "homeJournal");
        if (useJournalKeyMap) {
          ImmutableMap<String, String> otherJournals = ImmutableMap.of("targetJournal", "targetSite");
          map.put("otherJournals", otherJournals);
        }
        return map.build();
      }
    };
    Site homeSite = new Site("homeSite", homeTheme, dummyScheme);

    Theme targetTheme = new StubTheme("targetTheme") {
      @Override
      protected Map<String, Object> getJournalConfigMap() {
        return ImmutableMap.<String, Object>of("journalKey", "targetJournal");
      }
    };
    Site targetSite = new Site("targetSite", targetTheme, dummyScheme);

    SiteSet siteSet = new SiteSet(ImmutableList.of(homeSite, targetSite));
    Site resolved = homeTheme.resolveForeignJournalKey(siteSet, "targetJournal");
    assertEquals(resolved, targetSite);
  }

}
