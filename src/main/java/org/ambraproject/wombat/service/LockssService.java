package org.ambraproject.wombat.service;

import org.ambraproject.wombat.config.site.Site;

import java.io.IOException;

/**
 * Responsible for returning the DOIs of article published in a given year and month
 */
public interface LockssService {

  /**
   *
   * @param requestedYear
   * @return
   */
  public abstract String[] getMonthsForYear(String requestedYear);

  /**
   *
   * @param site
   * @param year
   * @param month
   * @return
   * @throws IOException
   */
  public abstract String[] getArticleDoisPerMonth(Site site, String year, String month) throws IOException;

}
