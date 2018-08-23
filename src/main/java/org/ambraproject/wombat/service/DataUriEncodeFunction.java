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
    return new QName("http://www.ambraproject.org/xslt/", "embed-data-uri");
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

  public static int extractInt(XdmValue value) throws SaxonApiException {
    XdmItem item = value.itemAt(0);
    XdmAtomicValue atomic = (XdmAtomicValue) item;
    return Math.toIntExact(atomic.getLongValue());
  }

  public static String extractString(XdmValue value) {
    XdmItem item = value.itemAt(0);
    XdmAtomicValue atomic = (XdmAtomicValue) item;
    return atomic.getStringValue();
  }

  public RequestedDoiVersion getDoiFromArguments(XdmValue[] arguments) throws SaxonApiException {
    String doi = extractString(arguments[1]);
    int ingestionNumber = extractInt(arguments[2]);

    return RequestedDoiVersion.ofIngestion(doi, ingestionNumber);
  }

  private static String makeDataUrl(ContentType contentType, byte[] body) {
    return "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(body);
  }

  public static Optional<String> encodeAsDataUrl(HttpEntity entity) {
    ContentType contentType = ContentType.get(entity);
    if (contentType == null) {
      return Optional.empty();
    } else {
      try {
        return Optional.of(makeDataUrl(contentType, IOUtils.toByteArray(entity.getContent())));
      } catch (IOException ex) {
        log.warn("Caught exception generating data-uri: {}", ex);
        return Optional.empty();
      }
    }
  }
  
  private Optional<ContentKey> getThumbnailContentKey(RequestedDoiVersion doi) {
    try {
      AssetPointer asset = articleResolutionService.toParentIngestion(doi);
      Map<String, ?> files = articleService.getItemFiles(asset);
      return Optional.of(createKeyFromMap((Map<String, String>) files.get("thumbnail")));
    } catch (IOException ex) {
      return Optional.empty();
    }      
  }

  public static ContentKey createKeyFromMap(Map<String, String> fileRepoMap) {
    String key = fileRepoMap.get("crepoKey");
    UUID uuid = UUID.fromString(fileRepoMap.get("crepoUuid"));
    ContentKey contentKey = ContentKey.createForUuid(key, uuid);
    if (fileRepoMap.get("bucketName") != null) {
      contentKey.setBucketName(fileRepoMap.get("bucketName"));
    }
    return contentKey;
  }

  @Override
  public XdmValue call(XdmValue[] arguments) throws SaxonApiException {
    XdmValue fallback = arguments[0];
    
    return getThumbnailContentKey(getDoiFromArguments(arguments))
      .flatMap((contentKey)->corpusContentApi.optionalRequest(contentKey))
      .map((request)->request.getEntity())
      .flatMap(DataUriEncodeFunction::encodeAsDataUrl)
      .map((s) -> (XdmValue) new XdmAtomicValue(s))
      .orElse(fallback);
  }
}
