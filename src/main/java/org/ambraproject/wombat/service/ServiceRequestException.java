package org.ambraproject.wombat.service;

import java.io.IOException;

/**
 * Indicates that a request from this webapp to the service component failed.
 */
public class ServiceRequestException extends IOException {
  public ServiceRequestException(String message) {
    super(message);
  }

  public ServiceRequestException(String message, Throwable cause) {
    super(message, cause);
  }
}
