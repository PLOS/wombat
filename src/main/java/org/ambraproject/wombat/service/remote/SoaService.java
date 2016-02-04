package org.ambraproject.wombat.service.remote;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;

/**
 * A service for retrieving data from the SOA's RESTful server.
 * <p>
 * Each {@code address} argument for this service's methods must be formatted as a path to be appended to the root
 * server URL. For example, if the server is hosted at {@code http://example.com/api/v1/} and you want to send a request
 * to {@code http://example.com/api/v1/config}, then you would call {@code requestStream("config")}.
 */
public interface SoaService extends RestfulJsonService {

  /**
   * Return the host, port, and optionally part of the path.  For example "http://www.example.com/" or
   * "https://plos.org/api/"
   */
  public abstract URL getServerUrl();

  /**
   * Requests an asset, returning both the headers and stream.
   * <p>
   * The caller <em>must</em> either close the returned {@code CloseableHttpResponse} object or close the {@code
   * InputStream} to the response body. This is very important, because leaving responses hanging open can starve the
   * connection pool and cause horrible timeouts.
   *
   * @param assetId the asset ID within the SOA service's "assetfiles/" namespace
   * @return
   * @throws IOException
   */
  public abstract CloseableHttpResponse requestAsset(String assetId, Collection<? extends Header> headers) throws IOException;

}
