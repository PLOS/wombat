package org.ambraproject.wombat.service;

public class UnmatchedSiteException extends RuntimeException {

  public UnmatchedSiteException(String badSiteKey) {
    super("Key not matched to site: " + badSiteKey);
  }

}
