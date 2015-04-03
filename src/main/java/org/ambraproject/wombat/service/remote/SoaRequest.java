package org.ambraproject.wombat.service.remote;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.ambraproject.wombat.util.UriUtil;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class SoaRequest {

  private final String path;
  private final ImmutableList<NameValuePair> params;

  private SoaRequest(Builder builder) {
    this.path = builder.path;
    this.params = ImmutableList.copyOf(builder.params);
  }

  @VisibleForTesting
  public String getPath() {
    return path;
  }

  @VisibleForTesting
  public ImmutableList<NameValuePair> getParams() {
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
        path + "?" + URLEncodedUtils.format(params, Charsets.UTF_8);
  }


  public static Builder request(String path) {
    return new Builder(path);
  }

  public static class Builder {
    private final String path;
    private final List<NameValuePair> params;

    private Builder(String path) {
      Preconditions.checkArgument(!path.contains("?"));
      this.path = Preconditions.checkNotNull(path);
      this.params = new ArrayList<>();
    }

    public Builder addParameter(String name) {
      return addParameter(name, null);
    }

    public Builder addParameter(String name, String value) {
      params.add(new BasicNameValuePair(name, value));
      return this;
    }

    public Builder addParameters(Collection<? extends NameValuePair> pairs) {
      params.addAll(pairs);
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
