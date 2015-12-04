package org.ambraproject.wombat.service.remote;

import java.io.IOException;

/**
 * Indicates that a request from this webapp to the service component failed.
 */
public class ServiceRequestException extends IOException {

  private final int statusCode;
  private final String responseBody;

  ServiceRequestException(int statusCode, String message, String responseBody) {
    super(message);
    this.statusCode = statusCode;
    this.responseBody = responseBody;
  }

  ServiceRequestException(int statusCode, String message, Throwable cause, String responseBody) {
    super(message, cause);
    this.statusCode = statusCode;
    this.responseBody = responseBody;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public String getResponseBody() {
    return responseBody;
  }
}
