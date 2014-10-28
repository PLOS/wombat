package org.ambraproject.wombat.util;

import javax.xml.bind.DatatypeConverter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class CalendarUtil {
  private CalendarUtil() {
    throw new AssertionError("Not instantiable");
  }

  public static String formatIso8601Date(String date, String format) {
    Calendar calendar = DatatypeConverter.parseDateTime(date);
    calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
    return new SimpleDateFormat(format).format(calendar.getTime());
  }

}
