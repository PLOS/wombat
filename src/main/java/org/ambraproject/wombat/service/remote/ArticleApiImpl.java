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

import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;

public class ArticleApiImpl extends AbstractRestfulJsonApi implements ArticleApi {

  @Autowired
  private RuntimeConfiguration runtimeConfiguration;

  @Override
  public URL getServerUrl() {
    return runtimeConfiguration.getServer();
  }

  @Override
  protected String getCachePrefix() {
    return "article";
  }

  @Override
  public CloseableHttpResponse requestAsset(String assetId, Collection<? extends Header> headers)
      throws IOException {
    HttpGet get = buildGet(ApiAddress.builder("assetfiles").addToken(assetId).build());
    get.setHeaders(headers.toArray(new Header[headers.size()]));
    return remoteStreamer.getResponse(get);
  }

}
