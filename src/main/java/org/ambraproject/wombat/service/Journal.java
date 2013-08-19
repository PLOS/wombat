/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2013 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.wombat.service;

import com.google.common.collect.ImmutableMap;

import java.util.EnumSet;
import java.util.Map;

/**
 * Enumerates current PLOS journals.
 */
public enum Journal {

  PLOS_ONE("PlosOne", "PLoSONE"),
  PLOS_BIOLOGY("PlosBiology", "PLoSBiology"),
  PLOS_MEDICINE("PlosMedicine", "PLoSMedicine"),
  PLOS_COMP_BIOL("PlosCompBiol", "PLoSCompBiol"),
  PLOS_GENETICS("PlosGenetics", "PLoSGenetics"),
  PLOS_PATHOGENS("PlosPathogens", "PLoSPathogens"),
  PLOS_NTDS("PlosNtds", "PLoSNTD"),
  PLOS_COLLECTIONS("PlosCollections", "PLoSCollections");

  private static Map<String, Journal> PATH_TO_INSTANCE;
  static {
    ImmutableMap.Builder<String, Journal> builder = ImmutableMap.builder();
    for (Journal journal : EnumSet.allOf(Journal.class)) {
      builder.put(journal.getPathName(), journal);
    }
    PATH_TO_INSTANCE = builder.build();
  }

  private String pathName;
  private String solrName;

  /**
   * Constructor.
   *
   * @param pathName value used for this journal in wombat URL paths
   * @param solrName value used for this journal in SOLR search
   */
  Journal(String pathName, String solrName) {
    this.pathName = pathName;
    this.solrName = solrName;
  }

  /**
   * Attempts to retrieve a Journal based on the pathName property.
   *
   * @param pathName journal name from the URL path
   * @return Journal instance
   * @throws IllegalArgumentException if the pathName does not correspond to a Journal
   */
  public static Journal fromPathName(String pathName) {
    Journal journal = PATH_TO_INSTANCE.get(pathName);
    if (journal == null) {
      throw new IllegalArgumentException("Unknown journal " + pathName);
    }
    return journal;
  }

  /**
   * @return the identifier used in wombat URL paths for this Journal
   */
  public String getPathName() {
    return pathName;
  }

  /**
   * @return the name used by SOLR search to restrict results to articles from this Journal
   */
  public String getSolrName() {
    return solrName;
  }
}
