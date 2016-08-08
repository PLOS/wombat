package org.ambraproject.wombat.service;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
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

public class XmlUtil {

  private static Document createXmlDocument(InputSource xmlSource) throws IOException {
    Document document;
    DocumentBuilder documentBuilder; // not thread-safe
    try {
      documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      throw new RuntimeException(e); // using default configuration; should be impossible
    }

    try {
      document = documentBuilder.parse(xmlSource);
    } catch (SAXException e) {
      throw new RuntimeException("Invalid XML syntax during document creation", e);
    }
    return document;
  }

  public static String extractElement(String xml, String tagName) throws IOException {
    return createXmlString(extractElement(createXmlDocument(new InputSource(xml)), tagName));
  }

  public static String extractElement(InputStream xmlStream, String tagName) throws IOException {
    try {
      return createXmlString(extractElement(createXmlDocument(new InputSource(xmlStream)), tagName));
    } finally {
      xmlStream.close();
    }
  }

  public static String removeElement(String xml, String tagName) throws IOException {
    return createXmlString(removeElement(createXmlDocument(new InputSource(xml)), tagName));
  }

  public static String removeElement(InputStream xmlStream, String tagName) throws IOException {
    try {
      return createXmlString(removeElement(createXmlDocument(new InputSource(xmlStream)), tagName));
    } finally {
      xmlStream.close();
    }
  }

  private static String createXmlString(Node node) {
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

  private static Element extractElement(Document xmlDoc, String tagName) {
    return (Element) xmlDoc.getElementsByTagName(tagName).item(0);
  }

  private static Document removeElement(Document xmlDoc, String tagName) {
    Element element = extractElement(xmlDoc, tagName);
    element.getParentNode().removeChild(element);
    xmlDoc.normalize();
    return xmlDoc;
  }

}

