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
import java.util.Objects;
import java.util.Optional;
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

  public static final class RevisionPointer {
    private final int revisionNumber;
    private final int ingestionNumber;

    private RevisionPointer(int revisionNumber, int ingestionNumber) {
      this.revisionNumber = revisionNumber;
      this.ingestionNumber = ingestionNumber;
    }

    public int getRevisionNumber() {
      return revisionNumber;
    }

    public int getIngestionNumber() {
      return ingestionNumber;
    }

    @Override
    public boolean equals(Object o) {
      return this == o || o != null && getClass() == o.getClass()
          && revisionNumber == ((RevisionPointer) o).revisionNumber && ingestionNumber == ((RevisionPointer) o).ingestionNumber;
    }

    @Override
    public int hashCode() {
      return 31 * revisionNumber + ingestionNumber;
    }
  }

  /**
   * Find the latest revision from a table of revisions. The table generally comes from parsed JSON, which is why the
   * keys are strings.
   *
   * @param revisionTable a table from revision numbers to their ingestion numbers
   * @return the latest revision with the ingestion it points to, or empty if the the article is no revisions exist
   * because the article is unpublished
   */
  public static Optional<RevisionPointer> findLatestRevision(Map<String, ? extends Number> revisionTable) {
    return revisionTable.entrySet().stream()
        .map((Map.Entry<String, ? extends Number> entry) -> {
          int revisionNumber = Integer.parseInt(entry.getKey());
          int ingestionNumber = entry.getValue().intValue();
          return new RevisionPointer(revisionNumber, ingestionNumber);
        })
        .max(Comparator.comparing(RevisionPointer::getRevisionNumber));
  }

  private static ArticlePointer resolve(RequestedDoiVersion id, Map<String, ?> articleOverview) {
    final String canonicalDoi = (String) Objects.requireNonNull(articleOverview.get("doi"));
    final Map<String, Number> revisionTable = (Map<String, Number>) Objects.requireNonNull(articleOverview.get("revisions"));

    OptionalInt ingestionNumber = id.getIngestionNumber();
    if (ingestionNumber.isPresent()) {
      return new ArticlePointer(id, canonicalDoi, ingestionNumber.getAsInt(), OptionalInt.empty());
    }

    OptionalInt revisionNumber = id.getRevisionNumber();
    if (revisionNumber.isPresent()) {
      int revisionValue = revisionNumber.getAsInt();
      Number ingestionForRevision = revisionTable.get(Integer.toString(revisionValue));
      if (ingestionForRevision == null) {
        String message = String.format("Article %s has no revision %d", id.getDoi(), revisionValue);
        throw new NotFoundException(message);
      }
      return new ArticlePointer(id, canonicalDoi, ingestionForRevision.intValue(), OptionalInt.of(revisionValue));
    } else {
      RevisionPointer latestRevision = findLatestRevision(revisionTable)
          .orElseThrow(() -> {
            String message = String.format("Article %s has no published revisions", id.getDoi());
            return new NotFoundException(message);
          });
      return new ArticlePointer(id, canonicalDoi, latestRevision.getIngestionNumber(), OptionalInt.of(latestRevision.getRevisionNumber()));
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
