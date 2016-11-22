package org.ambraproject.wombat.util;

import org.ambraproject.wombat.config.site.JournalSite;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.service.UnmatchedSiteException;

public class MockSiteUtil {
  private MockSiteUtil() {
    throw new AssertionError("Not instantiable");
  }

  /**
   * Find the unique site that has a given journal key.
   * <p/>
   * This is ONLY A TESTING UTILITY because sites in general do not have unique journal keys. If you need to dereference
   * a site by journal key, use {@link org.ambraproject.wombat.config.theme.Theme#resolveForeignJournalKey}. The context
   * of a theme is needed, e.g., to distinguish between the desktop or mobile sites with that key. To get a journal
   * <em>name</em> (which, unlike the rest of the site object, should be globally unique per journal key) with no theme
   * context, use {@link SiteSet#getJournalNameFromKey}.
   *
   * @param siteSet
   * @param journalKey
   * @return
   */
  public static Site getByUniqueJournalKey(SiteSet siteSet, String journalKey) {
    Site matched = null;
    for (Site candidate : siteSet.getSites()) {
      if ((candidate instanceof JournalSite) && ((JournalSite) candidate).getJournalKey().equals(journalKey)) {
        if (matched == null) {
          matched = candidate;
        } else {
          String message = String.format("Journal key (%s) was not unique: %s; %s",
              journalKey, matched.getKey(), candidate.getKey());
          throw new IllegalArgumentException(message);
        }
      }
    }
    if (matched == null) {
      throw new UnmatchedSiteException("Not found: " + journalKey);
    }
    return matched;
  }

}
