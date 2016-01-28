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
import org.ambraproject.wombat.model.CategoryView;
import org.ambraproject.wombat.service.remote.SolrSearchService;
import org.ambraproject.wombat.util.CacheParams;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Serializable;
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
import java.util.function.Function;
import java.util.stream.Collectors;

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
    SortedMap<String, List<String>> categories = cache.get(cacheKey);
    if (categories == null) {
      categories = parseTopAndSecondLevelCategoriesWithoutCache(journalKey);
      cache.put(cacheKey, (Serializable) categories);
    }
    return categories;
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
  public CategoryView parseCategories(final String journalKey)
    throws IOException {

    String cacheKey = "categories:" + CacheParams.createKeyHash(journalKey);
    CategoryView categories;

    categories = cache.get(cacheKey); // remains null if not cached

    if (categories == null) {
      categories = parseCategoriesWithoutCache(journalKey);
      cache.put(cacheKey, categories);
    }
    return categories;
  }

  @SuppressWarnings("unchecked")
  private CategoryView parseCategoriesWithoutCache(String journalKey)
    throws IOException {

    List<String> subjects = solrSearchService.getAllSubjects(journalKey);

    return createMapFromStringList(subjects);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Collection<SolrSearchService.SubjectCount> getCounts(CategoryView taxonomy, String journalKey) throws IOException {
    Map<String, SolrSearchService.SubjectCount> counts = getAllCounts(journalKey);

    List<SolrSearchService.SubjectCount> subjectCounts = taxonomy.getChildren().values().stream()
        .map(categoryView -> counts.get(categoryView.getName())).collect(Collectors.toList());
    subjectCounts.add(counts.get(taxonomy.getName()));
    return subjectCounts;
  }

  /**
   * Returns article counts for a given journal for all subject terms in the taxonomy.
   * The results will be cached for CACHE_TTL.
   *
   * @param journalKey specifies the current journal
   * @return map from subject term to article count
   * @throws IOException
   */
  private Map<String, SolrSearchService.SubjectCount> getAllCounts(final String journalKey) throws IOException {

    String cacheKey = "categoryCount:" + CacheParams.createKeyHash(journalKey);
    Map<String, SolrSearchService.SubjectCount> counts;

    counts = cache.get(cacheKey); // remains null if not cached

    if (counts == null) {
      counts = getAllCountsWithoutCache(journalKey).stream()
          .collect(Collectors.toMap(SolrSearchService.SubjectCount::getCategory, Function.identity()));
      cache.put(cacheKey, (Serializable) counts);
    }
    return counts;
  }

  //todo: may need to get total article count here
  //EG counts.put(CategoryView.ROOT_NODE_NAME, subjectCounts.totalArticles);
  private Collection<SolrSearchService.SubjectCount> getAllCountsWithoutCache(String currentJournal) throws IOException {
    return solrSearchService.getAllSubjectCounts(currentJournal);
  }

  /**
   * Given a list of "/" delimited strings build a structured map
   *
   * @param categories list of Pairs wrapping the category name and article count
   *
   * @return a new treeMap
   */
  private static CategoryView createMapFromStringList(List<String> categories) {
    CategoryView root = new CategoryView("ROOT");

    // Since there can be multiple paths to the same child, we don't want to create the same
    // child more than once.  Hence the need for this map.
    Map<String, CategoryView> createdCategories = new HashMap<>();
    for (String category : categories) {
      if(category.charAt(0) == '/') {
        //Ignore first "/"
        root = recurseValues(root, category.substring(1).split("/"), 0, createdCategories);
      } else {
        root = recurseValues(root, category.split("/"), 0, createdCategories);
      }
    }

    return root;
  }

  private static CategoryView recurseValues(CategoryView root, String categories[], int index,
      Map<String, CategoryView> created) {
    CategoryView child = root.getChildren().get(categories[index]);

    if (child == null) {
      child = created.get(categories[index]);
      if (child == null) {
        child = new CategoryView(categories[index]);
        created.put(categories[index], child);
      }
      root.addChild(child);
    }

    if ((index + 1) < categories.length) { // path end
      recurseValues(child, categories, index + 1, created);
    }

    return root;
  }
}
