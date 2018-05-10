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

import org.ambraproject.wombat.model.Reference;
import org.ambraproject.wombat.util.NodeListAdapter;
import org.ambraproject.wombat.util.ParseXmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.management.modelmbean.XMLParseException;
import javax.xml.parsers.DocumentBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class ParseXmlServiceImpl implements ParseXmlService {

  private static final Logger log = LoggerFactory.getLogger(ParseXmlServiceImpl.class);

  @Autowired
  private ParseReferenceService parseReferenceService;

  /**
   * {@inheritDoc}
   */
  @Override
  public Document getDocument(InputStream xml) throws IOException {
    Objects.requireNonNull(xml);

    Document doc;
    try {
      DocumentBuilder builder = XmlUtil.newDocumentBuilder();
      doc = builder.parse(xml);
    } catch (SAXException e) {
      throw new RuntimeException(e);
    }

    // to combine adjacent text nodes and remove empty ones in the dom
    doc.getDocumentElement().normalize();
    return doc;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Reference> parseArticleReferences(Document doc,
                                                ParseReferenceService.DoiToJournalLinkService linkService)
      throws IOException {

    List<Reference> references = new ArrayList<>();
    List<Node> refListNodes = NodeListAdapter.wrap(doc.getElementsByTagName("ref-list"));

    if (ParseXmlUtil.isNullOrEmpty(refListNodes)) {
      log.info("No <ref-list> element was found in the xml.");
      return references;
    }

    for (Node refListNode : refListNodes) {
      if (refListNode.getNodeType() != Node.ELEMENT_NODE) {
        throw new XmlContentException("<ref-list> is not an element.");
      }

      Element refListElement = (Element) refListNode;

      System.out.println("time2-1=" + System.currentTimeMillis() % 10000);
      List<Node> refNodes = NodeListAdapter.wrap(refListElement.getElementsByTagName("ref"));
      references.addAll(refNodes.stream()
          .map(ref -> {
            try {
              long begin = System.currentTimeMillis();
              List<Reference> list = parseReferenceService.buildReferences((Element) ref, linkService);
              long end = System.currentTimeMillis();
              total += end - begin;
              return list;
            } catch (XMLParseException | IOException e) {
              throw new RuntimeException(e);
            }
          }) // Stream<List<Reference>>
          .flatMap(Collection::stream).collect(Collectors.toList()));
      System.out.println(" total=" + total);
      System.out.println("time2-4=" + System.currentTimeMillis() % 10000);
    }
    return references;
  }

  static long total = 0;

}
