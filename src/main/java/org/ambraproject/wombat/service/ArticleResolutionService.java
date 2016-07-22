package org.ambraproject.wombat.service;

import com.google.common.collect.ImmutableSet;
import org.ambraproject.wombat.controller.NotFoundException;
import org.ambraproject.wombat.model.ScholarlyWorkId;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.OptionalInt;

public class ArticleResolutionService {

  @Autowired
  private ArticleApi articleApi;

  private Map<String, Number> fetchRevisionTable(ScholarlyWorkId id) {
    Map<String, ?> articleOverview;
    try {
      articleOverview = articleApi.requestObject(ApiAddress.builder("articles").embedDoi(id.getDoi()).build(), Map.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return (Map<String, Number>) articleOverview.get("revisions");
  }

  private int resolveToIngestionNumber(ScholarlyWorkId id) {
    OptionalInt ingestionNumber = id.getIngestionNumber();
    if (ingestionNumber.isPresent()) {
      return ingestionNumber.getAsInt();
    }

    return resolveFromRevisionNumber(id, fetchRevisionTable(id));
  }

  private static int resolveFromRevisionNumber(ScholarlyWorkId id, Map<String, Number> revisionTable) {
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

  private static ApiAddress.Builder toIngestion(String doi, int ingestionId) {
    return ApiAddress.builder("articles").embedDoi(doi)
        .addToken("ingestions").addToken(Integer.toString(ingestionId));
  }

  public ApiAddress.Builder toIngestion(ScholarlyWorkId articleId) {
    int ingestionId = resolveToIngestionNumber(articleId);
    return toIngestion(articleId.getDoi(), ingestionId);
  }

  private static final ImmutableSet<String> ARTICLE_ASSET_TYPES = ImmutableSet.of("article", "asset");

  public ApiAddress.Builder toParentIngestion(ScholarlyWorkId assetId) throws IOException {
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

    return toIngestion(parentDoi, ingestionNumber);
  }

}
