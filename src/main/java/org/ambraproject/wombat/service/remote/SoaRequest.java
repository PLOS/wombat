package org.ambraproject.wombat.service.remote;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.ambraproject.wombat.util.UriUtil;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public final class SoaRequest {

  private static final Joiner PATH_JOINER = Joiner.on('/');
  public static final Charset CHARSET = Charsets.UTF_8;

  private final String path;
  private final ImmutableList<NameValuePair> params;

  private SoaRequest(Builder builder) {
    this.path = PATH_JOINER.join(builder.pathTokens);
    this.params = ImmutableList.copyOf(builder.params);
  }

  @VisibleForTesting
  String getPath() {
    return path;
  }

  @VisibleForTesting
  ImmutableList<NameValuePair> getParams() {
    return params;
  }

  URI buildUri(SoaService soaService) {
    URI base = UriUtil.concatenate(soaService.getServerUrl(), path);
    URIBuilder uriBuilder = new URIBuilder(base);
    if (!params.isEmpty()) {
      uriBuilder.addParameters(params);
    }

    try {
      return uriBuilder.build();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  // Should be consistent with equals, therefore usable as a cache key.
  @Override
  public String toString() {
    return params.isEmpty() ? path :
        path + "?" + URLEncodedUtils.format(params, CHARSET);
  }


  public static Builder request(String path) {
    return new Builder(path);
  }

  public static class Builder {
    private final List<String> pathTokens;
    private final List<NameValuePair> params;

    private Builder(String path) {
      this.pathTokens = new ArrayList<>(2);
      Preconditions.checkArgument(!path.contains("?"));
      this.pathTokens.add(path);

      this.params = new ArrayList<>(4);
    }

    public Builder addPathToken(String token) {
      Preconditions.checkArgument(!token.contains("/"));
      Preconditions.checkArgument(!token.contains("?"));
      try {
        pathTokens.add(URLEncoder.encode(token, CHARSET.toString()));
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException(e);
      }
      return this;
    }

    public Builder addParameter(String name) {
      return addParameter(name, null);
    }

    public Builder addParameter(String name, String value) {
      params.add(new BasicNameValuePair(name, value));
      return this;
    }

    public SoaRequest build() {
      return new SoaRequest(this);
    }
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SoaRequest that = (SoaRequest) o;
    return path.equals(that.path) && params.equals(that.params);
  }

  @Override
  public int hashCode() {
    return 31 * path.hashCode() + params.hashCode();
  }
}
