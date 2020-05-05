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
import org.ambraproject.wombat.service.remote.SolrSearchApi;
import org.ambraproject.wombat.service.remote.SolrSearchApiImpl;
import org.ambraproject.wombat.service.remote.SolrSearchApiImpl.SolrEnumeratedDateRange;
import org.ambraproject.wombat.service.remote.SolrSearchApiImpl.SolrSortOrder;
import org.apache.commons.lang3.Range;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import static org.ambraproject.wombat.service.remote.SolrSearchApi.MAXIMUM_SOLR_RESULT_COUNT;

public class ArticleArchiveServiceImpl implements ArticleArchiveService {

  private static final ImmutableList<String> MONTHS = ImmutableList.copyOf(new DateFormatSymbols().getMonths());

  @Autowired
  SolrSearchApiImpl solrSearchApi;
  
  @Override
  public Range<Date> getDatesForJournal(Site site) throws IOException, ParseException {
    ArticleSearchQuery query = ArticleSearchQuery.builder()
      .setRows(0)
      .setStatsField("publication_date")
      .setSortOrder(SolrSortOrder.RELEVANCE)
      .setDateRange(SolrEnumeratedDateRange.ALL_TIME)
      .setJournalKeys(ImmutableList.of(site.getJournalKey()))
      .build();
    SolrSearchApi.Result result = solrSearchApi.search(query);
    Date minDate = result.getPublicationDateStats().get().getMin();
    Date maxDate = result.getPublicationDateStats().get().getMax();
    return Range.between(minDate, maxDate);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ImmutableList<String> getMonthsForYear(int requestedYear) {
    int currentYear = Calendar.getInstance().get(Calendar.YEAR);
    if (requestedYear < currentYear) {
      return MONTHS;
    } else if (requestedYear == currentYear) {
      // Months are 0-based on Calendar
      int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
      return MONTHS.subList(0, currentMonth + 1);
    }
    return ImmutableList.of();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SolrSearchApi.Result getArticleDoisPerMonth(Site site, String year, String month,
                                                     String cursor) throws IOException, ParseException {
    Calendar startDate = Calendar.getInstance();
    startDate.setTime(new SimpleDateFormat("MMMM").parse(month));
    startDate.set(Calendar.YEAR, Integer.parseInt(year));
    startDate.set(Calendar.DAY_OF_MONTH, 1);

    Calendar endDate = (Calendar) startDate.clone();
    endDate.add(Calendar.MONTH, 1);

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    SolrSearchApiImpl.SolrExplicitDateRange dateRange = new SolrSearchApiImpl.SolrExplicitDateRange
        ("Monthly Search", dateFormat.format(startDate.getTime()), dateFormat.format(endDate.getTime()));

    ArticleSearchQuery query = ArticleSearchQuery.builder()
      .setJournalKeys(ImmutableList.of(site.getJournalKey()))
      .setRows(MAXIMUM_SOLR_RESULT_COUNT)
      .setSortOrder(SolrSearchApiImpl.SolrSortOrder.DATE_OLDEST_FIRST)
      .setDateRange(dateRange)
      .setCursor(cursor)
      .build();
    return solrSearchApi.search(query);
  }
}
