package org.ambraproject.wombat.service.remote;

import java.io.IOException;

/**
 * Indicates that a request from this webapp to the service component failed.
 */
public class ServiceRequestException extends IOException {

  private final int statusCode;

  ServiceRequestException(int statusCode, String message) {
    super(message);
    this.statusCode = statusCode;
  }

  ServiceRequestException(int statusCode, String message, Throwable cause) {
    super(message, cause);
    this.statusCode = statusCode;
  }

  public int getStatusCode() {
    return statusCode;
  }

}
