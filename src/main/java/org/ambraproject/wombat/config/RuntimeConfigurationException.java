package org.ambraproject.wombat.config;

public class RuntimeConfigurationException extends RuntimeException {

  public RuntimeConfigurationException(String message) {
    super(message);
  }

  public RuntimeConfigurationException(String message, Throwable cause) {
    super(message, cause);
  }

}
