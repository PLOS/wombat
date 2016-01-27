package org.ambraproject.wombat.service;

import com.google.common.base.Optional;
import org.ambraproject.wombat.model.CategoryView;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Provides services related to reading and browsing the subject category taxonomy.
 *
 * @author John Callaway
 * @author Joe Osowski
 */

public interface BrowseTaxonomyService {

  /**
   * Parses a list of slash-delimited categories, as returned by solr, into a sorted map
   * from top-level category to a list of second-level categories.
   *
   * @param currentJournal The current journal
   * @param cacheTtl       The time, in seconds, to cache the results of a query to the service tier.
   *                       If absent, don't read or add to the cache and always check for new data.
   * @return map with keys of (sorted) top-level categories
   */
  Map<String, List<String>> parseTopAndSecondLevelCategories(String currentJournal, Optional<Integer> cacheTtl) throws IOException;

  /**
   * For the current journal return a complete structured map of the taxonomic categories
   *
   * @param currentJournal the current journal
   * @param cacheTtl       The time, in seconds, to cache the results of a query to the service tier.
   *                       If absent, don't read or add to the cache and always check for new data.
   * @return a complete structured map of the taxonomic categories
   */
  CategoryView parseCategories(String currentJournal, Optional<Integer> cacheTtl) throws IOException;

  /**
   * Returns the number of articles, for a given journal, associated with the parent term and all
   * direct children of a subject taxonomy term.
   *
   * @param taxonomy       the term in the taxonomy to examine, as well as its direct children
   * @param currentJournal specifies the journal
   * @param cacheTtl       The time, in seconds, to cache the results of a query to the service tier.
   *                       If absent, don't read or add to the cache and always check for new data.
   * @return map from taxonomy term to count of articles
   */
  Map<String, Long> getCounts(CategoryView taxonomy, String currentJournal, Optional<Integer> cacheTtl) throws IOException;
}