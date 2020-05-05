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

package org.ambraproject.wombat.util;

import java.util.List;
import java.util.Map;
import com.google.common.collect.ImmutableList;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.util.MultiValueMap;

/**
 * Builder pattern applied as a convenience over
 * {@link org.apache.http.client.utils.URLEncodedUtils}.
 */
public class UrlParamBuilder {

  ImmutableList.Builder<NameValuePair> builder;

  public UrlParamBuilder() {
    this.builder = new ImmutableList.Builder<NameValuePair>();
  }

  public ImmutableList<NameValuePair> build() {
    return this.builder.build();
  }

  public static UrlParamBuilder params() {
    return new UrlParamBuilder();
  }

  public UrlParamBuilder add(String name, String value) {
    this.builder.add(new BasicNameValuePair(name, value));
    return this;

  }

  public UrlParamBuilder addAll(MultiValueMap<String, String> params) {
    for (Map.Entry<String, List<String>> entry : params.entrySet()) {
      String key = entry.getKey();
      for (String value : entry.getValue()) {
        this.add(key, value);
      }
    }
    return this;
  }
}
