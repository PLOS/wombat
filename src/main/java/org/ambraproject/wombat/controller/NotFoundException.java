package org.ambraproject.wombat.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Indicates that the user requested an entity that does not exist, or didn't supply a required parameter (including
 * supplying an empty string as the parameter). Causes the user to see a 404 page.
 * <p>
 * Compare {@link org.ambraproject.wombat.service.EntityNotFoundException}, which is thrown by a service to indicate
 * that a persistent entity does not exist. Typically a service throwing {@code EntityNotFoundException} causes a
 * controller to rethrow {@code NotFoundException}.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {

  NotFoundException() {
  }

  public NotFoundException(Throwable cause) {
    super(cause);
  }

  public NotFoundException(String message) {
    super(message);
  }

  public NotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

}
