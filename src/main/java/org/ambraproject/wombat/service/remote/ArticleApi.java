/*
 * Copyright (c) 2017 Public Library of Science
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

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
public interface ArticleApi extends RestfulJsonApi {

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
