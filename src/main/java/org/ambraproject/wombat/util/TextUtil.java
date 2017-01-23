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

import com.google.common.base.CharMatcher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtil {

  /**
   * Removes the footnote marker from a given text
   * Footnote Marker Example:
   * ¤b Current address: Joint Genome Institute, Walnut Creek, California, United States of America
   * footnote marker would be "¤b" and that text will be replaced with an empty string.
   *
   * @param text with footnote marker
   * @return text without footnote marker
   */
  public static String removeFootnoteMarker(final String text) {
    // \b word boundary doesn't work because of the ¤ character
    return text.replaceAll("(?<=(^|\\s))¤\\w+(?=($|\\s))", "");
  }

  /*
   * Matches an opening or closing tag in XML, HTML, etc. This quick and dirty solution looks only for
   * the closing '>' character. It may get tripped up by edge cases, such as a '>' appearing inside an attribute.
   * Consider replacing with a proper XML parser if there's trouble.
   */
  private static final Pattern TAG_PATTERN = Pattern.compile("</?\\w.*?>");

  /**
   * Remove all XML or HTML markup tags and return the unadorned text. Collapses free whitespace in text.
   *
   * @param code markup code
   * @return the text with no markup
   */
  public static String removeMarkup(String code) {
    // Remove internal markup
    Matcher tagMatcher = TAG_PATTERN.matcher(code);
    String text = tagMatcher.replaceAll("");

    // Convert free whitespace to human-friendly.
    text = sanitizeWhitespace(text);

    return text;
  }

  /**
   * Remove leading and trailing whitespace, and collapse all other whitespace into single spaces. Given marked-up text
   * (such as from XML or HTML), this condenses indentations and line breaks into a human-readable form, as a web
   * browser would show it.
   *
   * @param text some text
   * @return the same text with whitespace trimmed and collapsed
   */
  public static String sanitizeWhitespace(String text) {
    return CharMatcher.WHITESPACE.trimAndCollapseFrom(text, ' ');
  }

}
