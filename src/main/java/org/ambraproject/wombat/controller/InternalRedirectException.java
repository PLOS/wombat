package org.ambraproject.wombat.controller;

import org.ambraproject.wombat.config.site.url.Link;

import java.util.Objects;

/**
 * Indicates that we should redirect to an internal link instead of serving a page.
 */
class InternalRedirectException extends RuntimeException {

  private final Link link;

  /**
   * @param link the address to which to redirect
   */
  InternalRedirectException(Link link) {
    this.link = Objects.requireNonNull(link);
  }

  public Link getLink() {
    return link;
  }

}
