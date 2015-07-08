package org.ambraproject.wombat.service;

import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.service.remote.SolrSearchService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class LockssServiceImpl implements LockssService {

  private static final String MONTHS[] = new DateFormatSymbols().getMonths();

  @Autowired
  SolrSearchService solrSearchService;

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
  public String[] getArticleDoisPerMonth(Site site, String year, String month) throws IOException {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    Calendar startDate = Calendar.getInstance();
    startDate.set(Integer.parseInt(year), Arrays.asList(MONTHS).indexOf(month), 1);

    Calendar endDate = (Calendar) startDate.clone();
    endDate.add(Calendar.MONTH, 1);

    SolrSearchService.SolrExplicitDateRange dateRange = new SolrSearchService.SolrExplicitDateRange
        ("Monthly Search", dateFormat.format(startDate.getTime()), dateFormat.format(endDate.getTime()));

    Map searchResult = solrSearchService.simpleSearch("", site, 0, 1000000,
        SolrSearchService.SolrSortOrder
            .DATE_OLDEST_FIRST, dateRange);

    List docs = (List) searchResult.get("docs");

    String dois[] = new String[docs.size()];

    for (int i = 0; i < docs.size(); i++) {
      Map<?,?> doc = (Map) docs.get(i);
      dois[i] = (String) doc.get("id");
    }
    return dois;
  }
}
