package org.ambraproject.wombat.config;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

/**
 * Simple wrapper around a map from journal keys to journal objects, for use as a Spring bean.
 */
public class JournalSet {

  private final ImmutableMap<String, Journal> journalMap;

  private JournalSet(Iterable<Journal> journals) {
    ImmutableMap.Builder<String, Journal> journalMap = ImmutableMap.builder();
    for (Journal journal : journals) {
      journalMap.put(journal.getKey(), journal);
    }
    this.journalMap = journalMap.build();
  }

  public static JournalSet create(Map<String, ? extends Theme> themesForJournals) {
    List<Journal> journals = Lists.newArrayListWithCapacity(themesForJournals.size());
    for (Map.Entry<String, ? extends Theme> entry : themesForJournals.entrySet()) {
      String key = entry.getKey();
      Theme theme = entry.getValue();
      Journal journal = new Journal(key, theme);
      journals.add(journal);
    }
    return new JournalSet(journals);
  }




  public Journal getJournal(String journalKey) {
    Journal journal = journalMap.get(journalKey);
    if (journal == null) {
      throw new IllegalArgumentException("Not matched to a journal: " + journalKey);
    }
    return journal;
  }

  public ImmutableCollection<Journal> getJournals() {
    return journalMap.values();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    return journalMap.equals(((JournalSet) o).journalMap);
  }

  @Override
  public int hashCode() {
    return journalMap.hashCode();
  }

}
