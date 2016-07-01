package org.ambraproject.wombat.service;

import org.ambraproject.wombat.model.CitedArticle;

import java.io.InputStream;
import java.util.List;

/**
 * This class is used to parse the document xml
 */
public interface ParseXmlService {
  List<CitedArticle> parseArticleCitation(InputStream xml);
}
