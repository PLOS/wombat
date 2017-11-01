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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.ambraproject.wombat.config.site.RequestMappingContextDictionary;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.config.site.url.Link;
import org.ambraproject.wombat.identity.AssetPointer;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.ambraproject.wombat.service.ArticleResolutionService;
import org.ambraproject.wombat.service.ArticleService;
import org.ambraproject.wombat.service.remote.ContentKey;
import org.ambraproject.wombat.service.remote.CorpusContentApi;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Controller
public class ArticleAssetController extends WombatController {

  private static final Logger log = LoggerFactory.getLogger(ArticleAssetController.class);

  @Autowired
  private CorpusContentApi corpusContentApi;
  @Autowired
  private ArticleResolutionService articleResolutionService;
  @Autowired
  private ArticleService articleService;
  @Autowired
  private RequestMappingContextDictionary requestMappingContextDictionary;

  static enum AssetUrlStyle {
    FIGURE_IMAGE("figureImage", "size", new String[]{"figure", "table", "standaloneStrikingImage"}),
    ASSET_FILE("assetFile", "type", new String[]{"article", "supplementaryMaterial", "graphic"});

    private final String handlerName;
    private final String typeParameterName;
    private final ImmutableSet<String> itemTypes;

    private AssetUrlStyle(String handlerName, String typeParameterName, String[] itemTypes) {
      this.handlerName = Objects.requireNonNull(handlerName);
      this.typeParameterName = Objects.requireNonNull(typeParameterName);
      this.itemTypes = ImmutableSet.copyOf(itemTypes);
    }

    private static final ImmutableMap<String, AssetUrlStyle> BY_ITEM_TYPE =
        EnumSet.allOf(AssetUrlStyle.class).stream()
            .flatMap((AssetUrlStyle s) -> s.itemTypes.stream()
                .map((String itemType) -> Maps.immutableEntry(itemType, s)))
            .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));

    static AssetUrlStyle findByItemType(String itemType) {
      AssetUrlStyle style = BY_ITEM_TYPE.get(itemType);
      if (style == null) throw new IllegalArgumentException("Unrecognized: " + itemType);
      return style;
    }

    Link buildRedirectLink(RequestMappingContextDictionary rmcd,
                           Site site, RequestedDoiVersion id,
                           String fileType, boolean isDownload) {
      Link.Factory.PatternBuilder builder = Link.toLocalSite(site).toPattern(rmcd, handlerName);

      // It's better to use the RequestedDoiVersion than an AssetPointer because, if the user made a request with
      // no revision information, we don't want to permanently redirect to a URL that has a revision number.
      builder.addQueryParameter(DoiVersionArgumentResolver.ID_PARAMETER, id.getDoi());
      id.getRevisionNumber().ifPresent(revisionNumber ->
          builder.addQueryParameter(DoiVersionArgumentResolver.REVISION_PARAMETER, revisionNumber));
      id.getIngestionNumber().ifPresent(ingestionNumber ->
          builder.addQueryParameter(DoiVersionArgumentResolver.INGESTION_PARAMETER, ingestionNumber));

      builder.addQueryParameter(typeParameterName, fileType);
      if (isDownload) {
        builder.addQueryParameter("download", "");
      }
      return builder.build();
    }
  }


  private void serve(HttpServletRequest requestFromClient, HttpServletResponse responseToClient,
                     AssetUrlStyle requestedStyle, Site site, RequestedDoiVersion id,
                     String fileType, boolean isDownload)
      throws IOException {
    AssetPointer asset = articleResolutionService.toParentIngestion(id);

    Map<String, ?> itemMetadata = articleService.getItemMetadata(asset);
    String itemType = (String) itemMetadata.get("itemType");
    AssetUrlStyle itemStyle = AssetUrlStyle.findByItemType(itemType);
    if (requestedStyle != itemStyle) {
      Link redirectLink = itemStyle.buildRedirectLink(requestMappingContextDictionary, site, id, fileType, isDownload);
      String location = redirectLink.get(requestFromClient);
      log.warn(String.format("Redirecting %s request for %s to <%s>. Bad link?",
          requestedStyle, asset.asParameterMap(), location));
      redirectTo(responseToClient, location);
      return;
    }

    Map<String, ?> files = (Map<String, ?>) itemMetadata.get("files");
    Map<String, ?> fileRepoKey = (Map<String, ?>) files.get(fileType);
    if (fileRepoKey == null) {
      fileRepoKey = handleUnmatchedFileType(id, itemType, fileType, files);
    }

    // TODO: Check visibility against site?

    ContentKey contentKey = createKey(fileRepoKey);
    try (CloseableHttpResponse responseFromApi = corpusContentApi.request(contentKey, ImmutableList.of())) {
      forwardAssetResponse(responseFromApi, responseToClient, isDownload);
    }
  }

  /**
   * The keys are item types where, if at item of that type has only one file and get a request for an missing file
   * type, we prefer to serve the one file and log a warning rather than serve a 404 response. The values are file types
   * expected to exist, to ensure that we still serve 404s on URLs that are total nonsense.
   * <p>
   * This is a kludge over a data condition in PLOS's corpus where some items were not ingested with reformatted images
   * as expected. We assume that it is safe to recover gracefully for certain item types because there is generally
   * little difference in image resizing.
   * <p>
   * The main omission is {@code itemType="supplementaryMaterial"}, where we still want to be strict about the {@code
   * fileType="supplementary"}. Also, a hit on {@code itemType="figure"} or {@code itemType="table"} is bigger trouble
   * because the image resizing can be very significant, so we want a hard failure in that case.
   */
  // TODO: Get values from a Rhino API library, if and when such a thing exists
  private static final ImmutableSetMultimap<String, String> ITEM_TYPES_TO_TOLERATE = ImmutableSetMultimap.<String, String>builder()
      .putAll("graphic", "original", "thumbnail")
      .putAll("standaloneStrikingImage", "original", "small", "inline", "medium", "large")
      .build();

  /**
   * Handle a file request where the supplied file type could not be matched to any files for an existing item. If
   * possible, resolve to another file to serve instead. Else, throw an appropriate exception.
   */
  private static Map<String, ?> handleUnmatchedFileType(RequestedDoiVersion id,
                                                        String itemType, String fileType,
                                                        Map<String, ?> files) {
    if (files.size() == 1 && ITEM_TYPES_TO_TOLERATE.containsEntry(itemType, fileType)) {
      Map.Entry<String, ?> onlyEntry = Iterables.getOnlyElement(files.entrySet());
      log.warn(String.format("On %s (%s), received request for %s; only available file is %s",
          id, itemType, fileType, onlyEntry.getKey()));
      return (Map<String, ?>) onlyEntry.getValue();
    }
    String message = String.format("Unrecognized file type (\"%s\") for id: %s", fileType, id);
    throw new NotFoundException(message);
  }

  /**
   * Redirect to a given link. (We can't just return a {@link org.springframework.web.servlet.view.RedirectView} because
   * we ordinarily want to pass the raw response to {@link #forwardAssetResponse}. So we mess around with it directly.)
   */
  private void redirectTo(HttpServletResponse response, String location) {
    response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
    response.setHeader(HttpHeaders.LOCATION, location);
  }

  @RequestMapping(name = "assetFile", value = "/article/file", params = {"type"})
  public void serveAssetFile(HttpServletRequest request, HttpServletResponse response,
                             @SiteParam Site site,
                             RequestedDoiVersion id,
                             @RequestParam(value = "type", required = true) String fileType,
                             @RequestParam(value = "download", required = false) String isDownload)
      throws IOException {
    serve(request, response, AssetUrlStyle.ASSET_FILE, site, id, fileType, booleanParameter(isDownload));
  }

  @RequestMapping(name = "figureImage", value = "/article/figure/image")
  public void serveFigureImage(HttpServletRequest request, HttpServletResponse response,
                               @SiteParam Site site,
                               RequestedDoiVersion id,
                               @RequestParam(value = "size") String figureSize,
                               @RequestParam(value = "download", required = false) String isDownload)
      throws IOException {
    serve(request, response, AssetUrlStyle.FIGURE_IMAGE, site, id, figureSize, booleanParameter(isDownload));
  }

  private static ContentKey createKey(Map<String, ?> fileRepoKey) {
    String key = (String) fileRepoKey.get("crepoKey");
    UUID uuid = UUID.fromString((String) fileRepoKey.get("crepoUuid"));
    ContentKey contentKey = ContentKey.createForUuid(key, uuid);
    if (fileRepoKey.get("bucketName") != null) {
      contentKey.setBucketName(fileRepoKey.get("bucketName").toString());
    }
    return contentKey;
  }

}
