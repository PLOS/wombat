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

  @Test
  public void testSanitizeWhitespace() {
    Assert.assertEquals(TextUtil.sanitizeWhitespace(""), "");
    Assert.assertEquals(TextUtil.sanitizeWhitespace("foo"), "foo");
    Assert.assertEquals(TextUtil.sanitizeWhitespace("foo bar"), "foo bar");
    Assert.assertEquals(TextUtil.sanitizeWhitespace(" foo bar"), "foo bar");
    Assert.assertEquals(TextUtil.sanitizeWhitespace("foo bar "), "foo bar");
    Assert.assertEquals(TextUtil.sanitizeWhitespace("foo  bar"), "foo bar");
    Assert.assertEquals(TextUtil.sanitizeWhitespace("   foo  bar   "), "foo bar");
    Assert.assertEquals(TextUtil.sanitizeWhitespace("\n\r foo\tbar \n \n "), "foo bar");
    Assert.assertEquals(TextUtil.sanitizeWhitespace(
            " A Phase Two Randomised Controlled Double Blind Trial of High Dose\n" +
                "                    Intravenous Methylprednisolone and Oral Prednisolone versus Intravenous Normal\n" +
                "                    Saline and Oral Prednisolone in Individuals with Leprosy Type 1 Reactions and/or\n" +
                "                    Nerve Function Impairment. "),
        "A Phase Two Randomised Controlled Double Blind Trial of High Dose Intravenous Methylprednisolone and Oral " +
            "Prednisolone versus Intravenous Normal Saline and Oral Prednisolone in Individuals with Leprosy Type 1 " +
            "Reactions and/or Nerve Function Impairment.");
  }

}
