package org.ambraproject.wombat.service.remote;

import org.apache.http.conn.HttpHostConnectException;

import java.io.IOException;

public class ServiceConnectionException extends IOException {
  ServiceConnectionException(HttpHostConnectException e) {
    super("Could not connect to: " + e.getHost(), e);
  }
}
