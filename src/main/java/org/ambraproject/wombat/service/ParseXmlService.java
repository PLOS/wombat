package org.ambraproject.wombat.service;

import org.ambraproject.wombat.model.Reference;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * This class is used to parse the document xml
 */
public interface ParseXmlService {
  List<Reference> parseArticleReferences(InputStream xml) throws ParserConfigurationException, IOException, SAXException, XmlContentException;
}
