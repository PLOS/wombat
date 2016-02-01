/*
 * Copyright (c) 2006-2013 by Public Library of Science
 *
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ambraproject.wombat.service;

import org.ambraproject.rhombat.cache.Cache;
import org.ambraproject.wombat.model.TaxonomyCountTable;
import org.ambraproject.wombat.model.TaxonomyGraph;
import org.ambraproject.wombat.service.remote.SolrSearchService;
import org.ambraproject.wombat.util.CacheParams;
import org.ambraproject.wombat.util.CacheUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Collection;

/**
 * {@inheritDoc}
 */
public class BrowseTaxonomyServiceImpl implements BrowseTaxonomyService {

  @Autowired
  private SolrSearchService solrSearchService;

  @Autowired
  private Cache cache;

  /**
   * {@inheritDoc}
   */
  public TaxonomyGraph parseCategories(final String journalKey)
    throws IOException {

    String cacheKey = "categories:" + CacheParams.createKeyHash(journalKey);
    return CacheUtil.getOrCompute(cache, cacheKey,
        () -> TaxonomyGraph.create(solrSearchService.getAllSubjects(journalKey)));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public TaxonomyCountTable getCounts(TaxonomyGraph taxonomy, String journalKey) throws IOException {
    String cacheKey = "categoryCount:" + CacheParams.createKeyHash(journalKey);
    return CacheUtil.getOrCompute(cache, cacheKey,
        () -> {
          Collection<SolrSearchService.SubjectCount> counts = solrSearchService.getAllSubjectCounts(journalKey);
          return new TaxonomyCountTable(counts);
        });
  }

}
