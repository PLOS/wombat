package org.ambraproject.wombat.controller;

/**
 * Indicates that, even though an article or related entity exists, the user should get a 404 status because the content
 * should not be accessible through the present request.
 */
public class NotVisibleException extends RuntimeException {
  public NotVisibleException() {
  }

  public NotVisibleException(String message) {
    super(message);
  }

  public NotVisibleException(String message, Throwable cause) {
    super(message, cause);
  }

  public NotVisibleException(Throwable cause) {
    super(cause);
  }
}
