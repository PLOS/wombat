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

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.Charsets;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder pattern applied as a convenience over {@link org.apache.http.client.utils.URLEncodedUtils}.
 */
public class UrlParamBuilder {
  private final List<NameValuePair> params;

  private UrlParamBuilder() {
    this.params = new ArrayList<>();
  }

  public static UrlParamBuilder params() {
    return new UrlParamBuilder();
  }

  public UrlParamBuilder add(String name, String value) {
    params.add(new BasicNameValuePair(name, value));
    return this;
  }

  public ImmutableList<NameValuePair> build() {
    return ImmutableList.copyOf(params);
  }

  public NameValuePair[] buildArray() {
    return params.toArray(new NameValuePair[params.size()]);
  }

  public String format() {
    return URLEncodedUtils.format(params, Charsets.UTF_8).replace("%2F", "/");
  }

  @Override
  public String toString() {
    return format();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    return params.equals(((UrlParamBuilder) o).params);
  }

  @Override
  public int hashCode() {
    return params.hashCode();
  }
}
