package org.ambraproject.wombat.controller;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.ambraproject.wombat.config.site.RequestMappingContextDictionary;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.site.url.Link;
import org.ambraproject.wombat.identity.AssetPointer;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.ambraproject.wombat.service.ApiAddress;
import org.ambraproject.wombat.service.ArticleResolutionService;
import org.ambraproject.wombat.service.ArticleService;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.ambraproject.wombat.service.remote.ContentKey;
import org.ambraproject.wombat.service.remote.CorpusContentApi;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

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
  private CorpusContentApi corpusContentApi;
  @Autowired
  private ArticleResolutionService articleResolutionService;
  @Autowired
  private ArticleService articleService;
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
    Map<String, ?> metadata = getMetadataForDoi(id);
    String doiType = (String) metadata.get("type");

    if (!metadata.containsKey("article")) {
      Map<String, ?> journal = (Map<String, ?>) metadata.get("journal");
      String journalKey = (String) journal.get("journalKey");
      return new DoiTypeInfo(doiType, journalKey);
    }

    // The DOI belongs to an article asset.
    // Request its latest revision in order to get its journal and, if it is an asset, its itemType.
    Map<String, ?> articleMetadata = (Map<String, ?>) metadata.get("article");
    Map<String, ?> revisionTable = (Map<String, ?>) articleMetadata.get("revisions");
    Optional<Integer> ingestionNumber = revisionTable.entrySet().stream()
        .max(Comparator.comparing(entry -> Integer.valueOf(entry.getKey()))) // find the latest revision
        .map(entry -> ((Number) entry.getValue()).intValue()); // extract its ingestion number
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
    String canonicalDoi = (String) metadata.get("doi");
    Map<String, ?> itemMetadata = (Map<String, ?>) itemTable.get(canonicalDoi);
    String itemType = (String) itemMetadata.get("itemType");

    return new DoiTypeInfo(itemType, journalKey);
  }

  @FunctionalInterface
  private static interface RedirectFunction {
    Link getLink(Link.Factory factory, RequestedDoiVersion id);
  }

  private final ImmutableMap<String, RedirectFunction> redirectHandlers = ImmutableMap.<String, RedirectFunction>builder()
      .put("volume", redirectToSinglePage("browseVolumes"))
      .put("issue", redirectWithIdParameter("browseIssues"))
      .put("comment", redirectWithIdParameter("articleCommentTree"))

      .put("article", redirectWithIdParameter("article"))
      .put("figure", redirectWithIdParameter("figurePage"))
      .put("table", redirectWithIdParameter("figurePage"))

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
    Link.Factory factory = Link.toForeignSite(site, typeInfo.journalKey, siteSet);
    return redirectFunction.getLink(factory, id);
  }

  @RequestMapping(name = "assetFile", value = "/article/file", params = {"type"})
  public void serveAssetFile(HttpServletResponse responseToClient,
                             @SiteParam Site site,
                             RequestedDoiVersion id,
                             @RequestParam(value = "type", required = true) String fileType,
                             @RequestParam(value = "download", required = false) String isDownload)
      throws IOException {
    AssetPointer asset = articleResolutionService.toParentIngestion(id);
    Map<String, ?> files = articleService.getItemFiles(asset);
    Map<String, ?> fileRepoKey = (Map<String, ?>) files.get(fileType);
    if (fileRepoKey == null) {
      String message = String.format("Unrecognized file type (\"%s\") for id: %s", fileType, id);
      throw new NotFoundException(message);
    }

    // TODO: Check visibility against site?

    ContentKey contentKey = createKey(fileRepoKey);
    try (CloseableHttpResponse responseFromApi = corpusContentApi.request(contentKey, ImmutableList.of())) {
      forwardAssetResponse(responseFromApi, responseToClient, booleanParameter(isDownload));
    }
  }

  private static ContentKey createKey(Map<String, ?> fileRepoKey) {
    // TODO: Account for bucket name
    String key = (String) fileRepoKey.get("crepoKey");
    UUID uuid = UUID.fromString((String) fileRepoKey.get("crepoUuid"));
    return ContentKey.createForUuid(key, uuid);
  }

}
