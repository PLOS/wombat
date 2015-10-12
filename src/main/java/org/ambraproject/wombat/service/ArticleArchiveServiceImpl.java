package org.ambraproject.wombat.service;

import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.service.remote.ArticleSearchQuery;
import org.ambraproject.wombat.service.remote.SolrSearchServiceImpl;
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

  private static final String MONTHS[] = new DateFormatSymbols().getMonths();

  @Autowired
  SolrSearchServiceImpl solrSearchService;

  @Override
  public Map<?, ?> getYearsForJournal(Site site) throws IOException, ParseException {
    Map<String, String> yearRange = (Map<String, String>) solrSearchService.getStats("publication_date",
        site.getJournalKey());
    return yearRange;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String[] getMonthsForYear(String requestedYear) {
    int currentYear = Calendar.getInstance().get(Calendar.YEAR);
    if (Integer.parseInt(requestedYear) < currentYear) {
      return MONTHS;
    } else if (Integer.parseInt(requestedYear) == currentYear) {
      // Months are 0-based on Calendar
      int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
      String[] months = Arrays.copyOf(MONTHS, currentMonth + 1);
      return months;
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

    SolrSearchServiceImpl.SolrExplicitDateRange dateRange = new SolrSearchServiceImpl.SolrExplicitDateRange
        ("Monthly Search", dateFormat.format(startDate.getTime()), dateFormat.format(endDate.getTime()));

    ArticleSearchQuery.Builder query = ArticleSearchQuery.builder()
        .setJournalKeys(Collections.singletonList(site.getJournalKey()))
        .setStart(0)
        .setRows(1000000)
        .setSortOrder(SolrSearchServiceImpl.SolrSortOrder.DATE_OLDEST_FIRST)
        .setDateRange(dateRange);
    Map<String, Map> searchResult = (Map<String, Map>) solrSearchService.search(query.build());
    return searchResult;
  }
}
