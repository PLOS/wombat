package org.ambraproject.wombat.service;

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

    OptionalInt revisionNumber = id.getRevisionNumber();
    if (revisionNumber.isPresent()) {
      Number ingestionForRevision = fetchRevisionTable(id).get(Integer.toString(revisionNumber.getAsInt()));
      if (ingestionForRevision == null) {
        String message = String.format("Article %s has no revision %d", id.getDoi(), revisionNumber.getAsInt());
        throw new NotFoundException(message);
      }
      return ingestionForRevision.intValue();
    } else {
      // Find the maximum revision number in the table and return its ingestion number
      return fetchRevisionTable(id).entrySet().stream()
          .max(Comparator.comparing(entry -> Integer.valueOf(entry.getKey())))
          .map(entry -> entry.getValue().intValue())
          .orElseThrow(() -> {
            String message = String.format("Article %s has no published revisions", id.getDoi());
            return new NotFoundException(message);
          });
    }
  }

  public ApiAddress.Builder toIngestion(ScholarlyWorkId id) {
    int ingestionId = resolveToIngestionNumber(id);
    return ApiAddress.builder("articles").embedDoi(id.getDoi())
        .addToken("ingestions").addToken(Integer.toString(ingestionId));
  }

}
