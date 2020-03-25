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

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import org.ambraproject.wombat.util.CacheKey;
import org.ambraproject.wombat.util.UrlParamBuilder;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractContentApi {

  @Autowired
  private ArticleApi articleApi;
  @Autowired
  private RemoteService<InputStream> remoteStreamer;
  @Autowired
  private RemoteService<Reader> remoteReader;
  @Autowired
  protected Gson gson;

  public CloseableHttpResponse request(URI requestAddress, Collection<? extends Header> headers)
      throws IOException {
    HttpGet get = new HttpGet(requestAddress);
    get.setHeaders(headers.toArray(new Header[headers.size()]));
    return remoteStreamer.getResponse(get);
  }

  public String requestContent(URI uri) throws IOException {
    try (CloseableHttpResponse response = this.request(uri)) {
      return EntityUtils.toString(response.getEntity(), "UTF-8");
    }
  }

  public CloseableHttpResponse request(URI requestAddress) throws IOException {
    return request(requestAddress, ImmutableList.of());
  }

  protected Reader requestReader(URI uri) throws IOException {
    return remoteReader.request(new HttpGet(uri));
  }

  protected InputStream requestStream(URI uri) throws IOException {
    return remoteStreamer.request(new HttpGet(uri));
  }
}
