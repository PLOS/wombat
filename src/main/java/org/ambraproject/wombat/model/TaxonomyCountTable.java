package org.ambraproject.wombat.model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Objects;

public class TaxonomyCountTable {

  private final TaxonomyGraph taxonomyGraph;
  private final ImmutableMap<String, SubjectCount> counts;

  public TaxonomyCountTable(TaxonomyGraph taxonomyGraph, Collection<SubjectCount> counts) {
    this.taxonomyGraph = Objects.requireNonNull(taxonomyGraph);
    this.counts = Maps.uniqueIndex(counts, SubjectCount::getSubject);
  }

  /*
   * TODO: Provide views as needed
   */

}
