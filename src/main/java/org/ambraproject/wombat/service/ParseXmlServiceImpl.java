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

import org.ambraproject.wombat.config.site.RequestMappingContextDictionary;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.site.url.Link;
import org.ambraproject.wombat.identity.ArticlePointer;
import org.ambraproject.wombat.model.Reference;
import org.ambraproject.wombat.service.remote.CorpusContentApi;
import org.ambraproject.wombat.util.NodeListAdapter;
import org.ambraproject.wombat.util.ParseXmlUtil;
import org.apache.commons.io.output.WriterOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.management.modelmbean.XMLParseException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class ParseXmlServiceImpl implements ParseXmlService {

  /**
   * Initial size (in bytes) of buffer that holds transformed article HTML before passing it to the model.
   */
  private static final int XFORM_BUFFER_SIZE = 0x8000;

  private static final Logger log = LoggerFactory.getLogger(ParseXmlServiceImpl.class);

  @Autowired
  private SiteSet siteSet;

  @Autowired
  private RequestMappingContextDictionary requestMappingContextDictionary;

  @Autowired
  private Charset charset;

  @Autowired
  private CorpusContentApi corpusContentApi;

  @Autowired
  private ParseReferenceService parseReferenceService;

  @Autowired
  private DoiToJournalResolutionService doiToJournalResolutionService;

  @Autowired
  private ArticleTransformService articleTransformService;

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

      List<Node> refNodes = NodeListAdapter.wrap(refListElement.getElementsByTagName("ref"));
      references.addAll(refNodes.stream()
          .map(ref -> {
            try {
              return parseReferenceService.buildReferences((Element) ref, linkService);
            } catch (XMLParseException | IOException e) {
              throw new RuntimeException(e);
            }
          }) // Stream<List<Reference>>
          .flatMap(Collection::stream).collect(Collectors.toList()));
    }
    return references;
  }
  
  /**
   * Gets article xml from cache if it exists; otherwise, gets it from rhino and caches it. Then it parses the
   * references and does html transform
   *
   * @param articlePointer
   * @param request
   * @return an XmlContent containing the list of references and article html
   * @throws IOException
   */
  public XmlContent getXmlContent(Site site, ArticlePointer articlePointer, HttpServletRequest request) throws IOException {
    InputStream stream = corpusContentApi.readManuscript(articlePointer);
    byte[] xml = ByteStreams.toByteArray(stream);
    final Document document = getDocument(new ByteArrayInputStream(xml));
    
    // do not supply Solr related link service now
    List<Reference> references = parseArticleReferences(document, null);
    
    // invoke the Solr API once to resolve all journal keys
    List<String> dois = references.stream().map(ref -> ref.getDoi()).filter(doi -> inPlosJournal(doi)).collect(Collectors.toList());
    List<String> keys = doiToJournalResolutionService.getJournalKeysFromDois(dois, site);
    
    // store the link text from journal key to references.
    // since Reference is immutable, need to create a new list of new reference objects.
    Iterator<Reference> itRef = references.iterator();
    Iterator<String> itKey = keys.iterator();
    List<Reference> referencesWithLinks = new ArrayList<Reference>();
    while (itRef.hasNext()) {
      Reference ref = itRef.next();
      if (!inPlosJournal(ref.getDoi())) {
        referencesWithLinks.add(ref);
        continue;
      }
      
      String key = itKey.next();
      if (Strings.isNullOrEmpty(key)) {
        referencesWithLinks.add(ref);
        continue;
      }
      
      Reference.Builder builder = new Reference.Builder(ref);
      Reference refWithLink = builder.setFullArticleLink(getLinkText(site, request, ref.getDoi(), key)).build();
      referencesWithLinks.add(refWithLink);
    }
    
    references = referencesWithLinks;
    
    StringWriter articleHtml = new StringWriter(XFORM_BUFFER_SIZE);
    try (OutputStream outputStream = new WriterOutputStream(articleHtml, charset)) {
      articleTransformService.transformArticle(site, articlePointer, references,
                                               new ByteArrayInputStream(xml), outputStream);
    }
    
    return new XmlContent(articleHtml.toString(), references);
  }

  private Boolean inPlosJournal(String doi) {
    return doi != null && doi.startsWith("10.1371/");
  }

  private String getLinkText(Site site, HttpServletRequest request, String doi, String citationJournalKey) throws IOException {
    String linkText = null;
    if (citationJournalKey != null) {
      linkText = Link.toForeignSite(site, citationJournalKey, siteSet)
          .toPattern(requestMappingContextDictionary, "article")
          .addQueryParameter("id", doi)
          .build()
          .get(request);
    }
    return linkText;
  }
}
