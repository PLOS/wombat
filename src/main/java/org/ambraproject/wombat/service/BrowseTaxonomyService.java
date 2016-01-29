package org.ambraproject.wombat.service;

import org.ambraproject.wombat.model.TaxonomyCountTable;
import org.ambraproject.wombat.model.TaxonomyGraph;

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
   * @param journalKey The current journal
   * @return map with keys of (sorted) top-level categories
   */
  Map<String, List<String>> parseTopAndSecondLevelCategories(String journalKey) throws IOException;

  /**
   * For the current journal return a complete structured map of the taxonomic categories
   *
   * @param journalKey the current journal
   * @return a complete structured map of the taxonomic categories
   */
  TaxonomyGraph parseCategories(String journalKey) throws IOException;

  /**
   * Returns the number of articles, for a given journal, associated with the parent term and all
   * direct children of a subject taxonomy term.
   *
   * @param taxonomy       the term in the taxonomy to examine, as well as its direct children
   * @param journalKey specifies the journal
   * @return map from taxonomy term to count of articles
   */
  TaxonomyCountTable getCounts(TaxonomyGraph taxonomy, String journalKey) throws IOException;

}