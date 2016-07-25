package org.ambraproject.wombat.service;

import com.google.common.collect.ImmutableSet;
import org.ambraproject.wombat.controller.NotFoundException;
import org.ambraproject.wombat.identity.ArticlePointer;
import org.ambraproject.wombat.identity.AssetPointer;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
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

  private static int resolveFromRevisionNumber(RequestedDoiVersion id, Map<String, Number> revisionTable) {
    OptionalInt revisionNumber = id.getRevisionNumber();
    if (revisionNumber.isPresent()) {
      Number ingestionForRevision = revisionTable.get(Integer.toString(revisionNumber.getAsInt()));
      if (ingestionForRevision == null) {
        String message = String.format("Article %s has no revision %d", id.getDoi(), revisionNumber.getAsInt());
        throw new NotFoundException(message);
      }
      return ingestionForRevision.intValue();
    } else {
      // Find the maximum revision number in the table and return its ingestion number
      return revisionTable.entrySet().stream()
          .max(Comparator.comparing(entry -> Integer.valueOf(entry.getKey())))
          .map(entry -> entry.getValue().intValue())
          .orElseThrow(() -> {
            String message = String.format("Article %s has no published revisions", id.getDoi());
            return new NotFoundException(message);
          });
    }
  }

  public ArticlePointer toIngestion(RequestedDoiVersion articleId) {
    Map<String, ?> articleOverview = fetchArticleOverview(articleId);
    String canonicalDoi = (String) articleOverview.get("doi");
    int ingestionNumber = articleId.getIngestionNumber().orElseGet(() -> {
      Map<String, Number> revisionTable = (Map<String, Number>) articleOverview.get("revisions");
      return resolveFromRevisionNumber(articleId, revisionTable);
    });
    return new ArticlePointer(canonicalDoi, ingestionNumber);
  }

  private static final ImmutableSet<String> ARTICLE_ASSET_TYPES = ImmutableSet.of("article", "asset");

  public AssetPointer toParentIngestion(RequestedDoiVersion assetId) throws IOException {
    Map<String, ?> doiOverview = articleApi.requestObject(
        ApiAddress.builder("dois").embedDoi(assetId.getDoi()).build(),
        Map.class);

    String type = (String) doiOverview.get("type");
    if (!ARTICLE_ASSET_TYPES.contains(type)) {
      throw new NotFoundException("Not an article asset: " + assetId);
    }

    Map<String, ?> parentArticle = (Map<String, ?>) doiOverview.get("article");
    String parentDoi = (String) parentArticle.get("doi");
    int ingestionNumber = assetId.getIngestionNumber().orElseGet(() -> {
      Map<String, Number> revisionTable = (Map<String, Number>) parentArticle.get("revisions");
      return resolveFromRevisionNumber(assetId, revisionTable);
    });
    ArticlePointer parentArticlePtr = new ArticlePointer(parentDoi, ingestionNumber);

    String canonicalAssetDoi = (String) doiOverview.get("doi");
    return new AssetPointer(canonicalAssetDoi, parentArticlePtr);
  }

}
