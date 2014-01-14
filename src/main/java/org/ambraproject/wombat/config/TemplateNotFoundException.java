package org.ambraproject.wombat.config;

public class TemplateNotFoundException extends RuntimeException {
  TemplateNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  TemplateNotFoundException(String message) {
    super(message);
  }
}
