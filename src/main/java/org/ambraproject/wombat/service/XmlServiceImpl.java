package org.ambraproject.wombat.service;

import com.google.common.base.Charsets;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.function.Function;

public class XmlServiceImpl implements XmlService {

  private static final Charset XML_CHARSET = Charsets.UTF_8;
  private static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

  @Override
  public Document createXmlDocument(InputStream xmlStream) throws IOException {
    Document document;
    try {
      DocumentBuilder documentBuilder; // not thread-safe
      try {
        documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      } catch (ParserConfigurationException e) {
        throw new RuntimeException(e); // using default configuration; should be impossible
      }

//      BufferedReader br = null;
//      StringBuilder sb = new StringBuilder();
//
//      String line;
//      try {
//
//        br = new BufferedReader(new InputStreamReader(xmlStream));
//        while ((line = br.readLine()) != null) {
//          sb.append(line);
//        }
//
//      } catch (IOException e) {
//        e.printStackTrace();
//      } finally {
//        if (br != null) {
//          try {
//            br.close();
//          } catch (IOException e) {
//            e.printStackTrace();
//          }
//        }
//      }


      try {
        document = documentBuilder.parse(xmlStream);
      } catch (SAXException e) {
        throw new RuntimeException("Invalid XML syntax during document creation", e);
      }
    } finally {
      xmlStream.close();
    }
    return document;
  }

  @Override
  public String extractElementFromFragment(String xmlString, String tagName) throws IOException {
    WrappedXmlFragment fragment = new WrappedXmlFragment(xmlString);
    return createXmlString(extractElement(fragment.buildWrappedDocument(), tagName));
  }

  @Override
  public String extractElement(InputStream xmlStream, String tagName) throws IOException {
    return createXmlString(extractElement(createXmlDocument(xmlStream), tagName));
  }

  @Override
  public String removeElementFromFragment(String xmlString, String tagName) throws IOException {
    WrappedXmlFragment fragment = new WrappedXmlFragment(xmlString);
    return fragment.modifyAndUnwrap(document -> removeElement(document, tagName));
  }

  @Override
  public String removeElement(InputStream xmlStream, String tagName) throws IOException {
    return createXmlString(removeElement(createXmlDocument(xmlStream), tagName));
  }

  private String createXmlString(Node node) {
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

  private Element extractElement(Document xmlDoc, String tagName) {
    return (Element) xmlDoc.getElementsByTagName(tagName).item(0);
  }

  private Document removeElement(Document xmlDoc, String tagName) {
    Element element = extractElement(xmlDoc, tagName);
    element.getParentNode().removeChild(element);
    xmlDoc.normalize();
    return xmlDoc;
  }

  /**
   * A fragment of XML code that is wrapped in a fake element in order to form a pseudo-document.
   */
  private class WrappedXmlFragment {
    private final String fragmentXml;
    private final String openingTag;
    private final String closingTag;

    public WrappedXmlFragment(String fragmentXml) {
      this("root", fragmentXml);
    }

    public WrappedXmlFragment(String wrappingTagName, String fragmentXml) {
      this.fragmentXml = Objects.requireNonNull(fragmentXml);

      Objects.requireNonNull(wrappingTagName);
      openingTag = '<' + wrappingTagName + '>';
      closingTag = "</" + wrappingTagName + '>';
    }

    /**
     * @return a fake XML document made by wrapping the fragment in a new, root-level element
     */
    public Document buildWrappedDocument() throws IOException {
      String documentXml = XML_DECLARATION + openingTag + fragmentXml + closingTag;
      return createXmlDocument(IOUtils.toInputStream(documentXml, XML_CHARSET));
    }

    /**
     * Apply a modification to the document and return the wrapped fragment, with the same modification applied to it,
     * without its fake wrapping element.
     *
     * @param modification the operation to apply to the document
     * @return XML code representing the modified fragment
     */
    public String modifyAndUnwrap(Function<? super Document, ? extends Node> modification) throws IOException {
      Document document = buildWrappedDocument();
      Node modified = modification.apply(document);
      String modifiedString = createXmlString(modified);
      return removeAffixes(openingTag, modifiedString, closingTag);
    }
  }

  /**
   * Verify that a string has expected affixes and remove them.
   *
   * @param prefix the prefix to remove
   * @param text   a string that must have the given prefix and suffix
   * @param suffix the suffix to remove
   * @return the string with the prefix and suffix removed
   * @throws IllegalArgumentException if {@code text} does not begin with {@code prefix} or end with {@code suffix}
   */
  private static String removeAffixes(String prefix, String text, String suffix) {
    if (!prefix.equals(text.substring(0, prefix.length()))) {
      throw new IllegalArgumentException("Prefix does not match text");
    }
    if (!suffix.equals(text.substring(text.length() - suffix.length()))) {
      throw new IllegalArgumentException("Suffix does not match text");
    }
    return text.substring(prefix.length(), text.length() - suffix.length());
  }

}

