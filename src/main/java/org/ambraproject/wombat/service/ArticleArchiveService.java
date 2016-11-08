package org.ambraproject.wombat.service;

import org.ambraproject.wombat.config.site.JournalSite;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

/**
 * Responsible for returning the DOIs of article published in a given year and month
 */
public interface ArticleArchiveService {

  /**
   * Returns the publication year range for a given journal
   *
   * @param site specifies the name of the journal
   * @return a Map of stats values for publication date
   * @throws IOException
   * @throws ParseException
   */
  public abstract Map<?, ?> getYearsForJournal(JournalSite site) throws IOException, ParseException;

  /**
   * Returns all of the months for the requested year. If it's the current year,
   * it will return the months until the current month.
   *
   * @param requestedYear the year for which the months are listed
   * @return list of months
   */
  public abstract String[] getMonthsForYear(String requestedYear);

  /**
   * Returns all of the articles published for a given year and month per journal.
   *
   * @param site specifies the name of the journal
   * @param year specifies the year
   * @param month specifies the month
   *
   * @return list of articles published in a given year and month in a journal
   * @throws IOException
   */
  public abstract Map<?, ?> getArticleDoisPerMonth(JournalSite site, String year, String month) throws IOException;

}
