package org.ambraproject.wombat.service.remote;

import com.google.common.base.Strings;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;

/**
 * Superclass for service beans that make calls to remote APIs.
 */
abstract class AbstractRemoteService<S extends Closeable> implements RemoteService<S> {
  private static final Logger log = LoggerFactory.getLogger(AbstractRemoteService.class);

  private final Optional<HttpClientConnectionManager> connectionManager;

  protected AbstractRemoteService(HttpClientConnectionManager connectionManager) {
    this.connectionManager = Optional.ofNullable(connectionManager);
  }

  private CloseableHttpClient createClient() {
    HttpClientBuilder clientBuilder = HttpClientBuilder.create();
    if (connectionManager.isPresent()) {
      clientBuilder = clientBuilder.setConnectionManager(connectionManager.get());
    }
    return clientBuilder.build();
  }

  @Override
  public CloseableHttpResponse getResponse(HttpUriRequest target) throws IOException {
    // Don't close the client, as this shuts down the connection pool. Do close every response or its entity stream.
    CloseableHttpClient client = createClient();

    // We want to return an unclosed response, so close the response only if we throw an exception.
    boolean returningResponse = false;
    CloseableHttpResponse response = null;
    try {
      response = client.execute(target);
      StatusLine statusLine = response.getStatusLine();
      int statusCode = statusLine.getStatusCode();
      if (isErrorStatus(statusCode)) {
        URI targetUri = target.getURI();
        String address = targetUri.getPath();
        if (!Strings.isNullOrEmpty(targetUri.getQuery())) {
          address += "?" + targetUri.getQuery();
        }
        if (statusCode == HttpStatus.NOT_FOUND.value()) {
          throw new EntityNotFoundException(address);
        } else {
          String message = String.format("Request to \"%s\" failed (%d): %s",
              address, statusCode, statusLine.getReasonPhrase());
          throw new ServiceRequestException(statusCode, message);
        }
      }
      returningResponse = true;
      return response;
    } finally {
      if (!returningResponse && response != null) {
        response.close();
      }
    }
  }

  private static boolean isErrorStatus(int statusCode) {
    HttpStatus.Series series = HttpStatus.Series.valueOf(statusCode);
    return (series == HttpStatus.Series.CLIENT_ERROR) || (series == HttpStatus.Series.SERVER_ERROR);
  }

  @Override
  public S request(HttpUriRequest target) throws IOException {
    return open(requestEntity(target));
  }

  /**
   * Get a response and open the response entity.
   *
   * @param target the request to send
   * @return the response entity
   * @throws IOException      on making the request
   * @throws RuntimeException if a response is received but it has no response entity object
   * @see org.apache.http.HttpResponse#getEntity()
   */
  private HttpEntity requestEntity(HttpUriRequest target) throws IOException {
    Objects.requireNonNull(target);

    // We must leave the response open if we return a valid stream, but must close it otherwise.
    boolean returningResponse = false;
    CloseableHttpResponse response = null;
    try {
      response = getResponse(target);
      HttpEntity entity = response.getEntity();
      if (entity == null) {
        throw new RuntimeException("No response");
      }
      returningResponse = true;
      return entity;
    } finally {
      if (!returningResponse && response != null) {
        response.close();
      }
    }
  }

}
