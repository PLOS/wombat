package org.ambraproject.wombat.service;

import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.service.remote.SolrSearchService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArticleArchiveServiceImpl implements ArticleArchiveService {

  private static final String MONTHS[] = new DateFormatSymbols().getMonths();

  @Autowired
  SolrSearchService solrSearchService;

  @Override
  public int[] getYearsForJournal(Site site) throws IOException, ParseException {
    Map<String, String> rawQueryParams = new HashMap();
    rawQueryParams.put("stats", "true");
    rawQueryParams.put("stats.field", "publication_date");

    Map<String, Map> rawResult = (Map<String, Map>) solrSearchService.simpleSearch("", site, 0, 0,
        SolrSearchService.SolrSortOrder .RELEVANCE, SolrSearchService.SolrDateRange.ALL_TIME,rawQueryParams);

    Map<String, Map> statsField = (Map<String, Map>) rawResult.get("stats").get("stats_fields");
    Map<String, String> publicationDate = (Map<String, String>) statsField.get("publication_date");

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    Calendar date = Calendar.getInstance();
    date.setTime(dateFormat.parse(publicationDate.get("min")));
    int minYear = date.get(Calendar.YEAR);
    date.setTime(dateFormat.parse(publicationDate.get("max")));
    int maxYear = date.get(Calendar.YEAR);

    int[] yearRange = {minYear, maxYear};

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
  public String[] getArticleDoisPerMonth(Site site, String year, String month) throws IOException {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    Calendar startDate = Calendar.getInstance();
    startDate.set(Integer.parseInt(year), Arrays.asList(MONTHS).indexOf(month), 1);

    Calendar endDate = (Calendar) startDate.clone();
    endDate.add(Calendar.MONTH, 1);

    SolrSearchService.SolrExplicitDateRange dateRange = new SolrSearchService.SolrExplicitDateRange
        ("Monthly Search", dateFormat.format(startDate.getTime()), dateFormat.format(endDate.getTime()));

    Map searchResult = solrSearchService.simpleSearch("", site, 0, 1000000,
        SolrSearchService.SolrSortOrder.DATE_OLDEST_FIRST, dateRange);

    List docs = (List) searchResult.get("docs");

    String dois[] = new String[docs.size()];

    for (int i = 0; i < docs.size(); i++) {
      Map<?,?> doc = (Map) docs.get(i);
      dois[i] = (String) doc.get("id");
    }
    return dois;
  }
}
