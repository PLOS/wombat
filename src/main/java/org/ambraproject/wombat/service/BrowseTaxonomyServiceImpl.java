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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import org.ambraproject.wombat.cache.Cache;
import org.ambraproject.wombat.model.TaxonomyGraph;
import org.ambraproject.wombat.service.remote.ArticleSearchQuery;
import org.ambraproject.wombat.service.remote.SolrSearchApi;
import org.ambraproject.wombat.util.CacheKey;
import org.ambraproject.wombat.util.CacheUtil;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * {@inheritDoc}
 */
public class BrowseTaxonomyServiceImpl implements BrowseTaxonomyService {

  @Autowired
  private SolrSearchApi solrSearchApi;

  @Autowired
  private Cache cache;

  /**
   * {@inheritDoc}
   */
  public TaxonomyGraph parseCategories(final String journalKey) throws IOException {

    CacheKey cacheKey = CacheKey.create("categories", journalKey);
    return CacheUtil
      .getOrCompute(cache, cacheKey, () -> {
          ArticleSearchQuery query = ArticleSearchQuery.builder()
            .setFacetFields(ImmutableList.of("subject_hierarchy"))
            .setFacetLimit(-1)
            .setJournalKeys(ImmutableList.of(journalKey))
            .setRows(0)
            .build();

          List<String> subjects = solrSearchApi.search(query)
            .getFacets()
            .get("subject_hierarchy")
            .keySet()
            .stream()
            .collect(Collectors.toList());
          return TaxonomyGraph.create(subjects);
        });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, Integer> getCounts(TaxonomyGraph taxonomy, String journalKey)
      throws IOException {
    CacheKey cacheKey = CacheKey.create("categoryCount", journalKey);
    return CacheUtil.getOrCompute(cache, cacheKey, () -> {
        ArticleSearchQuery query = ArticleSearchQuery.builder()
          .setFacetFields(ImmutableList.of("subject_facet"))
          .setFacetLimit(-1)
          .setJournalKeys(ImmutableList.of(journalKey))
          .setRows(0)
          .build();

        SolrSearchApi.Result results = solrSearchApi.search(query);
        ImmutableSortedMap.Builder<String, Integer> builder = ImmutableSortedMap.naturalOrder();
        builder.putAll(results.getFacets().get("subject_facet"));
        builder.put("ROOT", results.getNumFound());
        return builder.build();
      });
  }
}
