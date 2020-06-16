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

import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.identity.ArticlePointer;
import org.ambraproject.wombat.model.Reference;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;

import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * This class is used to parse the article xml
 */
public interface ParseXmlService {

  @SuppressWarnings("serial")
  static class XmlContent implements Serializable {
    public final String html;
    public final ImmutableList<Reference> references;

    public XmlContent(String html, List<Reference> references) {
      this.html = Objects.requireNonNull(html);
      this.references = ImmutableList.copyOf(references);
    }
  }

  /**
   * @param xml article xml
   * @return article xml transformed into a document
   * @throws IOException
   */
  Document getDocument(InputStream xml) throws IOException;

  /**
   * Parses the references in the article xml
   *
   * @param doc article xml transformed into a document
   * @return list of Reference objects
   * @throws ParserConfigurationException
   * @throws IOException
   * @throws SAXException
   * @throws XmlContentException
   */
  List<Reference> parseArticleReferences(Document doc,
                                         ParseReferenceService.DoiToJournalLinkService linkService) throws IOException;

  XmlContent getXmlContent(Site site, ArticlePointer articlePointer, HttpServletRequest request) throws IOException;
}
