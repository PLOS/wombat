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

import com.google.common.collect.ImmutableMap;
import org.ambraproject.wombat.config.site.RequestMappingContextDictionary;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.site.url.Link;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.ambraproject.wombat.service.remote.ApiAddress;
import org.ambraproject.wombat.service.ArticleResolutionService;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Mappings for requests for DOIs that belong to works of unknown type.
 */
@Controller
public class GeneralDoiController extends WombatController {

  @Autowired
  private ArticleApi articleApi;
  @Autowired
  private RequestMappingContextDictionary requestMappingContextDictionary;
  @Autowired
  private SiteSet siteSet;

  @RequestMapping(name = "doi", value = "/doi")
  public RedirectView redirectFromDoi(HttpServletRequest request,
                                      @SiteParam Site site,
                                      RequestedDoiVersion id)
      throws IOException {
    return getRedirectFor(site, id).getRedirect(request);
  }

  Map<String, ?> getMetadataForDoi(RequestedDoiVersion id) throws IOException {
    ApiAddress address = ApiAddress.builder("dois").embedDoi(id.getDoi()).build();
    try {
      return articleApi.requestObject(address, Map.class);
    } catch (EntityNotFoundException e) {
      throw new NotFoundException("DOI not found: " + id, e);
    }
  }

  private static class DoiTypeInfo {
    private final String typeKey;
    private final String journalKey;

    private DoiTypeInfo(String typeKey, String journalKey) {
      this.typeKey = Objects.requireNonNull(typeKey);
      this.journalKey = Objects.requireNonNull(journalKey);
    }
  }

  private DoiTypeInfo getTypeOf(RequestedDoiVersion id) throws IOException {
    Map<String, ?> doiMetadata = getMetadataForDoi(id);
    String doiType = (String) doiMetadata.get("type");

    if (!doiMetadata.containsKey("article")) {
      Map<String, ?> journal = (Map<String, ?>) doiMetadata.get("journal");
      String journalKey = (String) journal.get("journalKey");
      return new DoiTypeInfo(doiType, journalKey);
    }

    // The DOI belongs to an article asset.
    // Request its latest revision in order to get its journal and, if it is an asset, its itemType.
    Map<String, ?> articleMetadata = (Map<String, ?>) doiMetadata.get("article");
    Map<String, Number> revisionTable = (Map<String, Number>) articleMetadata.get("revisions");
    Optional<Integer> ingestionNumber = ArticleResolutionService.findLatestRevision(revisionTable)
        .map(ArticleResolutionService.RevisionPointer::getIngestionNumber);
    if (!ingestionNumber.isPresent()) {
      // The article is unpublished. There is no particular ingestion whose itemType we should use.
      throw new NotFoundException();
    }
    String articleDoi = (String) articleMetadata.get("doi");
    Map<String, ?> ingestionMetadata = articleApi.requestObject(
        ApiAddress.builder("articles").embedDoi(articleDoi)
            .addToken("ingestions").addToken(ingestionNumber.get().toString()).build(),
        Map.class);
    Map<String, ?> journal = (Map<String, ?>) ingestionMetadata.get("journal");
    String journalKey = (String) journal.get("journalKey");

    if (!doiType.equals("asset")) {
      return new DoiTypeInfo(doiType, journalKey);
    }

    Map<String, ?> itemView = articleApi.requestObject(
        ApiAddress.builder("articles").embedDoi(articleDoi)
            .addToken("ingestions").addToken(ingestionNumber.get().toString())
            .addToken("items").build(),
        Map.class);
    Map<String, ?> itemTable = (Map<String, ?>) itemView.get("items");
    String canonicalDoi = (String) doiMetadata.get("doi");
    Map<String, ?> itemMetadata = (Map<String, ?>) itemTable.get(canonicalDoi);
    String itemType = (String) itemMetadata.get("itemType");

    return new DoiTypeInfo(itemType, journalKey);
  }

  /**
   * A function that produces a redirect link for a type of DOI-identified content.
   */
  @FunctionalInterface
  private static interface RedirectFunction {
    /**
     * @param factory a link factory set up to point at a particular site
     * @param id      the requested DOI
     * @return a link to the handler covered by this object, resolving to the given site and DOI
     */
    Link getLink(Link.Factory factory, RequestedDoiVersion id);
  }

  private final ImmutableMap<String, RedirectFunction> redirectHandlers = ImmutableMap.<String, RedirectFunction>builder()
      .put("volume", redirectToSinglePage("browseVolumes"))
      .put("issue", redirectWithIdParameter("browseIssues"))
      .put("comment", redirectWithIdParameter("articleCommentTree"))

      .put("article", redirectWithIdParameter("article"))
      .put("figure", redirectWithIdParameter("figurePage"))
      .put("table", redirectWithIdParameter("figurePage"))
      .put("reviewLetter", redirectWithIdParameter("peerReview"))

      .put("supplementaryMaterial", redirectToAssetFile("supplementary"))
      .put("graphic", redirectToAssetFile("original"))
      .put("standaloneStrikingImage", redirectToAssetFile("original"))

      .build();

  private Link.Factory.PatternBuilder buildLinkToId(Link.Factory factory, RequestedDoiVersion id, String handlerName) {
    Link.Factory.PatternBuilder builder = factory.toPattern(requestMappingContextDictionary, handlerName);
    builder.addQueryParameter("id", id.getDoi());
    id.getRevisionNumber().ifPresent(revisionNumber ->
        builder.addQueryParameter("rev", revisionNumber));
    return builder;
  }

  private RedirectFunction redirectWithIdParameter(String handlerName) {
    Objects.requireNonNull(handlerName);
    return (Link.Factory factory, RequestedDoiVersion id) ->
        buildLinkToId(factory, id, handlerName).build();
  }

  /**
   * @return a {@code RedirectFunction} that only goes to a handler and ignores the {@link RequestedDoiVersion}
   */
  private RedirectFunction redirectToSinglePage(String handlerName) {
    Objects.requireNonNull(handlerName);
    return (Link.Factory factory, RequestedDoiVersion id) ->
        factory.toPattern(requestMappingContextDictionary, handlerName).build();
  }

  private RedirectFunction redirectToAssetFile(String fileType) {
    Objects.requireNonNull(fileType);
    return (Link.Factory factory, RequestedDoiVersion id) ->
        buildLinkToId(factory, id, "assetFile")
            .addQueryParameter("type", fileType)
            .build();
  }

  private Link getRedirectFor(Site site, RequestedDoiVersion id) throws IOException {
    DoiTypeInfo typeInfo = getTypeOf(id);
    RedirectFunction redirectFunction = redirectHandlers.get(typeInfo.typeKey);
    if (redirectFunction == null) {
      throw new RuntimeException(String.format("Unrecognized type (%s) for: %s", typeInfo.typeKey, id));
    }
    Site targetSite = site.getTheme().resolveForeignJournalKey(siteSet, typeInfo.journalKey);
    Link.Factory factory = Link.toAbsoluteAddress(targetSite);
    return redirectFunction.getLink(factory, id);
  }

}
