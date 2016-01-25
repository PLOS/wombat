package org.ambraproject.wombat.service;

import com.google.common.base.Charsets;
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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;

public class XmlServiceImpl implements XmlService {

  static final Charset XML_CHARSET = Charsets.UTF_8;

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

  public String createXmlString(Node node) {
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

  @Override
  public String extractElement(String xmlString, String tagName) throws IOException {
    InputStream xmlStream = new ByteArrayInputStream(xmlString.getBytes(XML_CHARSET));
    return createXmlString(extractElement(createXmlDocument(xmlStream), tagName));
  }

  @Override
  public String extractElement(InputStream xmlStream, String tagName) throws IOException {
    return createXmlString(extractElement(createXmlDocument(xmlStream), tagName));
  }

  @Override
  public String removeElement(String xmlString, String tagName) throws IOException {
    InputStream xmlStream = new ByteArrayInputStream(xmlString.getBytes(XML_CHARSET));
    return createXmlString(removeElement(createXmlDocument(xmlStream), tagName));
  }

  @Override
  public String removeElement(InputStream xmlStream, String tagName) throws IOException {
    return createXmlString(removeElement(createXmlDocument(xmlStream), tagName));
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

}

