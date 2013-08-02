package org.ambraproject.wombat.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.Map;

/**
 * Simple wrapper around a map from journal keys to theme objects, for use as a Spring bean.
 */
public class JournalThemeMap {

  private final ImmutableMap<String, Theme> themesForJournals;

  public JournalThemeMap(Map<String, ? extends Theme> themesForJournals) {
    this.themesForJournals = ImmutableMap.copyOf(themesForJournals);
  }

  public Theme getTheme(String journalKey) {
    return themesForJournals.get(journalKey); // TODO: Throw exception instead of returning null?
  }

  public ImmutableSet<String> getJournalKeys() {
    return themesForJournals.keySet();
  }

  public ImmutableSet<Map.Entry<String, Theme>> asEntrySet() {
    return themesForJournals.entrySet();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    return themesForJournals.equals(((JournalThemeMap) o).themesForJournals);
  }

  @Override
  public int hashCode() {
    return themesForJournals.hashCode();
  }

}
