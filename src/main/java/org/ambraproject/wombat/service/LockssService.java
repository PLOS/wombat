package org.ambraproject.wombat.service;

import java.util.Arrays;
import java.util.Calendar;

public class LockssService {

  private static final String MONTHS[] = {"January", "February", "March", "April", "May", "June", "July",
      "September", "October", "November", "December"};

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
}
