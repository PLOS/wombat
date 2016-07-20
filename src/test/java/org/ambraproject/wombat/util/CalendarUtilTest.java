package org.ambraproject.wombat.util;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class CalendarUtilTest {

  @Test
  public void testFormatIso8601Date() {
    assertEquals(CalendarUtil.formatIso8601Date("2015-01-12T23:59:00Z", "yyyy-MM-dd"), "2015-01-12");
    assertEquals(CalendarUtil.formatIso8601Date("2015-01-13T00:00:00Z", "yyyy-MM-dd"), "2015-01-13");

    // Try a day when daylight savings time was in effect.
    assertEquals(CalendarUtil.formatIso8601Date("2015-08-06T23:59:00Z", "yyyy-MM-dd"), "2015-08-06");
    assertEquals(CalendarUtil.formatIso8601Date("2015-08-07T00:00:00Z", "yyyy-MM-dd"), "2015-08-07");
  }
}
