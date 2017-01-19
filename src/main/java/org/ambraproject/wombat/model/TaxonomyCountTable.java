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

package org.ambraproject.wombat.model;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Maps;
import org.ambraproject.wombat.service.remote.SolrSearchApi.SubjectCount;

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
