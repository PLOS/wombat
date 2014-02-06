package org.ambraproject.wombat.service;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Calendar;

/**
 * A service for retrieving data from the SOA's RESTful server.
 * <p/>
 * Each {@code address} argument for this service's methods must be formatted as a path to be appended to the root
 * server URL. For example, if the server is hosted at {@code http://example.com/api/v1/} and you want to send a request
 * to {@code http://example.com/api/v1/config}, then you would call {@code requestStream("config")}.
 */
public interface SoaService {

  /**
   * Send a REST request and open a stream as the response.
   *
   * @param address the path to which to send the REST request
   * @return a stream to the response
   * @throws IOException             if there is an error connecting to the server
   * @throws NullPointerException    if the address is null
   * @throws EntityNotFoundException if the object at the address does not exist
   */
  public abstract InputStream requestStream(String address) throws IOException;

  /**
   * Send a REST request and serialize the response to an object. The serialization is controlled by the {@link
   * org.ambraproject.wombat.config.SpringConfiguration#gson()} bean.
   *
   * @param address       the path to which to send the REST request
   * @param responseClass the object type into which to serialize the JSON response
   * @param <T>           the type of {@code responseClass}
   * @return the response, serialized from JSON into an object
   * @throws IOException             if there is an error connecting to the server
   * @throws NullPointerException    if either argument is null
   * @throws EntityNotFoundException if the object at the address does not exist
   */
  public abstract <T> T requestObject(String address, Class<T> responseClass) throws IOException;

  /**
   * Simple wrapper around a timestamp and an object for the return type of requestObjectIfModifiedSince.
   *
   * @param <T>
   */
  public static class IfModifiedSinceResult<T> implements Serializable {

    /**
     * The result of the lookup to the SOA service.
     */
    public T result;

    /**
     * The last modification time for the object returned by the SOA service.
     */
    public Calendar lastModified;
  }

  /**
   * Requests an object, using the "If-Modified-Since" header in the request so that the object
   * will only be returned if it was modified after the given time.  Otherwise, the result field
   * of the return type will be null.  This is useful when results from the SOA service are
   * being added to a cache, and we only want to retrieve the result if it is newer than the
   * version stored in the cache.
   *
   * @param address the path to which to send the REST request
   * @param responseClass the object type into which to serialize the JSON response
   * @param lastModified the object will be returned iff the SOA server indicates that
   *     it was modified after this timestamp
   * @param <T> the type of {@code responseClass}
   * @return an instance of {@link IfModifiedSinceResult}
   * @throws IOException
   */
  public abstract <T> IfModifiedSinceResult<T> requestObjectIfModifiedSince(String address, Class<T> responseClass,
      Calendar lastModified) throws IOException;

  /**
   * Requests an asset, returning both the headers and stream.
   *
   * @param assetId the asset ID within the SOA service's "assetfiles/" namespace
   * @return
   * @throws IOException
   */
  public abstract HttpResponse requestAsset(String assetId, Header... headers) throws IOException;

}
