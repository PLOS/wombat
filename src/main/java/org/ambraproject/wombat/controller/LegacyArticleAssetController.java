package org.ambraproject.wombat.controller;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.ambraproject.wombat.config.site.RequestMappingContextDictionary;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.config.site.url.Link;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.ambraproject.wombat.service.ApiAddress;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class LegacyArticleAssetController extends WombatController {
  private static final Logger log = LoggerFactory.getLogger(LegacyArticleAssetController.class);

  @Autowired
  private RequestMappingContextDictionary requestMappingContextDictionary;
  @Autowired
  private GeneralDoiController generalDoiController;
  @Autowired
  private ArticleApi articleApi;

  private static enum LegacyFileExtensionRedirectStrategy {
    ARTICLE("article") {
      private final ImmutableSortedMap<String, String> extensions = ImmutableSortedMap
          .<String, String>orderedBy(String.CASE_INSENSITIVE_ORDER)
          .put("XML", "manuscript")
          .put("PDF", "printable")
          .build();

      @Override
      protected String resolveToFileType(String fileExtension) {
        return resolveFromMap(extensions, fileExtension);
      }
    },

    FIGURE("figure", "table", "standaloneStrikingImage") {
      private final ImmutableSortedMap<String, String> extensions = ImmutableSortedMap
          .<String, String>orderedBy(String.CASE_INSENSITIVE_ORDER)
          .put("TIF", "original").put("TIFF", "original").put("GIF", "original")
          .put("PNG_S", "small")
          .put("PNG_I", "inline")
          .put("PNG_M", "medium")
          .put("PNG_L", "large")
          .build();

      @Override
      protected String resolveToFileType(String fileExtension) {
        return resolveFromMap(extensions, fileExtension);
      }
    },

    GRAPHIC("graphic") {
      private final ImmutableSortedMap<String, String> extensions = ImmutableSortedMap
          .<String, String>orderedBy(String.CASE_INSENSITIVE_ORDER)
          .put("TIF", "original").put("TIFF", "original").put("GIF", "original")
          .put("PNG", "thumbnail")
          .build();

      @Override
      protected String resolveToFileType(String fileExtension) {
        return resolveFromMap(extensions, fileExtension);
      }
    },

    SUPPLEMENTARY_MATERIAL("supplementaryMaterial") {
      @Override
      protected String resolveToFileType(String fileExtension) {
        return "supplementary";
      }
    };

    private static String resolveFromMap(Map<String, String> map, String fileExtension) {
      String fileType = map.get(fileExtension);
      if (fileType == null) throw new NotFoundException("Unrecognized file extension: " + fileExtension);
      return fileType;
    }

    private final ImmutableSet<String> associatedTypes;

    private LegacyFileExtensionRedirectStrategy(String... associatedTypes) {
      this.associatedTypes = ImmutableSet.copyOf(associatedTypes);
    }

    /**
     * @param fileExtension a legacy file extension
     * @return the equivalent file type
     */
    protected abstract String resolveToFileType(String fileExtension);

    private static final ImmutableMap<String, LegacyFileExtensionRedirectStrategy> STRATEGIES = ImmutableMap.copyOf(
        // Map each LegacyFileExtensionRedirectStrategy by its associated types.
        // A strategy with more than one type goes under more than one map key.
        EnumSet.allOf(LegacyFileExtensionRedirectStrategy.class).stream()
            .flatMap((LegacyFileExtensionRedirectStrategy strategy) -> strategy.associatedTypes.stream()
                .map((String associatedType) -> Maps.immutableEntry(associatedType, strategy)))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

    public static String resolveToFileType(String itemType, String fileExtension) {
      LegacyFileExtensionRedirectStrategy strategy = STRATEGIES.get(Objects.requireNonNull(itemType));
      if (strategy == null) {
        // Not NotFoundException; we expect to recognize any item type that the service returns.
        throw new RuntimeException("Unrecognized item type: " + itemType);
      }
      return strategy.resolveToFileType(Objects.requireNonNull(fileExtension));
    }
  }

  /**
   * Serve the identified asset file.
   *
   * @param rawId    an ID for an asset (if {@code unique} is present) or an asset file (if {@code unique} is absent)
   * @param unique   if present, assume the asset has a single file and serve that file; else, serve an identified file
   * @param download forward Content-Disposition headers with "attachment" value only if {@code true}
   */
  @RequestMapping(name = "asset", value = "/article/asset")
  public ModelAndView serveAsset(HttpServletRequest request,
                                 @SiteParam Site site,
                                 @RequestParam(value = "id", required = true) String rawId,
                                 @RequestParam(value = "unique", required = false) String unique,
                                 @RequestParam(value = "download", required = false) String download)
      throws IOException {
    requireNonemptyParameter(rawId);

    final String assetDoi;
    final Optional<String> fileExtension;
    if (booleanParameter(unique)) {
      assetDoi = rawId;
      fileExtension = Optional.empty();
    } else {
      int extensionIndex = rawId.lastIndexOf(".");
      fileExtension = (extensionIndex < 0) ? Optional.empty() : Optional.of(rawId.substring(extensionIndex + 1));
      assetDoi = (extensionIndex < 0) ? rawId : rawId.substring(0, extensionIndex);
    }

    Map<String, ?> itemMetadata = getItemMetadata(assetDoi);
    String itemType = (String) itemMetadata.get("itemType");

    final String fileType;
    if (fileExtension.isPresent()) {
      fileType = LegacyFileExtensionRedirectStrategy.resolveToFileType(itemType, fileExtension.get());
    } else {
      Map<String, ?> itemFiles = (Map<String, ?>) itemMetadata.get("files");
      Set<String> fileTypes = itemFiles.keySet();
      if (fileTypes.size() == 1) {
        fileType = Iterables.getOnlyElement(fileTypes);
      } else {
          /*
           * The user queried for the unique file of a non-unique asset. Because they might have manually punched in an
           * invalid URL, show a 404 page. Also log a warning in case it was caused by a buggy link.
           */
        log.warn("Received request for unique asset file with ID=\"{}\". More than one associated file type: {}",
            assetDoi, fileTypes);
        throw new NotFoundException();
      }
    }

    ArticleAssetController.AssetUrlStyle style = ArticleAssetController.AssetUrlStyle.findByItemType(itemType);
    Link redirectLink = style.buildRedirectLink(requestMappingContextDictionary, site,
        RequestedDoiVersion.of(assetDoi), fileType, booleanParameter(download));
    return new ModelAndView(redirectLink.getRedirect(request));
  }

  private Map<String, ?> getItemMetadata(String rawAssetDoi) throws IOException {
    Map<String, ?> assetMetadata = generalDoiController.getMetadataForDoi(RequestedDoiVersion.of(rawAssetDoi));
    Map<String, ?> article = (Map<String, ?>) assetMetadata.get("article");
    String articleDoi = (String) article.get("doi");

    Map<String, ?> revisions = (Map<String, ?>) article.get("revisions");
    Map.Entry<String, ?> latestRevisionEntry = revisions.entrySet().stream()
        .max(Comparator.comparing(entry -> Integer.valueOf(entry.getKey())))
        .orElseThrow(() -> new NotFoundException("Article is not published: " + articleDoi));
    int ingestionNumber = ((Number) latestRevisionEntry.getValue()).intValue();

    Map<String, ?> itemResponse = articleApi.requestObject(
        ApiAddress.builder("articles").embedDoi(articleDoi)
            .addToken("ingestions").addToken(Integer.toString(ingestionNumber))
            .addToken("items").build(),
        Map.class);
    Map<String, ?> itemTable = (Map<String, ?>) itemResponse.get("items");

    String canonicalAssetDoi = (String) assetMetadata.get("doi");
    return (Map<String, ?>) Objects.requireNonNull(itemTable.get(canonicalAssetDoi));
  }

}
