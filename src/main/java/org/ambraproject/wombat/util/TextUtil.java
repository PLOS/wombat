package org.ambraproject.wombat.util;

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
    String replaced = text.replaceAll("(?<=(^|\\s))¤\\w+(?=($|\\s))", "");

    return replaced;
  }
}
