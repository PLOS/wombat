package org.ambraproject.wombat.service;

import org.ambraproject.wombat.config.site.Site;

import java.io.IOException;
import java.text.ParseException;

/**
 * Responsible for returning the DOIs of article published in a given year and month
 */
public interface ArticleArchiveService {

  public abstract int[] getYearForJournal(Site site) throws IOException, ParseException;

  /**
   * Returns all of the months for the requested year. If it's the current year,
   * it will return the months until the current month.
   *
   * @param requestedYear the year for which the months are listed
   * @return list of months
   */
  public abstract String[] getMonthsForYear(String requestedYear);

  /**
   * Returns all of the article DOIs published for a given year and month per journal.
   *
   * @param site specifies the name of the journal
   * @param year specifies the year
   * @param month specifies the month
   *
   * @return list of article DOIs published in a given year and month in a journal
   * @throws IOException
   */
  public abstract String[] getArticleDoisPerMonth(Site site, String year, String month) throws IOException;

}
