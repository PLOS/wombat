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
import java.util.Map;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.model.TaxonomyGraph;

/**
 * Provides services related to reading and browsing the subject category taxonomy.
 *
 * @author John Callaway
 * @author Joe Osowski
 */

public interface BrowseTaxonomyService {

  /**
   * For the current journal return a complete structured map of the taxonomic categories
   *
   * @param journalKey the current journal
   * @param site the current site
   * @return a complete structured map of the taxonomic categories
   */
  TaxonomyGraph parseCategories(String journalKey, Site site) throws IOException;

  /**
   * Returns the number of articles, for a given journal, associated with the parent term and all
   * direct children of a subject taxonomy term.
   *
   * @param taxonomy       the term in the taxonomy to examine, as well as its direct children
   * @param journalKey specifies the journal
   * @param site the current site
   * @return map from taxonomy term to count of articles
   */
  Map<String, Integer> getCounts(TaxonomyGraph taxonomy, String journalKey, Site site) throws IOException;

}
