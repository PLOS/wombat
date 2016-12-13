package org.ambraproject.wombat.config.theme;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.site.url.SiteRequestScheme;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertEquals;

public class ThemeTest {

  @DataProvider
  public Object[][] booleans() {
    return new Object[][]{{false}, {true}};
  }

  @Test(dataProvider = "booleans")
  public void testResolveForeignJournalKey(final boolean useJournalKeyMap) throws Exception {
    SiteRequestScheme dummyScheme = SiteRequestScheme.builder().build();

    Theme homeTheme = new SimpleStubTheme("homeTheme", "homeJournal") {
      @Override
      protected Map<String, Object> getJournalConfigMap() {
        Map<String, Object> map = super.getJournalConfigMap();
        if (useJournalKeyMap) {
          ImmutableMap<String, String> otherJournals = ImmutableMap.of("targetJournal", "targetSite");
          map.put("otherJournals", otherJournals);
        }
        return map;
      }
    };
    Site homeSite = new Site("homeSite", homeTheme, dummyScheme);

    Theme targetTheme = new SimpleStubTheme("targetTheme", "targetJournal");
    Site targetSite = new Site("targetSite", targetTheme, dummyScheme);

    SiteSet siteSet = new SiteSet(ImmutableList.of(homeSite, targetSite));
    Site resolved = homeTheme.resolveForeignJournalKey(siteSet, "targetJournal");
    assertEquals(resolved, targetSite);
  }

}
