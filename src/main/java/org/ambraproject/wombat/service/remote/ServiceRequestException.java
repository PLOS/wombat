package org.ambraproject.wombat.service.remote;

import java.io.IOException;

/**
 * Indicates that a request from this webapp to the service component failed.
 */
public class ServiceRequestException extends IOException {
  ServiceRequestException(String message) {
    super(message);
  }

  ServiceRequestException(String message, Throwable cause) {
    super(message, cause);
  }
}
