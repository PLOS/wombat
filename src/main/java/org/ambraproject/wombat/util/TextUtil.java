package org.ambraproject.wombat.util;

import org.w3c.dom.Node;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

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

  /**
   * Convert a parsed DOM node to equivalent XML source code.
   * <p/>
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

}
