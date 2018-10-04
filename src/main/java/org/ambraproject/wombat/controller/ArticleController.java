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

package org.ambraproject.wombat.controller;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.ambraproject.wombat.config.site.RequestMappingContextDictionary;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.site.url.Link;
import org.ambraproject.wombat.identity.ArticlePointer;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.ambraproject.wombat.model.Reference;
import org.ambraproject.wombat.service.ArticleTransformService;
import org.ambraproject.wombat.service.DoiToJournalResolutionService;
import org.ambraproject.wombat.service.ParseXmlService;
import org.ambraproject.wombat.service.remote.CorpusContentApi;
import org.ambraproject.wombat.service.remote.orcid.OrcidApi;
import org.ambraproject.wombat.service.remote.orcid.OrcidAuthenticationTokenExpiredException;
import org.ambraproject.wombat.service.remote.orcid.OrcidAuthenticationTokenReusedException;
import org.apache.commons.io.output.WriterOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.w3c.dom.Document;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for rendering an article.
 */
@Controller
public class ArticleController extends WombatController {

  private static final Logger log = LoggerFactory.getLogger(ArticleController.class);

  /**
   * Initial size (in bytes) of buffer that holds transformed article HTML before passing it to the model.
   */
  private static final int XFORM_BUFFER_SIZE = 0x8000;

  @Autowired
  private Charset charset;
  @Autowired
  private CorpusContentApi corpusContentApi;
  @Autowired
  private ArticleTransformService articleTransformService;
  @Autowired
  private ArticleMetadata.Factory articleMetadataFactory;
  @Autowired
  private ParseXmlService parseXmlService;
  @Autowired
  private Gson gson;
  @Autowired
  private RuntimeConfiguration runtimeConfiguration;
  @Autowired
  private SiteSet siteSet;
  @Autowired
  private RequestMappingContextDictionary requestMappingContextDictionary;
  @Autowired
  private DoiToJournalResolutionService doiToJournalResolutionService;
  @Autowired
  private OrcidApi orcidApi;

  // TODO: this method currently makes 5 backend RPCs, all sequentially. Explore reducing this
  // number, or doing them in parallel, if this is a performance bottleneck.
  @RequestMapping(name = "article", value = "/article")
  public String renderArticle(HttpServletRequest request,
                              Model model,
                              @SiteParam Site site,
                              RequestedDoiVersion articleId)
      throws IOException {
    ArticlePointer articlePointer = articleMetadataFactory.get(site, articleId)
        .validateVisibility("article")
        .populate(request, model)
        .fillAmendments(model)
        .getArticlePointer();

    XmlContent xmlContent = getXmlContent(site, articlePointer, request);
    model.addAttribute("articleText", xmlContent.html);
    model.addAttribute("references", xmlContent.references);

    return site + "/ftl/article/article";
  }

  /**
   * Serves a request for the "about the authors" page for an article.
   *
   * @param model     data to pass to the view
   * @param site      current site
   * @param articleId specifies the article
   * @return path to the template
   * @throws IOException
   */
  @RequestMapping(name = "articleAuthors", value = "/article/authors")
  public String renderArticleAuthors(HttpServletRequest request, Model model, @SiteParam Site site,
                                     RequestedDoiVersion articleId) throws IOException {
    articleMetadataFactory.get(site, articleId)
        .validateVisibility("articleAuthors")
        .populate(request, model);
    return site + "/ftl/article/authors";
  }

  /**
   * Serves the article metrics tab content for an article.
   *
   * @param model     data to pass to the view
   * @param site      current site
   * @param articleId specifies the article
   * @return path to the template
   * @throws IOException
   */
  @RequestMapping(name = "articleMetrics", value = "/article/metrics")
  public String renderArticleMetrics(HttpServletRequest request, Model model, @SiteParam Site site,
                                     RequestedDoiVersion articleId) throws IOException {
    articleMetadataFactory.get(site, articleId)
        .validateVisibility("articleMetrics")
        .populate(request, model);
    return site + "/ftl/article/metrics";
  }

  /**
   * Serves the related content tab content for an article.
   *
   * @param model     data to pass to the view
   * @param site      current site
   * @param articleId specifies the article
   * @return path to the template
   * @throws IOException
   */
  @RequestMapping(name = "articleRelatedContent", value = "/article/related")
  public String renderArticleRelatedContent(HttpServletRequest request, Model model, @SiteParam Site site,
                                            RequestedDoiVersion articleId) throws IOException {
    articleMetadataFactory.get(site, articleId)
        .validateVisibility("articleRelatedContent")
        .populate(request, model);
    return site + "/ftl/article/relatedContent";
  }

  /**
   * Serves the peer review tab content for an article.
   *
   * @param model     data to pass to the view
   * @param site      current site
   * @param articleId specifies the article
   * @return path to the template
   * @throws IOException
   */
  @RequestMapping(name = "articlePeerReview", value = "/article/peerReview")
  public String renderArticlePeerReview(HttpServletRequest request, Model model, @SiteParam Site site,
                                            RequestedDoiVersion articleId) throws IOException {
    articleMetadataFactory.get(site, articleId)
        .validateVisibility("articlePeerReview")
        .populate(request, model);
    return site + "/ftl/article/peerReview";
  }

  /**
   * Returns a list of figures and tables of a given article; main usage is the figshare tile on the Metrics
   * tab
   *
   * @param site current site
   * @param articleId DOI identifying the article
   * @return a list of figures and tables of a given article
   * @throws IOException
   */
  @RequestMapping(name = "articleFigsAndTables", value = "/article/assets/figsAndTables")
  public ResponseEntity<List> listArticleFiguresAndTables(@SiteParam Site site,
                                                          RequestedDoiVersion articleId) throws IOException {
    List<Map<String, ?>> figureView = articleMetadataFactory.get(site, articleId)
        .validateVisibility("articleFigsAndTables")
        .getFigureView();

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    return new ResponseEntity<>(figureView, headers, HttpStatus.OK);
  }

  @RequestMapping(name = "uploadPreprintRevision", value = "/article/uploadPreprintRevision")
  public String uploadPreprintRevision(HttpServletRequest request, Model model, @SiteParam Site site,
                                       @RequestParam("state") String state,
                                       @RequestParam("code") String code) throws IOException, URISyntaxException {
    final byte[] decodedState = Base64.getDecoder().decode(state);
    final String decodedJson = URLDecoder.decode(new String(decodedState), "UTF-8");
    Map<String, Object> stateJson = gson.fromJson(decodedJson, HashMap.class);

    String correspondingAuthorOrcidId = (String) stateJson.get("orcid_id");
    String authenticatedOrcidId = "";

    try {
      authenticatedOrcidId = orcidApi.getOrcidIdFromAuthorizationCode(site, code);
    } catch (OrcidAuthenticationTokenExpiredException | OrcidAuthenticationTokenReusedException e) {
      model.addAttribute("orcidAuthenticationError", e.getMessage());
    }

    boolean isError = true;
    if (correspondingAuthorOrcidId.equals(authenticatedOrcidId)) {
      model.addAttribute("orcidId", correspondingAuthorOrcidId);
      isError = false;
    } else if (!Strings.isNullOrEmpty(authenticatedOrcidId)) {
      model.addAttribute("orcidAuthenticationError", "ORCID IDs do not match. " +
          "Corresponding author ORCID ID must be used.");
    }

    if (isError) {
      final RequestedDoiVersion articleId = RequestedDoiVersion.of((String) stateJson.get("doi"));
      return renderArticle(request, model, site, articleId);
    } else {
      return site + "/ftl/article/uploadPreprintRevision";
    }
  }

  @SuppressWarnings("serial")
  static class XmlContent implements Serializable {
    private final String html;
    private final ImmutableList<Reference> references;

    public XmlContent(String html, List<Reference> references) {
      this.html = Objects.requireNonNull(html);
      this.references = ImmutableList.copyOf(references);
    }
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
  private XmlContent getXmlContent(Site site, ArticlePointer articlePointer,
                                   HttpServletRequest request) throws IOException {
    return corpusContentApi.readManuscript(articlePointer, site, "html", (InputStream stream) -> {
      byte[] xml = ByteStreams.toByteArray(stream);
      final Document document = parseXmlService.getDocument(new ByteArrayInputStream(xml));

      // do not supply Solr related link service now
      List<Reference> references = parseXmlService.parseArticleReferences(document, null);

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
    });
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
