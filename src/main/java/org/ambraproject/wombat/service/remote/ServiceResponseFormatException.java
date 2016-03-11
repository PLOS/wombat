package org.ambraproject.wombat.service.remote;

/**
 * Indicates that a remote service provided a response in an unexpected or invalid format.
 */
public class ServiceResponseFormatException extends RuntimeException {
  ServiceResponseFormatException(String message) {
    super(message);
  }

  ServiceResponseFormatException(String message, Throwable cause) {
    super(message, cause);
  }
}
