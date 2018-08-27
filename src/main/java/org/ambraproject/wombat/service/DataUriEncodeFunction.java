/*
 * Copyright (c) 2018 Public Library of Science
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

import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import net.sf.saxon.s9api.ExtensionFunction;
import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.OccurrenceIndicator;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SequenceType;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;
import org.ambraproject.wombat.identity.AssetPointer;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.ambraproject.wombat.service.remote.ContentKey;
import org.ambraproject.wombat.service.remote.CorpusContentApi;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataUriEncodeFunction implements ExtensionFunction {
  private static final Logger log = LoggerFactory.getLogger(DataUriEncodeFunction.class);

  @Autowired
  private CorpusContentApi corpusContentApi;

  @Autowired
  private ArticleResolutionService articleResolutionService;

  @Autowired
  private ArticleService articleService;

  @Override
  public QName getName() {
    return new QName("http://www.ambraproject.org/xslt/", "embed-data-url");
  }

  @Override
  public SequenceType getResultType() {
    return SequenceType.makeSequenceType(ItemType.STRING, OccurrenceIndicator.ONE);
  }

  @Override
  public SequenceType[] getArgumentTypes() {
    return new SequenceType[] {
      // Fallback
      SequenceType.makeSequenceType(ItemType.STRING, OccurrenceIndicator.ONE),
      // DOI
      SequenceType.makeSequenceType(ItemType.STRING, OccurrenceIndicator.ONE),
      // Ingestion Number
      SequenceType.makeSequenceType(ItemType.INTEGER, OccurrenceIndicator.ONE)
    };
  }

  /**
   * Extract a single int value from an XdmValue.
   * @param value the XdmValue to extract from
   * @return the int extracted
   */
  public static int extractInt(XdmValue value) throws SaxonApiException {
    XdmItem item = value.itemAt(0);
    XdmAtomicValue atomic = (XdmAtomicValue) item;
    return Math.toIntExact(atomic.getLongValue());
  }

  /**
   * Extract a single string value from an XdmValue.
   * @param value the XdmValue to extract from
   * @return the string extracted
   */
  public static String extractString(XdmValue value) {
    XdmItem item = value.itemAt(0);
    XdmAtomicValue atomic = (XdmAtomicValue) item;
    return atomic.getStringValue();
  }

  /**
   * Generate a RequestedDoiVersion from the arguments passed.
   * @param arguments an XdmValue array. The first entry is ignored,
   *   the second is the doi string, and the third is the ingestion
   *   number
   * @return the RequestedDoiVersion generated from the arguments
   */
  public RequestedDoiVersion getDoiFromArguments(XdmValue[] arguments) throws SaxonApiException {
    String doi = extractString(arguments[1]);
    int ingestionNumber = extractInt(arguments[2]);

    return RequestedDoiVersion.ofIngestion(doi, ingestionNumber);
  }

  private static String makeDataUrl(ContentType contentType, byte[] body) {
    return "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(body);
  }

  /**
   * Encode a HttpEntity as a data url, or return empty if there was an IOException.
   * @param entity  the HttpEntity to encode; will use the Content-Type and the body
   * @return either a string representing the base-64 encoded data
   *   url, or empty if there was an error
   */
  public static Optional<String> encodeAsDataUrl(HttpEntity entity) {
    ContentType contentType = ContentType.get(entity);
    if (contentType == null) {
      return Optional.empty();
    } else {
      try {
        return Optional.of(makeDataUrl(contentType, IOUtils.toByteArray(entity.getContent())));
      } catch (IOException ex) {
        log.warn("Caught exception generating data-url: {}", ex);
        return Optional.empty();
      }
    }
  }
  
  /**
   * Function to be used from within stylesheets. Will either encode a
   * thumbnail as a data-url or return a fallback URL.
   * @param arguments an array of XdmValue arguments passed in from
   *   the stylesheet. The first is a fallback URL that will be
   *   returned if there is a problem fetching any data. The second is
   *   the doi. The third is the ingestion number to retrieve.
   * @return either a data-url for the thumbnail content or the
   *   fallback string
   */
  @Override
  public XdmValue call(XdmValue[] arguments) throws SaxonApiException {
    XdmValue fallback = arguments[0];
    
    return articleService.getThumbnailKey(getDoiFromArguments(arguments))
      .flatMap(corpusContentApi::optionalRequest)
      .map(CloseableHttpResponse::getEntity)
      .flatMap(DataUriEncodeFunction::encodeAsDataUrl)
      .map((s)-> (XdmValue) new XdmAtomicValue(s)) // Is there a more succient way to do this?
      .orElse(fallback);
  }
}
