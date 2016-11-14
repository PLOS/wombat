package org.ambraproject.wombat.service;

import org.ambraproject.wombat.service.remote.ArticleSearchQuery;
import org.ambraproject.wombat.service.remote.SolrSearchApi;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class DoiToJournalResolutionService {

  @Autowired
  private SolrSearchApi solrSearchApi;

  public String getJournalKeyFromDoi(String doi) throws IOException {
    ArticleSearchQuery explicitDoiSearchQuery = ArticleSearchQuery.builder()
        .setSimple(false)
        .setRows(1)
        .setQuery("id:\"" + doi + "\"")
        .build();

    Map<String, ?> results = solrSearchApi.search(explicitDoiSearchQuery);
    List<SolrArticleAdapter> solrArticleAdapters = SolrArticleAdapter.unpackSolrQuery(results);
    String journalKey = null;
    if (solrArticleAdapters.size() == 1) {
      journalKey = solrArticleAdapters.get(0).getJournalKey();
    }
    return journalKey;
  }
}


