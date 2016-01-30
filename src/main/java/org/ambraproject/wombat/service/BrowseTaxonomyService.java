package org.ambraproject.wombat.service;

import org.ambraproject.wombat.model.CategoryView;
import org.ambraproject.wombat.service.remote.SolrSearchService;

import java.io.IOException;
import java.util.Collection;
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
  CategoryView parseCategories(String journalKey) throws IOException;

  /**
   * Returns the number of articles, for a given journal, associated with the parent term and all
   * direct children of a subject taxonomy term.
   *
   * @param taxonomy       the term in the taxonomy to examine, as well as its direct children
   * @param journalKey specifies the journal
   * @return map from taxonomy term to count of articles
   */
  Collection<SolrSearchService.SubjectCount> getCounts(CategoryView taxonomy, String journalKey) throws IOException;

  /**
   * For the passed in category, find the matching CategoryView.  This will come in handy when looking for
   * getting the correctly formatted case corrected category name
   *
   * @param parentCategoryView the categoryView to search from
   * @param category the string of the category to search for
   *
   * @return The first matching category view
   *
   */
  CategoryView findCategory(CategoryView parentCategoryView, String category) throws IOException;
}