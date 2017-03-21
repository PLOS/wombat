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

package org.ambraproject.wombat.util;

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
      if (candidate.getJournalKey().equals(journalKey)) {
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
