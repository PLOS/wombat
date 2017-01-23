/*
 * Copyright (c) 2017 Public Library of Science
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

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
