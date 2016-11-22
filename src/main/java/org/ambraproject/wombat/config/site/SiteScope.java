package org.ambraproject.wombat.config.site;

/**
 * Descriptors for request handlers that describe which types of {@link Site} objects may be resolved for a given
 * request handler.
 *
 * @see SiteResolver
 * @see RequestMappingContext
 */
public enum SiteScope {

  /**
   * Requests resolve to sites that belong to a journal.
   */
  JOURNAL_SPECIFIC,

  /**
   * Requests resolve to sites that do not belong to a journal.
   */
  JOURNAL_NEUTRAL,

  /**
   * Requests do not resolve to sites.
   */
  SITELESS;

}
