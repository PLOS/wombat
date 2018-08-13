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

import com.google.common.base.Charsets;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.ambraproject.wombat.service.CitationDownloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Controller for rendering and downloading ciatations.
 */
@Controller
public class CitationController extends WombatController {

  private static final Logger log = LoggerFactory.getLogger(CitationController.class);

  @Autowired
  private CitationDownloadService citationDownloadService;
  @Autowired
  private ArticleMetadata.Factory articleMetadataFactory;


  @RequestMapping(name = "citationDownloadPage", value = "/article/citation")
  public String renderCitationDownloadPage(HttpServletRequest request, Model model, @SiteParam Site site,
                                           RequestedDoiVersion articleId)
      throws IOException {
    articleMetadataFactory.get(site, articleId)
        .validateVisibility("citationDownloadPage")
        .populate(request, model);
    return site + "/ftl/article/citationDownload";
  }

  @RequestMapping(name = "downloadRisCitation", value = "/article/citation/ris", produces = "application/x-research-info-systems;charset=UTF-8")
  public ResponseEntity<String> serveRisCitationDownload(@SiteParam Site site, RequestedDoiVersion articleId)
      throws IOException {
    return serveCitationDownload(site, "downloadRisCitation", articleId, "ris",
        citationDownloadService::buildRisCitation);
  }

  @RequestMapping(name = "downloadBibtexCitation", value = "/article/citation/bibtex", produces = "application/x-bibtex;charset=UTF-8")
  public ResponseEntity<String> serveBibtexCitationDownload(@SiteParam Site site, RequestedDoiVersion articleId)
      throws IOException {
    return serveCitationDownload(site, "downloadBibtexCitation", articleId, "bib",
        citationDownloadService::buildBibtexCitation);
  }

  private ResponseEntity<String> serveCitationDownload(Site site, String handlerName,
                                                       RequestedDoiVersion articleId,
                                                       String fileExtension,
                                                       Function<Map<String, ?>, String> serviceFunction)
      throws IOException {
    ArticleMetadata articleMetadata = articleMetadataFactory.get(site, articleId)
        .validateVisibility(handlerName);
    Map<String, Object> combinedMetadata = new HashMap<>();
    combinedMetadata.putAll(articleMetadata.getIngestionMetadata());
    combinedMetadata.putAll(articleMetadata.getAuthors());

    String citationBody = serviceFunction.apply(combinedMetadata);
    String contentDispositionValue = String.format("attachment; filename=\"%s.%s\"",
        URLEncoder.encode((String) combinedMetadata.get("doi"), Charsets.UTF_8.toString()),
        fileExtension);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_DISPOSITION, contentDispositionValue);
    return new ResponseEntity<>(citationBody, headers, HttpStatus.OK);
  }

}
