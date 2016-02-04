package org.ambraproject.wombat.model;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Maps;
import org.ambraproject.wombat.service.remote.SolrSearchService.SubjectCount;

import java.io.Serializable;
import java.util.Collection;

public class TaxonomyCountTable implements Serializable {

  private final ImmutableSortedMap<String, SubjectCount> counts; // case-insensitive

  public TaxonomyCountTable(Collection<SubjectCount> counts) {
    this.counts = ImmutableSortedMap.copyOf(Maps.uniqueIndex(counts, SubjectCount::getSubject),
        String.CASE_INSENSITIVE_ORDER);
  }

  public long getCount(String subjectName) {
    SubjectCount subjectCount = counts.get(subjectName);
    if (subjectCount == null) {
      throw new IllegalArgumentException();
    }
    return subjectCount.getCount();
  }

}
