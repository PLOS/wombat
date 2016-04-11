package org.ambraproject.wombat.util;

import javax.xml.bind.DatatypeConverter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class CalendarUtil {

  private CalendarUtil() {
    throw new AssertionError("Not instantiable");
  }

  private static final TimeZone GMT = TimeZone.getTimeZone("GMT");

  /**
   * Formats a date string expressed in the ISO 8601 format.
   *
   * @param date ISO 8601 formatted date string
   * @param format format string to output
   * @param interpretDateAsLocalTime if true, the timezone in the date string input will be ignored,
   *     and the local server's timezone used instead.  This is a dangerous hack, but is necessary
   *     to work around a current problem in our solr indexing where publication dates are expressed
   *     as UTC, when they should be in local time.
   * @return
   */
  public static String formatIso8601Date(String date, String format, boolean interpretDateAsLocalTime) {
    Calendar calendar = DatatypeConverter.parseDateTime(date);
    if (interpretDateAsLocalTime) {
      calendar.setTimeZone(TimeZone.getDefault());
    }  // Else calendar will be set with tz in the ISO-8601 date string, which is usually UTC

    TimeZone timeZone = calendar.getTimeZone();
    if ("GMT+00:00".equals(timeZone.getID())) {
      timeZone = GMT; // kludge for formatting (remove the "+00:00" offset)
    }

    SimpleDateFormat formatObj = new SimpleDateFormat(format);
    formatObj.setTimeZone(timeZone);
    return formatObj.format(calendar.getTime());
  }

}
