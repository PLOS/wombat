package org.ambraproject.wombat.controller;

import com.google.common.base.Strings;
import org.ambraproject.wombat.service.remote.SoaRequest;
import org.ambraproject.wombat.service.remote.SoaService;
import org.ambraproject.wombat.util.RevisionId;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

class ArticleSpaceController extends WombatController {

  @Autowired
  protected SoaService soaService;

  protected SortedSet<Integer> getRevisionNumbers(String articleId) throws IOException {
    SortedSet<Integer> revisionNumbers = new TreeSet<>();
    Collection<Map<?, ?>> revisionData = soaService.requestObject(
        SoaRequest.request("articles/revisions").addParameter("id", articleId).build(),
        Collection.class);
    for (Map<?, ?> revisionObj : revisionData) {
      Collection<?> revisionList = (Collection<?>) revisionObj.get("revisionNumbers");
      for (Object revisionValue : revisionList) {
        revisionNumbers.add(((Number) revisionValue).intValue());
      }
    }
    return revisionNumbers;
  }

  protected RevisionId parseRevision(String articleId, String revision) throws IOException {
    final int revisionValue;
    if (Strings.isNullOrEmpty(revision)) {
      revisionValue = getRevisionNumbers(articleId).last(); // TODO What if it's empty?
    } else {
      try {
        revisionValue = Integer.parseInt(revision);
      } catch (NumberFormatException e) {
        throw new NotFoundException("revisionNumber is not a number", e);
      }

      if (!isValidRevisionNumber(revisionValue)) {
        throw new NotFoundException("revisionNumber is not a valid number");
      }
    }
    return RevisionId.create(articleId, revisionValue);
  }

  private static boolean isValidRevisionNumber(int revisionNumber) {
    // Subject to change? It's a UX concern whether these are numbered from 0 or 1.
    return revisionNumber > 0;
  }

}
