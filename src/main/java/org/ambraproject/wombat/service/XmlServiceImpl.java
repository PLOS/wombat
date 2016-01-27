package org.ambraproject.wombat.service;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

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
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;

public class XmlServiceImpl implements XmlService {

  private static final Charset XML_CHARSET = Charsets.UTF_8;
  private static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
  private static final String XML_ROOT_OPEN = "<root>";
  private static final String XML_ROOT_CLOSE = "</root>";

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
    InputStream xmlStream = new ByteArrayInputStream(wrapWithXmlRoot(xmlString).getBytes(XML_CHARSET));
    return createXmlString(extractElement(createXmlDocument(xmlStream), tagName));
  }

  @Override
  public String extractElement(InputStream xmlStream, String tagName) throws IOException {
    return createXmlString(extractElement(createXmlDocument(xmlStream), tagName));
  }

  @Override
  public String removeElementFromFragment(String xmlString, String tagName) throws IOException {
    InputStream xmlStream = new ByteArrayInputStream(wrapWithXmlRoot(xmlString).getBytes(XML_CHARSET));
    return createXmlStringFromWrapped(removeElement(createXmlDocument(xmlStream), tagName));
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

  private String createXmlStringFromWrapped(Node node) {
    String wrappedXmlFragment = createXmlString(node);
    return unwrapXmlRoot(wrappedXmlFragment);
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

  private String wrapWithXmlRoot(String xmlFragment) {
    return  XML_DECLARATION + XML_ROOT_OPEN + xmlFragment + XML_ROOT_CLOSE;
  }

  private String unwrapXmlRoot(String wrappedXmlFragment) {
    return wrappedXmlFragment.substring(XML_ROOT_OPEN.length(), wrappedXmlFragment.length() - XML_ROOT_CLOSE.length());
  }

}

