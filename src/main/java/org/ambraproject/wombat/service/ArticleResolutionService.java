package org.ambraproject.wombat.service;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.ambraproject.wombat.controller.NotFoundException;
import org.ambraproject.wombat.identity.ArticlePointer;
import org.ambraproject.wombat.identity.AssetPointer;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;

public class ArticleResolutionService {

  @Autowired
  private ArticleApi articleApi;

  private Map<String, ?> fetchArticleOverview(RequestedDoiVersion id) {
    try {
      return articleApi.requestObject(ApiAddress.builder("articles").embedDoi(id.getDoi()).build(), Map.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static ArticlePointer resolve(RequestedDoiVersion id, Map<String, ?> articleOverview) {
    final String canonicalDoi = (String) Objects.requireNonNull(articleOverview.get("doi"));
    final Map<String, Number> revisionTable = (Map<String, Number>) Objects.requireNonNull(articleOverview.get("revisions"));

    OptionalInt ingestionNumber = id.getIngestionNumber();
    if (ingestionNumber.isPresent()) {
      return new ArticlePointer(canonicalDoi, ingestionNumber.getAsInt(), OptionalInt.empty());
    }

    OptionalInt revisionNumber = id.getRevisionNumber();
    if (revisionNumber.isPresent()) {
      int revisionValue = revisionNumber.getAsInt();
      Number ingestionForRevision = revisionTable.get(Integer.toString(revisionValue));
      if (ingestionForRevision == null) {
        String message = String.format("Article %s has no revision %d", id.getDoi(), revisionValue);
        throw new NotFoundException(message);
      }
      return new ArticlePointer(canonicalDoi, ingestionForRevision.intValue(), OptionalInt.of(revisionValue));
    } else {
      // Find the maximum revision number in the table
      Map.Entry<Integer, Integer> maxRevisionEntry = revisionTable.entrySet().stream()
          .map((Map.Entry<String, Number> entry) ->
              Maps.immutableEntry(Integer.valueOf(entry.getKey()), entry.getValue().intValue()))
          .max(Comparator.comparing(Map.Entry::getKey))
          .orElseThrow(() -> {
            String message = String.format("Article %s has no published revisions", id.getDoi());
            return new NotFoundException(message);
          });
      return new ArticlePointer(canonicalDoi, maxRevisionEntry.getValue(), OptionalInt.of(maxRevisionEntry.getKey()));
    }
  }

  public ArticlePointer toIngestion(RequestedDoiVersion articleId) {
    return resolve(articleId, fetchArticleOverview(articleId));
  }

  private static final ImmutableSet<String> ARTICLE_ASSET_TYPES = ImmutableSet.of("article", "asset");

  public AssetPointer toParentIngestion(RequestedDoiVersion assetId) throws IOException {
    Map<String, ?> doiOverview;
    try {
      doiOverview = articleApi.requestObject(
          ApiAddress.builder("dois").embedDoi(assetId.getDoi()).build(),
          Map.class);
    } catch (EntityNotFoundException e) {
      throw new NotFoundException(e);
    }

    String type = (String) doiOverview.get("type");
    if (!ARTICLE_ASSET_TYPES.contains(type)) {
      throw new NotFoundException("Not an article asset: " + assetId);
    }

    Map<String, ?> parentArticle = (Map<String, ?>) doiOverview.get("article");
    ArticlePointer parentArticlePtr = resolve(assetId, parentArticle);

    String canonicalAssetDoi = (String) doiOverview.get("doi");
    return new AssetPointer(canonicalAssetDoi, parentArticlePtr);
  }

}
