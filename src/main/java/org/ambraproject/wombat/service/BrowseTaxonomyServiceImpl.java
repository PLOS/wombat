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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

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
  public SortedMap<String, List<String>> parseTopAndSecondLevelCategories(final String journalKey)
    throws IOException {
    String cacheKey = "topAndSecondLevelCategories:" + CacheParams.createKeyHash(journalKey);
    return CacheUtil.getOrCompute(cache, cacheKey,
        () -> parseTopAndSecondLevelCategoriesWithoutCache(journalKey));
  }

  private SortedMap<String, List<String>> parseTopAndSecondLevelCategoriesWithoutCache(String currentJournal)
    throws IOException {
    List<String> fullCategoryPaths = solrSearchService.getAllSubjects(currentJournal);

    // Since there are lots of duplicates, we start by adding the second-level
    // categories to a Set instead of a List.
    Map<String, Set<String >> map = new HashMap<>();
    for (String category : fullCategoryPaths) {

      // If the category doesn't start with a slash, it's one of the old-style
      // categories where we didn't store the full path.  Ignore these.
      if (category.charAt(0) == '/') {
        String[] fields = category.split("/");
        if (fields.length >= 3) {
          Set<String> subCats = map.get(fields[1]);
          if (subCats == null) {
            subCats = new HashSet<>();
          }
          subCats.add(fields[2]);
          map.put(fields[1], subCats);
        }
      }
    }

    // Now sort all the subcategory lists, and add them to the result.
    SortedMap<String, List<String>> results = new TreeMap<>();
    for (Map.Entry<String, Set<String>> entry : map.entrySet()) {
      List<String> subCatList = new ArrayList<>(entry.getValue());
      Collections.sort(subCatList);
      results.put(entry.getKey(), subCatList);
    }
    return results;
  }

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
    Collection<SolrSearchService.SubjectCount> counts = CacheUtil.getOrCompute(cache, cacheKey,
        () -> solrSearchService.getAllSubjectCounts(journalKey));
    return new TaxonomyCountTable(counts);
  }

}
