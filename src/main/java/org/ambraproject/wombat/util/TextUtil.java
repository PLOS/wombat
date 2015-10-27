package org.ambraproject.wombat.util;

import com.google.common.base.CharMatcher;
import org.w3c.dom.Node;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtil {

  /**
   * Removes the footnote marker from a given text Footnote Marker Example: ¤b Current address: Joint Genome Institute,
   * Walnut Creek, California, United States of America footnote marker would be "¤b" and that text will be replaced
   * with an empty string.
   *
   * @param text with footnote marker
   * @return text without footnote marker
   */
  public static String removeFootnoteMarker(final String text) {
    // \b word boundary doesn't work because of the ¤ character
    return text.replaceAll("(?<=(^|\\s))¤\\w+(?=($|\\s))", "");
  }

  /**
   * Convert a parsed DOM node to equivalent XML source code.
   * <p>
   * TODO: Unify with {@link org.ambraproject.wombat.service.ArticleTransformService#transformExcerpt}?
   *
   * @param node the node to convert
   * @return XML code
   */
  public static String recoverXml(Node node) {
    Transformer transformer;
    try {
      transformer = TransformerFactory.newInstance().newTransformer(); // not thread-safe
    } catch (TransformerConfigurationException e) {
      throw new RuntimeException(e); // Should be impossible; we are using default configuration
    }
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    StringWriter xml = new StringWriter();
    try {
      transformer.transform(new DOMSource(node), new StreamResult(xml));
    } catch (TransformerException e) {
      throw new RuntimeException(e);
    }
    return xml.toString();
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
