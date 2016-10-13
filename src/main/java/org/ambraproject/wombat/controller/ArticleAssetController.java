package org.ambraproject.wombat.controller;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.ambraproject.wombat.config.site.RequestMappingContextDictionary;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.identity.AssetPointer;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.ambraproject.wombat.service.ArticleResolutionService;
import org.ambraproject.wombat.service.ArticleService;
import org.ambraproject.wombat.service.remote.ContentKey;
import org.ambraproject.wombat.service.remote.CorpusContentApi;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.stream.Collectors;

@Controller
public class ArticleAssetController extends WombatController {

  @Autowired
  private CorpusContentApi corpusContentApi;
  @Autowired
  private ArticleResolutionService articleResolutionService;
  @Autowired
  private ArticleService articleService;
  @Autowired
  private RequestMappingContextDictionary requestMappingContextDictionary;

  private static enum AssetUrlStyle {
    FIGURE_IMAGE("figure", "table", "standaloneStrikingImage"),
    ASSET_FILE("article", "supplementaryMaterial", "graphic");

    private final ImmutableSet<String> itemTypes;

    private AssetUrlStyle(String... itemTypes) {
      this.itemTypes = ImmutableSet.copyOf(itemTypes);
    }

    public static final ImmutableMap<String, AssetUrlStyle> BY_ITEM_TYPE = ImmutableMap.copyOf(
        EnumSet.allOf(AssetUrlStyle.class).stream()
            .flatMap((AssetUrlStyle s) -> s.itemTypes.stream()
                .map((String itemType) -> Maps.immutableEntry(itemType, s)))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
  }


  private void serve(HttpServletRequest requestFromClient, HttpServletResponse responseToClient,
                     AssetUrlStyle requestedStyle, Site site, RequestedDoiVersion id,
                     String fileType, boolean isDownload)
      throws IOException {
    AssetPointer asset = articleResolutionService.toParentIngestion(id);

    Map<String, ?> itemMetadata = articleService.getItemMetadata(asset);
    String itemType = (String) itemMetadata.get("itemType");
    AssetUrlStyle itemStyle = Objects.requireNonNull(AssetUrlStyle.BY_ITEM_TYPE.get(itemType));
    if (requestedStyle != itemStyle) {
      throw new NotFoundException();
    }

    Map<String, ?> files = (Map<String, ?>) itemMetadata.get("files");
    Map<String, ?> fileRepoKey = (Map<String, ?>) files.get(fileType);
    if (fileRepoKey == null) {
      String message = String.format("Unrecognized file type (\"%s\") for id: %s", fileType, id);
      throw new NotFoundException(message);
    }

    // TODO: Check visibility against site?

    ContentKey contentKey = createKey(fileRepoKey);
    try (CloseableHttpResponse responseFromApi = corpusContentApi.request(contentKey, ImmutableList.of())) {
      forwardAssetResponse(responseFromApi, responseToClient, isDownload);
    }
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
                               @RequestParam(value = "size", required = true) String figureSize,
                               @RequestParam(value = "download", required = false) String isDownload)
      throws IOException {
    serve(request, response, AssetUrlStyle.FIGURE_IMAGE, site, id, figureSize, booleanParameter(isDownload));
  }

  private static ContentKey createKey(Map<String, ?> fileRepoKey) {
    // TODO: Account for bucket name
    String key = (String) fileRepoKey.get("crepoKey");
    UUID uuid = UUID.fromString((String) fileRepoKey.get("crepoUuid"));
    return ContentKey.createForUuid(key, uuid);
  }

}
