package org.ambraproject.wombat.service.remote;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;

/**
 * Superclass for service beans that make calls to remote APIs.
 */
abstract class AbstractRemoteService<S extends Closeable> implements RemoteService<S> {
  private static final Logger log = LoggerFactory.getLogger(AbstractRemoteService.class);

  private final Optional<HttpClientConnectionManager> connectionManager;

  protected AbstractRemoteService(HttpClientConnectionManager connectionManager) {
    this.connectionManager = Optional.fromNullable(connectionManager);
  }

  private CloseableHttpClient createClient() {
    HttpClientBuilder clientBuilder = HttpClientBuilder.create();
    if (connectionManager.isPresent()) {
      clientBuilder = clientBuilder.setConnectionManager(connectionManager.get());
    }
    return clientBuilder.build();
  }

  /**
   * Makes a request to the given URI, checks for response codes 400 or above, and returns the response.
   * <p/>
   * The caller <em>must</em> either close the returned {@code CloseableHttpResponse} object or close the {@code
   * InputStream} to the response body (i.e., {@code CloseableHttpResponse.getEntity().getContent()}). This is very
   * important, because leaving responses hanging open can starve the connection pool and cause horrible timeouts.
   *
   * @param targetUri the URI to which to send the request
   * @param headers   headers to add to the request, if any
   * @return response from the server
   * @throws IOException                                             if there is an error connecting to the server
   * @throws NullPointerException                                    if the address is null
   * @throws org.ambraproject.wombat.service.EntityNotFoundException if the object at the address does not exist
   */
  @Override
  public CloseableHttpResponse getResponse(URI targetUri, Header... headers) throws IOException {
    // Don't close the client, as this shuts down the connection pool. Do close every response or its entity stream.
    CloseableHttpClient client = createClient();
    HttpGet get = new HttpGet(targetUri);
    for (Header header : headers) {
      get.setHeader(header);
    }

    // We want to return an unclosed response, so close the response only if we throw an exception.
    boolean returningResponse = false;
    CloseableHttpResponse response = null;
    try {
      response = client.execute(get);
      StatusLine statusLine = response.getStatusLine();
      if (statusLine.getStatusCode() >= 400) {
        String address = targetUri.getPath();
        if (!Strings.isNullOrEmpty(targetUri.getQuery())) {
          address += "?" + targetUri.getQuery();
        }
        if (statusLine.getStatusCode() == HttpStatus.NOT_FOUND.value()) {
          throw new EntityNotFoundException(address);
        } else {
          String message = String.format("Request to \"%s\" failed (%d): %s",
              address, statusLine.getStatusCode(), statusLine.getReasonPhrase());
          throw new ServiceRequestException(message);
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

  @Override
  public S request(URI targetUri) throws IOException {
    return open(requestEntity(targetUri));
  }

  /**
   * Get a response and open the response entity.
   *
   * @param targetUri the URI from which to request a response
   * @return the response entity
   * @throws IOException      on making the request
   * @throws RuntimeException if a response is received but it has no response entity object
   * @see org.apache.http.HttpResponse#getEntity()
   */
  private HttpEntity requestEntity(URI targetUri) throws IOException {
    Preconditions.checkNotNull(targetUri);

    // We must leave the response open if we return a valid stream, but must close it otherwise.
    boolean returningResponse = false;
    CloseableHttpResponse response = null;
    try {
      response = getResponse(targetUri);
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
