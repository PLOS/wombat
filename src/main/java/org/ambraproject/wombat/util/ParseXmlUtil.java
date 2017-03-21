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
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;

public class ParseXmlUtil {
  private static final Logger log = LoggerFactory.getLogger(ParseXmlUtil.class);

  public static String getElementSingleValue(Element element, String name) {
    NodeList node = element.getElementsByTagName(name);
    if (node == null || node.getLength() == 0) {
      return null;
    }
    Node firstChild = node.item(0).getFirstChild();
    if (firstChild == null || firstChild.getNodeType() != Node.TEXT_NODE) {
      return null;
    }
    return firstChild.getNodeValue();
  }

  public static String getElementAttributeValue(Element element, String attributeName) {
    return element == null ? null :element.getAttribute(attributeName);
  }


  /**
   * Build a text field by partially reconstructing the node's content as XML. The output is text content between the
   * node's two tags, including nested XML tags with attributes, but not this node's outer tags. Continuous substrings
   * of whitespace may be substituted with other whitespace. Markup characters are escaped.
   * <p/>
   * This method is used instead of an appropriate XML library in order to match the behavior of legacy code, for now.
   *
   * @param node the node containing the text we are retrieving
   * @return the marked-up node contents
   */
  public static StringBuilder getElementMixedContent(StringBuilder mixedContent, Node node) {
    List<Node> children = NodeListAdapter.wrap(node.getChildNodes());
    for (Node child : children) {
      switch (child.getNodeType()) {
        case Node.TEXT_NODE:
          appendTextNode(mixedContent, child);
          break;
        case Node.ELEMENT_NODE:
          appendElementNode(mixedContent, child);
          break;
        default:
          log.warn("Skipping node (name={}, type={})", child.getNodeName(), child.getNodeType());
      }
    }
    return mixedContent;
  }

  private static void appendTextNode(StringBuilder nodeContent, Node child) {
    String text = child.getNodeValue();
    text = StringEscapeUtils.escapeXml11(text);
    nodeContent.append(text);
  }

  private static void appendElementNode(StringBuilder mixedContent, Node child) {
    String nodeName = child.getNodeName();
    mixedContent.append('<').append(nodeName);
    List<Node> attributes = NodeListAdapter.wrap(child.getAttributes());

    // Search for xlink attributes and declare the xlink namespace if found
    // TODO Better way? This is probably a symptom of needing to use a proper XML library here in the first place.
    for (Node attribute : attributes) {
      if (attribute.getNodeName().startsWith("xlink:")) {
        mixedContent.append(" xmlns:xlink=\"http://www.w3.org/1999/xlink\"");
        break;
      }
    }

    for (Node attribute : attributes) {
      mixedContent.append(' ').append(attribute.toString());
    }

    mixedContent.append('>');
    getElementMixedContent(mixedContent, child);
    mixedContent.append("</").append(nodeName).append('>');
  }

  public static String standardizeWhitespace(CharSequence text) {
    return (text == null) ? null : CharMatcher.whitespace().trimAndCollapseFrom(text, ' ');
  }

  public static boolean isNullOrEmpty(NodeList nodeList){
    return nodeList == null || nodeList.getLength() == 0;
  }

  public static boolean isNullOrEmpty(List<Node> nodeList){
    return nodeList == null || nodeList.size() == 0;
  }


  }
