package org.ambraproject.wombat.service;

import org.ambraproject.wombat.model.Reference;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * This class is used to parse the article xml
 */
public interface ParseXmlService {
  /**
   * Parses the references in the article xml
   *
   * @param xml  article xml
   * @return list of Reference objects
   * @throws ParserConfigurationException
   * @throws IOException
   * @throws SAXException
   * @throws XmlContentException
   */
  List<Reference> parseArticleReferences(InputStream xml,
                                         ParseReferenceService.DoiToJournalLinkService linkService) throws IOException;
}
