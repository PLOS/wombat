package org.ambraproject.wombat.util;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TextUtilTest {

  @Test
  public void testRemoveFootnoteMarker() {
    String expectedResult = " Current address: Joint Genome Institute, Walnut Creek, California, United States of America";
    String actualResult = TextUtil.removeFootnoteMarker("¤b Current address: Joint Genome Institute, Walnut Creek, California, United States of America");
    Assert.assertEquals(actualResult, expectedResult, "Footnote marker was not replaced");

    expectedResult = "Current  Current address: Joint Genome Institute, Walnut Creek, California, United States of America";
    actualResult = TextUtil.removeFootnoteMarker("Current ¤b Current address: Joint Genome Institute, Walnut Creek, California, United States of America");
    Assert.assertEquals(actualResult, expectedResult, "Footnote marker was not replaced");

  }
}
