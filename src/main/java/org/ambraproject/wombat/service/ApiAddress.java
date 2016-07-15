package org.ambraproject.wombat.service;

import com.google.common.collect.ImmutableList;
import org.ambraproject.wombat.util.UriUtil;
import org.apache.commons.io.Charsets;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ApiAddress {

  private final String path;
  private final ImmutableList<BasicNameValuePair> parameters;

  private ApiAddress(Builder builder) {
    this.path = builder.pathTokens.stream()
        .map(ApiAddress::encodePath)
        .collect(Collectors.joining("/"));
    this.parameters = ImmutableList.copyOf(builder.parameters);
  }

  private static String encodePath(String path) {
    try {
      return URLEncoder.encode(path, Charsets.UTF_8.toString());
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public URI buildUri(URL root) {
    return UriUtil.concatenate(root, getAddress());
  }

  public String getAddress() {
    return path + (parameters.isEmpty() ? "" : "?" + URLEncodedUtils.format(parameters, Charsets.UTF_8));
  }

  private static String escapeDoi(String doi) {
    return doi.replace("+", "+-").replace("/", "++");
  }

  public static Builder builder(String path) {
    return new Builder().addToken(path);
  }

  public static class Builder {
    private List<String> pathTokens = new ArrayList<>();
    private List<BasicNameValuePair> parameters = new ArrayList<>();

    private Builder() {
    }

    public Builder addToken(String token) {
      pathTokens.add(Objects.requireNonNull(token));
      return this;
    }

    public Builder embedDoi(String doiName) {
      return addToken(escapeDoi(doiName));
    }

    public Builder addParameter(String name, String value) {
      parameters.add(new BasicNameValuePair(name, Objects.requireNonNull(value)));
      return this;
    }

    public Builder addParameter(String name, int value) {
      return addParameter(name, Integer.toString(value));
    }

    public Builder addParameter(String name) {
      parameters.add(new BasicNameValuePair(name, null));
      return this;
    }

    public ApiAddress build() {
      return new ApiAddress(this);
    }
  }

  @Override
  public String toString() {
    return getAddress();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ApiAddress apiAddress = (ApiAddress) o;
    return parameters.equals(apiAddress.parameters) && path.equals(apiAddress.path);
  }

  @Override
  public int hashCode() {
    return 31 * path.hashCode() + parameters.hashCode();
  }
}
