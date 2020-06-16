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
import com.google.gson.Gson;
import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.identity.ArticlePointer;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.ambraproject.wombat.service.ParseXmlService;
import org.ambraproject.wombat.service.remote.orcid.OrcidApi;
import org.ambraproject.wombat.service.remote.orcid.OrcidAuthenticationTokenExpiredException;
import org.ambraproject.wombat.service.remote.orcid.OrcidAuthenticationTokenReusedException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Controller for rendering an article.
 */
@Controller
public class ArticleController extends WombatController {

  private static final Logger log = LogManager.getLogger(ArticleController.class);

  @Autowired
  private ArticleMetadata.Factory articleMetadataFactory;
  @Autowired
  private ParseXmlService parseXmlService;
  @Autowired
  private Gson gson;
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

    ParseXmlService.XmlContent xmlContent = parseXmlService.getXmlContent(site, articlePointer, request);
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

  void throwIfPeerReviewNotFound(Map<String, Object> map) {
    if (null == map.get("peerReview")) {
      throw new NotFoundException();
    }
  }
}
