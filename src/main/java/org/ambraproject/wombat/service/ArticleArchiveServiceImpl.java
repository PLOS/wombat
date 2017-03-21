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
import org.ambraproject.wombat.service.remote.ArticleSearchQuery;
import org.ambraproject.wombat.service.remote.SolrSearchApiImpl;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;

public class ArticleArchiveServiceImpl implements ArticleArchiveService {

  private static final ImmutableList<String> MONTHS = ImmutableList.copyOf(new DateFormatSymbols().getMonths());

  @Autowired
  SolrSearchApiImpl solrSearchApi;

  @Override
  public Map<?, ?> getYearsForJournal(Site site) throws IOException, ParseException {
    Map<String, String> yearRange = (Map<String, String>) solrSearchApi.getStats("publication_date",
        site.getJournalKey());
    return yearRange;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ImmutableList<String> getMonthsForYear(String requestedYear) {
    int currentYear = Calendar.getInstance().get(Calendar.YEAR);
    if (Integer.parseInt(requestedYear) < currentYear) {
      return MONTHS;
    } else if (Integer.parseInt(requestedYear) == currentYear) {
      // Months are 0-based on Calendar
      int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
      return MONTHS.subList(0, currentMonth + 1);
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<?, ?> getArticleDoisPerMonth(Site site, String year, String month) throws IOException {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    Calendar startDate = Calendar.getInstance();
    startDate.set(Integer.parseInt(year), Arrays.asList(MONTHS).indexOf(month), 1);

    Calendar endDate = (Calendar) startDate.clone();
    endDate.add(Calendar.MONTH, 1);

    SolrSearchApiImpl.SolrExplicitDateRange dateRange = new SolrSearchApiImpl.SolrExplicitDateRange
        ("Monthly Search", dateFormat.format(startDate.getTime()), dateFormat.format(endDate.getTime()));

    ArticleSearchQuery.Builder query = ArticleSearchQuery.builder()
        .setJournalKeys(Collections.singletonList(site.getJournalKey()))
        .setStart(0)
        .setRows(1000000)
        .setSortOrder(SolrSearchApiImpl.SolrSortOrder.DATE_OLDEST_FIRST)
        .setDateRange(dateRange);
    Map<String, Map> searchResult = (Map<String, Map>) solrSearchApi.search(query.build());
    return searchResult;
  }
}
