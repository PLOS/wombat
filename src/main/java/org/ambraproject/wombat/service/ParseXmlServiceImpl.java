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
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
  public List<Reference> parseArticleReferences(InputStream xml) throws ParserConfigurationException, IOException, SAXException, XmlContentException {
    Objects.requireNonNull(xml);

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.parse(xml);
    // to combine adjacent text nodes and remove empty ones in the dom
    doc.getDocumentElement().normalize();

    List<Reference> references = new ArrayList<>();
    List<Node> refListNodes = NodeListAdapter.wrap(doc.getElementsByTagName("ref-list"));

    if (ParseXmlUtil.isNullOrEmpty(refListNodes)) {
      log.info("No <ref-list> element was found in the xml.");
      return references;
    }

    if (refListNodes.size() > 1) {
      throw new XmlContentException("More than one <ref-list> element was found in the xml.");
    }

    Node refListNode = refListNodes.get(0);
    if (refListNode.getNodeType() != Node.ELEMENT_NODE) {
      throw new XmlContentException("<ref-list> is not an element.");
    }

    Element refListElement = (Element) refListNode;

    List<Node> refNodes = NodeListAdapter.wrap(refListElement.getElementsByTagName("ref"));
    references = refNodes.stream()
        .map(ref -> {
          try {
            return parseReferenceService.buildReferences((Element) ref);
          } catch (XMLParseException e) {
            throw new RuntimeException(e);
          }
        }) // Stream<List<Reference>>
        .flatMap(r -> r.stream()).collect(Collectors.toList());

    return references;
  }
}
