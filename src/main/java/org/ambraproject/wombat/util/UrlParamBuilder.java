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
