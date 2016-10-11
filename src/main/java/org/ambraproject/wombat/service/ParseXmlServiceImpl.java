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
  public List<Reference> parseArticleReferences(InputStream xml) throws IOException {
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

      List<Node> refNodes = NodeListAdapter.wrap(refListElement.getElementsByTagName("ref"));
      references.addAll(refNodes.stream()
          .map(ref -> {
            try {
              return parseReferenceService.buildReferences((Element) ref);
            } catch (XMLParseException e) {
              throw new RuntimeException(e);
            }
          }) // Stream<List<Reference>>
          .flatMap(Collection::stream).collect(Collectors.toList()));
    }
    return references;
  }
}
