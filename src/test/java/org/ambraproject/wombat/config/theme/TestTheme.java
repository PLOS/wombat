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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.site.url.SiteRequestScheme;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertEquals;

public class TestTheme {

  @DataProvider
  public Object[][] booleans() {
    return new Object[][]{{false}, {true}};
  }

  @Test(dataProvider = "booleans")
  public void testResolveForeignJournalKey(final boolean useJournalKeyMap) throws Exception {
    SiteRequestScheme dummyScheme = SiteRequestScheme.builder().build();

    Theme homeTheme = new StubTheme("homeTheme", "homeJournal") {
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

    Theme targetTheme = new StubTheme("targetTheme", "targetJournal");
    Site targetSite = new Site("targetSite", targetTheme, dummyScheme);

    SiteSet siteSet = new SiteSet(ImmutableList.of(homeSite, targetSite));
    Site resolved = homeTheme.resolveForeignJournalKey(siteSet, "targetJournal");
    assertEquals(resolved, targetSite);
  }

}
