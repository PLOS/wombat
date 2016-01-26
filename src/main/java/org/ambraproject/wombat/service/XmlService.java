package org.ambraproject.wombat.service;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.IOException;
import java.io.InputStream;

/**
 *  Used for creation, conversion and modification of XML documents and fragments
 */
public interface XmlService {

  /**
   * Convert a character input stream into a parsed XML document
   *
   * @param stream Input stream to convert to XML Document
   * @return an XML Document
   * @throws IOException
   */
  public Document createXmlDocument(InputStream xmlStream) throws IOException;

  /**
   * Convert a parsed DOM node to equivalent XML source code string.
   *
   * @param node the node to convert
   * @return XML source code string
   */
  public String createXmlString(Node node);

  /**
   * Return the first occurring instance of the named tag and its contents
   *
   * @param xmlString XML code fragment
   * @param tagName tag to be isolated
   * @return XML source code fragment
   * @throws IOException
   */
  String extractElement(String xmlString, String tagName) throws IOException;

  /**
   * Return the first occurring instance of the named tag and its contents
   *
   * @param xmlStream XML code fragment character stream
   * @param tagName tag to be isolated
   * @return XML source code fragment
   * @throws IOException
   */
  String extractElement(InputStream xmlStream, String tagName) throws IOException;

  /**
   * Remove the first occurring instance of the named tag and return the remaining code
   *
   * @param xmlString XML code fragment
   * @param tagName tag to be removed
   * @return remaining XML source code fragment
   * @throws IOException
   */
  String removeElement(String xmlString, String tagName) throws IOException;

  /**
   * Remove the first occurring instance of the named tag and return the remaining code
   *
   * @param xmlStream XML code fragment character stream
   * @param tagName tag to be removed
   * @return remaining XML source code fragment
   * @throws IOException
   */
  String removeElement(InputStream xmlStream, String tagName) throws IOException;

}
