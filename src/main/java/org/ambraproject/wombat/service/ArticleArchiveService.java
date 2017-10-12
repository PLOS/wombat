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

package org.ambraproject.wombat.service;

import com.google.common.collect.ImmutableList;
import org.ambraproject.wombat.config.site.Site;

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
  public abstract Map<?, ?> getYearsForJournal(Site site) throws IOException, ParseException;

  /**
   * Returns all of the months for the requested year. If it's the current year,
   * it will return the months until the current month.
   *
   * @param requestedYear the year for which the months are listed
   * @return list of months
   */
  public abstract ImmutableList<String> getMonthsForYear(int requestedYear);

  /**
   * Returns all of the articles published for a given year and month per journal.
   *
   * @param site specifies the name of the journal
   * @param year specifies the year
   * @param month specifies the month
   * @param cursor cursor mark provided for paging solr
   *
   * @return list of articles published in a given year and month in a journal
   * @throws IOException
   */
  public abstract Map<?, ?> getArticleDoisPerMonth(Site site, String year, String month,
                                                   String cursor) throws IOException, ParseException;

}
