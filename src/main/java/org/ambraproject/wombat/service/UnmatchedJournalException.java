package org.ambraproject.wombat.service;

public class UnmatchedJournalException extends RuntimeException {

  public UnmatchedJournalException(String badJournalKey) {
    super("Key not matched to journal: " + badJournalKey);
  }

}
