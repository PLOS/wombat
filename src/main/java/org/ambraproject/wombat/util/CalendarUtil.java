package org.ambraproject.wombat.util;

import javax.xml.bind.DatatypeConverter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
   * @return
   */
  public static String formatIso8601Date(String date, String format) {
    Calendar calendar = DatatypeConverter.parseDateTime(date);

    TimeZone timeZone = calendar.getTimeZone();
    if ("GMT+00:00".equals(timeZone.getID())) {
      timeZone = GMT; // kludge for formatting (remove the "+00:00" offset)
    }

    SimpleDateFormat formatObj = new SimpleDateFormat(format);
    formatObj.setTimeZone(timeZone);
    return formatObj.format(calendar.getTime());
  }

}
