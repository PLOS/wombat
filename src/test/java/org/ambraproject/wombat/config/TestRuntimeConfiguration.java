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

package org.ambraproject.wombat.config;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * Instance of {@link RuntimeConfiguration} suitable for tests.  Many of the return values here will be null or
 * meaningless defaults; we can fill them in with "real" values as tests need them.
 */
public class TestRuntimeConfiguration implements RuntimeConfiguration {

  public static final String SERVER_URL = "http://www.example.com/";

  @Override
  public URL getRhinoUrl() {
    try {
      final URL serverUrl = new URL(SERVER_URL);
      return serverUrl;
    } catch (MalformedURLException exception) {
      return null;
    }
  }

  @Override
  public boolean showDebug() {
    return false;
  }

  @Override
  public String getRootRedirect() {
    return null;
  }

  @Override
  public String getThemePath() {
    return null;
  }

  @Override
  public String getMemcachedServer() {
    return null;
  }

  @Override
  public String getCasUrl() {
    return null;
  }

  @Override
  public URL getSolrUrl() {
    return null;
  }

  @Override
  public URI getNedUrl() {
    return null;
  }

  @Override
  public URL getCollectionsUrl() {
    return null;
  }
}
