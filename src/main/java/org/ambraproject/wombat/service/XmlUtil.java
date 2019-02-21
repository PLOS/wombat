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

package org.ambraproject.wombat.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.CharEncoding;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Entities;
import org.jsoup.parser.Parser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import static java.lang.String.format;

public class XmlUtil {

  /**
   * Construct a non-validating document builder. We assume that we don't want to connect to remote servers to validate
   * except with a specific reason.
   *
   * @return a new document builder
   */
  static DocumentBuilder newDocumentBuilder() {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    // at a minimum the document builder needs to be namespace aware
    factory.setNamespaceAware(true);
    factory.setValidating(false);
    try {
      factory.setFeature("http://xml.org/sax/features/validation", false);
      factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
      factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      return factory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

  public static Document createXmlDocument(InputSource xmlSource) throws IOException {
    try {
      return newDocumentBuilder().parse(xmlSource);
    } catch (SAXException e) {
      throw new RuntimeException("Invalid XML syntax during document creation", e);
    }
  }

  public static String extractText(String xml) {
    InputSource xmlSource = new InputSource(new StringReader(xml));
    Node rootNode;
    try {
      rootNode = createXmlDocument(xmlSource).getFirstChild();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return rootNode.getTextContent().trim();
  }

  public static String extractElement(String xml, String tagName) throws IOException {
    InputSource xmlSource = new InputSource(new StringReader(xml));
    return createXmlString(extractElement(createXmlDocument(xmlSource), tagName));
  }

  public static String extractElement(String xml, String tagName, String attrName, String attrValue) throws IOException {
    InputSource xmlSource = new InputSource(new StringReader(xml));
    return createXmlString(extractElement(createXmlDocument(xmlSource), tagName, attrName, attrValue));
  }

  public static String extractElement(InputStream xmlStream, String tagName) throws IOException {
    try {
      return createXmlString(extractElement(createXmlDocument(new InputSource(xmlStream)), tagName));
    } finally {
      xmlStream.close();
    }
  }

  public static String removeElement(String xml, String tagName) throws IOException {
    InputSource xmlSource = new InputSource(new StringReader(xml));
    return createXmlString(removeElement(createXmlDocument(xmlSource), tagName));
  }

  public static String removeElement(InputStream xmlStream, String tagName) throws IOException {
    try {
      return createXmlString(removeElement(createXmlDocument(new InputSource(xmlStream)), tagName));
    } finally {
      xmlStream.close();
    }
  }

  public static NodeList getNodes(Document doc, String xpath) {
    XPath xPath = XPathFactory.newInstance().newXPath();
    try {
      return (NodeList) xPath.compile(xpath).evaluate(doc, XPathConstants.NODESET);
    } catch (XPathExpressionException e) {
      throw new RuntimeException(e);
    }
  }


  public static String createXmlString(Node node) {
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

  private static Element extractElement(Document xmlDoc, String tagName, String attrName, String attrValue) {
    try {
      XPath xpath = XPathFactory.newInstance().newXPath();
      XPathExpression expr = xpath.compile(format("//%s[@%s=\"%s\"]", tagName, attrName, attrValue));
      NodeList nl = (NodeList) expr.evaluate(xmlDoc, XPathConstants.NODESET);
      return (Element) nl.item(0);
    } catch (XPathExpressionException e) {
      throw new RuntimeException(e);
    }
  }
}

