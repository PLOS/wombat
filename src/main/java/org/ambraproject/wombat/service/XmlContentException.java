package org.ambraproject.wombat.service;


public class XmlContentException extends RuntimeException {

    public XmlContentException() {
    }

    public XmlContentException(String message) {
      super(message);
    }

    public XmlContentException(String message, Throwable cause) {
      super(message, cause);
    }

    public XmlContentException(Throwable cause) {
      super(cause);
    }

}

